package io.incepted.ultrafittimer.timer

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.*
import android.os.Binder
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dagger.android.AndroidInjection
import io.incepted.ultrafittimer.R
import io.incepted.ultrafittimer.db.DbRepository
import io.incepted.ultrafittimer.db.model.Preset
import io.incepted.ultrafittimer.db.model.TimerSetting
import io.incepted.ultrafittimer.db.model.WorkoutHistory
import io.incepted.ultrafittimer.db.source.LocalDataSource
import io.incepted.ultrafittimer.db.tempmodel.Round
import io.incepted.ultrafittimer.util.NotificationUtil
import io.incepted.ultrafittimer.util.RoundUtil
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class TimerService : Service(),
        LocalDataSource.OnPresetLoadedListener, LocalDataSource.OnTimerLoadedListener,
        LocalDataSource.OnHistorySavedListener {

    companion object {

        var SERVICE_STARTED = false

        var STOP_SELF = false

    }

    // Injection fields

    @Inject
    lateinit var sharedPref: SharedPreferences

    @Inject
    lateinit var notifManager: NotificationManager

    @Inject
    lateinit var broadcaster: LocalBroadcastManager

    @Inject
    lateinit var repository: DbRepository

    @Inject
    lateinit var notificationUtil: NotificationUtil

    @Inject
    lateinit var beepHelper: BeepHelper

    private lateinit var mReceiver: TimerActionReceiver

    private val binder = TimerServiceBinder()


    // Timer properties

    private var fromPreset = false

    private var targetId = -1L

    private var presetId = -1L

    private var cueSeconds = 3

    var timer: TimerSetting? = null

    private var timerHelper: TimerHelper? = null

    var totalProgress = 0

    // Timer is completed normally (not by stopping or any interruption)
    var timerCompleted = false

    var timerTerminated = false

    var lastTick: TickInfo? = null

    private var disposable: Disposable? = null


    inner class TimerServiceBinder : Binder() {
        fun getService(): TimerService {
            return this@TimerService
        }
    }


    // ---------------------------------------- Initialization --------------------------------------

    override fun onBind(p0: Intent?): IBinder {
        return binder
    }


    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()

        cueSeconds = sharedPref.getString(resources.getString(R.string.pref_key_cue_seconds),
                cueSeconds.toString())?.toInt() ?: cueSeconds

        val notif: Notification = notificationUtil.getTimerNotification(null)
        startForeground(TimerCommunication.TIMER_NOTIFICATION_ID, notif)

        mReceiver = TimerActionReceiver()

        this.registerReceiver(mReceiver, getTimerActionIntentFilter())

    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        SERVICE_STARTED = true
        unpackExtra(intent)
        loadTimer(fromPreset, targetId)

        return super.onStartCommand(intent, flags, startId)
    }


    override fun onDestroy() {
        // Unregister the notification action receiver
        unregisterReceiver(mReceiver)
        super.onDestroy()
    }


    private fun getTimerActionIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(NotificationUtil.ACTION_INTENT_FILTER_PAUSE)
        intentFilter.addAction(NotificationUtil.ACTION_INTENT_FILTER_RESUME)
        intentFilter.addAction(NotificationUtil.ACTION_INTENT_FILTER_DISMISS)
        return intentFilter
    }


    private fun unpackExtra(intent: Intent?) {
        if (intent != null && intent.extras != null) {
            fromPreset = intent.getBooleanExtra(TimerCommunication.BUNDLE_KEY_IS_PRESET, false)
            targetId = intent.getLongExtra(TimerCommunication.BUNDLE_KEY_TARGET_ID, 0L)
        }
    }


    private fun loadTimer(fromPreset: Boolean, targetId: Long) {
        if (fromPreset) repository.getPresetById(targetId, this)
        else repository.getTimerById(targetId, this)
    }


    private fun startTimer(timer: TimerSetting) {

        if (timerHelper == null)
            timerHelper = TimerHelper(warmupTime = timer.warmupSeconds,
                    cooldownTime = timer.cooldownSeconds,
                    rounds = (RoundUtil.getRoundList(timer, false)) as ArrayList<Round>)

        disposable = Observable.create<TickInfo> { it -> timerHelper?.startTimer(it) }
                .doOnDispose { Timber.d("disposed!") }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onNext = {
                            totalProgress++
                            lastTick = it
                            sendTick(it)

                            when {
                                it.switched -> {
                                    sendSessionSwitch(it)
                                    if (!it.firstTick) beepHelper.requestFire(BeepHelper.FLAG_BEEP)
                                }
                                it.remianingSecs in 1..cueSeconds ->
                                    beepHelper.requestFire(BeepHelper.FLAG_CUE)
                            }

                            updateNotification(it)
                        },
                        onComplete = {
                            timerCompleted = true
                            timerTerminated = true
                            beepHelper.requestFire(BeepHelper.FLAG_FINISH)
                            sendCompleted()
                            terminateTimer()
                            completeNotification()
                        },
                        onError = { it.printStackTrace() }
                )

    }



    // ------------------------------------------ Update UI -----------------------------------------


    private fun updateNotification(tickInfo: TickInfo?) {
        val notif = notificationUtil.getTimerNotification(tickInfo)
        notif.flags = Notification.FLAG_ONGOING_EVENT
        notifManager.notify(TimerCommunication.TIMER_NOTIFICATION_ID, notif)
    }


    private fun completeNotification() {
        val notif = notificationUtil.getCompleteNotification()
        notifManager.notify(TimerCommunication.TIMER_NOTIFICATION_ID, notif)
    }


    // ------------------------------------------ User Interaction ------------------------------------

    fun resumePauseTimer() {
        timerHelper?.pauseResumeTimer()
        notifManager.notify(TimerCommunication.TIMER_NOTIFICATION_ID, notificationUtil.getTimerNotificationToggleResumePause(isTimerPaused()))
        sendResumePauseState()
    }


    fun terminateTimer() {
        timerHelper?.terminateTimer()
        disposable?.dispose()
        saveLastUsedTimer(fromPreset, targetId)
        saveHistory(timerCompleted, lastTick)
    }


    private fun sendTick(tick: TickInfo?) {
        if (tick != null) {
            val intent = Intent(TimerCommunication.BR_ACTION_TIMER_TICK_RESULT)
            intent.putExtra(TimerCommunication.BR_EXTRA_KEY_TICK_SESSION_SESSION, tick.session)
            intent.putExtra(TimerCommunication.BR_EXTRA_KEY_TICK_SESSION_NAME, tick.workoutName)
            intent.putExtra(TimerCommunication.BR_EXTRA_KEY_TICK_SESSION_REMAINING_SECS, tick.remianingSecs)
            intent.putExtra(TimerCommunication.BR_EXTRA_KEY_TICK_SESSION_ROUND_TOTAL_SECS, tick.roundTotalSecs)
            intent.putExtra(TimerCommunication.BR_EXTRA_KEY_TICK_SESSION_ROUND_COUNT, tick.roundCount)
            intent.putExtra(TimerCommunication.BR_EXTRA_KEY_TICK_SESSION_TOTAL_ROUND, tick.totalRounds)
            broadcaster.sendBroadcast(intent)
        }
    }


    fun handleNotifAction(context: Context?, intent: Intent?) {
        when (intent?.action) {
            NotificationUtil.ACTION_INTENT_FILTER_DISMISS -> {
                timerCompleted = false
                timerTerminated = true
                sendTerminated()
                terminateTimer()
            }
            NotificationUtil.ACTION_INTENT_FILTER_RESUME -> resumePauseTimer()
            NotificationUtil.ACTION_INTENT_FILTER_PAUSE -> resumePauseTimer()
        }
    }


    fun isTimerPaused(): Boolean {
        return timerHelper?.resumed?.get() == false
    }


    private fun sendSessionSwitch(tickInfo: TickInfo?) {
        if (tickInfo != null) {
            val intent = Intent(TimerCommunication.BR_ACTION_TIMER_SESSION_SWITCH)
            intent.putExtra(TimerCommunication.BR_EXTRA_KEY_TICK_SESSION_SESSION, tickInfo.session)
            intent.putExtra(TimerCommunication.BR_EXTRA_KEY_TICK_SESSION_ROUND_TOTAL_SECS, tickInfo.roundTotalSecs)
            broadcaster.sendBroadcast(intent)
        }
    }


    private fun sendResumePauseState() {
        val intent = Intent(TimerCommunication.BR_ACTION_TIMER_RESUME_PAUSE_STATE)
        intent.putExtra(TimerCommunication.BR_EXTRA_KEY_RESUME_PAUSE_STATE, isTimerPaused())
        broadcaster.sendBroadcast(intent)
    }


    private fun sendTerminated() {
        broadcaster.sendBroadcast(Intent(TimerCommunication.BR_ACTION_TIMER_TERMINATED))
    }


    private fun sendCompleted() {
        broadcaster.sendBroadcast(Intent(TimerCommunication.BR_ACTION_TIMER_COMPLETED_RESULT))
    }


    private fun sendError() {
        broadcaster.sendBroadcast(Intent(TimerCommunication.BR_ACTION_TIMER_ERROR))
    }


    private fun saveHistory(completed: Boolean, tickInfo: TickInfo?) {
        val newHistory: WorkoutHistory = getWorkoutHistory(completed, tickInfo)
        repository.saveWorkoutHistory(newHistory, this)
    }


    private fun saveLastUsedTimer(fromPreset: Boolean, targetId: Long) {
        val editor = sharedPref.edit()
        val prefKey =
                resources.getString(if (fromPreset) R.string.pref_key_last_used_preset_id
                else R.string.pref_key_last_used_timer_id)
        val oppositePrefKey =
                resources.getString(if (fromPreset) R.string.pref_key_last_used_timer_id
                else  R.string.pref_key_last_used_preset_id)
        editor.putLong(prefKey, targetId)
        editor.putLong(oppositePrefKey, -1L)
        editor.apply()
    }


    private fun getWorkoutHistory(completed: Boolean, tickInfo: TickInfo?): WorkoutHistory {
        return if (completed)
            WorkoutHistory(timestamp = Date().time,
                    presetId = if (fromPreset) targetId else null,
                    timer_id = if (fromPreset) null else targetId)
        else
            WorkoutHistory(timestamp = Date().time,
                    presetId = if (fromPreset) targetId else null,
                    timer_id = if (fromPreset) null else targetId,
                    stoppedRound = tickInfo?.roundCount,
                    stoppedSecond = tickInfo?.remianingSecs?.toInt(),
                    stoppedSession = tickInfo?.session)
    }


    private fun finish() {
        beepHelper.release()
        SERVICE_STARTED = false
        stopSelf()
    }


    // ------------------------------------ Callbacks ---------------------------------------

    override fun onTimerLoaded(timer: TimerSetting) {
        this.timer = timer
        startTimer(timer)
    }


    override fun onTimerNotAvailable() {
        sendError()
    }


    override fun onPresetLoaded(preset: Preset) {
        presetId = preset.id ?: presetId
        repository.getTimerById(preset.timerSettingId, this)
    }


    override fun onPresetNotAvailable() {
        sendError()
    }


    override fun onHistorySaved(id: Long) {
        stopForeground(true)
        if (STOP_SELF)
            finish()
    }


    override fun onHistorySaveNotAvailable() {
        sendError()
    }


    inner class TimerActionReceiver : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            handleNotifAction(p0, p1)
        }
    }


}