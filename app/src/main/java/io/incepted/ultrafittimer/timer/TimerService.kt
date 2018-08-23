package io.incepted.ultrafittimer.timer

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.*
import android.os.Binder
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.incepted.ultrafittimer.R
import io.incepted.ultrafittimer.UltraFitApp
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

        const val TIMER_NOTIFICATION_ID = 4343122

        // Intent bundle content keys
        const val BUNDLE_KEY_IS_PRESET = "bundle_key_warmup_time"
        const val BUNDLE_KEY_TARGET_ID = "bundle_key_work_name"

        // Broadcast receiver extra keys for Service-Activity communication
        const val BR_ACTION_TIMER_TICK_RESULT = "io.incepted.ultrafittimer.timer.TIMER_TICK"
        const val BR_ACTION_TIMER_COMPLETED_RESULT = "io.incepted.ultrafittimer.timer.TIMER_COMPLETED"
        const val BR_ACTION_TIMER_SESSION_SWITCH = "io.incepted.ultrafittimer.timer.TIMER_SESSION_SWITCH"
        const val BR_ACTION_TIMER_RESUME_PAUSE_STATE = "io.incepted.ultrafittimer.timer.TIMER_RESUME_PAUSE_STATE"
        const val BR_ACTION_TIMER_TERMINATED = "io.incepted.ultrafittimer.timer.TIMER_TERMINATED"
        const val BR_ACTION_TIMER_ERROR = "io.incepted.ultrafittimer.timer.TIMER_ERROR"

        // Tick info extras
        const val BR_EXTRA_KEY_TICK_SESSION_NAME = "io.incepted.ultrafittimer.timer.EXTRA_KEY_SESSION_NAME"
        const val BR_EXTRA_KEY_TICK_SESSION_REMAINING_SECS = "io.incepted.ultrafittimer.timer.EXTRA_KEY_SESSION_REMAINING_SEC"
        const val BR_EXTRA_KEY_TICK_SESSION_ROUND_TOTAL_SECS = "io.incepted.ultrafittimer.timer.EXTRA_KEY_SESSION_ROUND_TOTAL_SECS"
        const val BR_EXTRA_KEY_TICK_SESSION_SESSION = "io.incepted.ultrafittimer.timer.EXTRA_KEY_SESSION_SESSION"
        const val BR_EXTRA_KEY_TICK_SESSION_ROUND_COUNT = "io.incepted.ultrafittimer.timer.EXTRA_KEY_SESSION_COUNT"
        const val BR_EXTRA_KEY_TICK_SESSION_TOTAL_ROUND = "io.incepted.ultrafittimer.timer.EXTRA_KEY_TOTAL_ROUND"

        // Resume pause state extra
        const val BR_EXTRA_KEY_RESUME_PAUSE_STATE = "io.incepted.ultrafittimer.timer.EXTRA_KEY_RESUME_PAUSE_STATE"


        var SERVICE_STARTED = false
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

    private lateinit var mReceiver: TimerActionReceiver


    private val binder = TimerServiceBinder()


    // Timer properties

    private var fromPreset = false

    private var targetId = -1L

    private var presetId = -1L

    private var cueSeconds = 3

    private var timer: TimerSetting? = null

    private var timerHelper: TimerHelper? = null

    var timerCompleted = false

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
        super.onCreate()

        (application as UltraFitApp).getAppComponent().inject(this)

        cueSeconds = sharedPref.getString(resources.getString(R.string.pref_key_cue_seconds),
                cueSeconds.toString())?.toInt() ?: cueSeconds

        val notif: Notification = notificationUtil.getTimerNotification(null)
        startForeground(TIMER_NOTIFICATION_ID, notif)

        mReceiver = TimerActionReceiver()

        this.registerReceiver(mReceiver, getTimerActionIntentFilter())

    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!SERVICE_STARTED) {
            SERVICE_STARTED = true
            unpackExtra(intent)
            loadTimer(fromPreset, targetId)
        }
        return super.onStartCommand(intent, flags, startId)
    }


    override fun onDestroy() {
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
            fromPreset = intent.getBooleanExtra(BUNDLE_KEY_IS_PRESET, false)
            targetId = intent.getLongExtra(BUNDLE_KEY_TARGET_ID, 0L)
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
                            lastTick = it
                            sendTick(it)

                            if (it.switched) {
                                sendSessionSwitch(it)
                            }

                            if (it.remianingSecs <= cueSeconds) {
                                // fire tick
                            }

                            updateNotification(it)
                        },
                        onComplete = {
                            timerCompleted = true
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
        notifManager.notify(TIMER_NOTIFICATION_ID, notif)
    }


    private fun completeNotification() {
        val notif = notificationUtil.getCompleteNotification()
        notif.flags = Notification.FLAG_ONGOING_EVENT
        notifManager.notify(TIMER_NOTIFICATION_ID, notif)
    }


    // ------------------------------------------ User Interaction ------------------------------------

    fun resumePauseTimer() {
        timerHelper?.pauseResumeTimer()
        notifManager.notify(TIMER_NOTIFICATION_ID, notificationUtil.getTimerNotificationToggleResumePause(isTimerPaused()))
        sendResumePasueState()
    }


    fun terminateTimer() {
        timerHelper?.terminateTimer()
        disposable?.dispose()
        saveLastUsedTimer(fromPreset, targetId)
        saveHistory(timerCompleted, lastTick)
    }


    private fun sendTick(tick: TickInfo?) {
        if (tick != null) {
            val intent = Intent(BR_ACTION_TIMER_TICK_RESULT)
            intent.putExtra(BR_EXTRA_KEY_TICK_SESSION_SESSION, tick.session)
            intent.putExtra(BR_EXTRA_KEY_TICK_SESSION_NAME, tick.workoutName)
            intent.putExtra(BR_EXTRA_KEY_TICK_SESSION_REMAINING_SECS, tick.remianingSecs)
            intent.putExtra(BR_EXTRA_KEY_TICK_SESSION_ROUND_TOTAL_SECS, tick.roundTotalSecs)
            intent.putExtra(BR_EXTRA_KEY_TICK_SESSION_ROUND_COUNT, tick.roundCount)
            intent.putExtra(BR_EXTRA_KEY_TICK_SESSION_TOTAL_ROUND, tick.totalRounds)
            broadcaster.sendBroadcast(intent)
        }
    }


    fun handleUserAction(context: Context?, intent: Intent?) {
        when (intent?.action) {
            NotificationUtil.ACTION_INTENT_FILTER_DISMISS -> {
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
            val intent = Intent(BR_ACTION_TIMER_SESSION_SWITCH)
            intent.putExtra(BR_EXTRA_KEY_TICK_SESSION_SESSION, tickInfo.session)
            intent.putExtra(BR_EXTRA_KEY_TICK_SESSION_ROUND_TOTAL_SECS, tickInfo.roundTotalSecs)
            broadcaster.sendBroadcast(intent)
        }
    }


    private fun sendResumePasueState() {
        val intent = Intent(BR_ACTION_TIMER_RESUME_PAUSE_STATE)
        intent.putExtra(BR_EXTRA_KEY_RESUME_PAUSE_STATE, isTimerPaused())
        broadcaster.sendBroadcast(intent)
    }


    private fun sendTerminated() {
        broadcaster.sendBroadcast(Intent(BR_ACTION_TIMER_TERMINATED))
    }


    private fun sendCompleted() {
        broadcaster.sendBroadcast(Intent(BR_ACTION_TIMER_COMPLETED_RESULT))
    }


    private fun sendError() {
        broadcaster.sendBroadcast(Intent(BR_ACTION_TIMER_ERROR))
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
        editor.putLong(prefKey, targetId)
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


    fun finish() {
        SERVICE_STARTED = false
        stopForeground(true)
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
        Timber.d("Workout history id #$id saved!")
        finish()
    }

    override fun onHistorySaveNotAvailable() {
        sendError()
    }


    inner class TimerActionReceiver : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            handleUserAction(p0, p1)
        }
    }


}