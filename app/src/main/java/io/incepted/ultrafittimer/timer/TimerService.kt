package io.incepted.ultrafittimer.timer

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
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

        // Broadcast receiver extra keys
        const val BR_ACTION_TIMER_TICK_RESULT = "io.incepted.ultrafittimer.timer.TIMER_RESULT"
        const val BR_ACTION_TIMER_COMPLETED_RESULT = "io.incepted.ultrafittimer.timer.COMPLETED_RESULT"
        const val BR_ACTION_TIMER_ERROR = "io.incepted.ultrafittimer.timer.TIMER_ERROR"

        const val BR_EXTRA_KEY_SESSION_NAME = "io.incepted.ultrafittimer.timer.SESSION_NAME"
        const val BR_EXTRA_KEY_SESSION_REMAINING_SECS = "io.incepted.ultrafittimer.timer.SESSION_REMAINING_SEC"
        const val BR_EXTRA_KEY_SESSION_SESSION = "io.incepted.ultrafittimer.timer.SESSION_SESSION"
        const val BR_EXTRA_KEY_SESSION_ROUND_COUNT = "io.incepted.ultrafittimer.timer.SESSION_COUNT"
        const val BR_EXTRA_KEY_SESSION_TOTAL_ROUND = "io.incepted.ultrafittimer.timer.TOTAL_ROUND"
    }


    @Inject
    lateinit var sharedPref: SharedPreferences

    @Inject
    lateinit var notificationUtil: NotificationUtil

    @Inject
    lateinit var broadcaster: LocalBroadcastManager

    @Inject
    lateinit var repository: DbRepository

    private val binder = TimerServiceBinder()

    private var cueSeconds = 3

    private var fromPreset = false

    private var targetId = -1L

    private var presetId = -1L

    private var timer: TimerSetting? = null

    private var timerHelper: TimerHelper? = null

    private var disposable: Disposable? = null

    var timerCompleted = false

    private var lastTick: TickInfo? = null


    inner class TimerServiceBinder : Binder() {
        fun getService(): TimerService {
            return this@TimerService
        }
    }


    override fun onCreate() {
        super.onCreate()

        (application as UltraFitApp).getAppComponent().inject(this)

        cueSeconds = sharedPref.getString("pref_key_cue_seconds", cueSeconds.toString())?.toInt() ?: cueSeconds

        val notif: Notification = notificationUtil.getTimerNotification().build()
        startForeground(TIMER_NOTIFICATION_ID, notif)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        unpackExtra(intent)
        loadTimer(fromPreset, targetId)
        return super.onStartCommand(intent, flags, startId)
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
                                Timber.d("Switched!")
                            }
                            if (it.remianingSecs <= cueSeconds) {
                                Timber.d("Tick!")
                            }
                        },
                        onComplete = {
                            timerCompleted = true
                            sendFinished()
                        },
                        onError = { it.printStackTrace() }
                )
    }


    fun resumePauseTimer() {
        timerHelper?.pauseResumeTimer()
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
            intent.putExtra(BR_EXTRA_KEY_SESSION_SESSION, tick.session)
            intent.putExtra(BR_EXTRA_KEY_SESSION_NAME, tick.workoutName)
            intent.putExtra(BR_EXTRA_KEY_SESSION_REMAINING_SECS, tick.remianingSecs)
            intent.putExtra(BR_EXTRA_KEY_SESSION_ROUND_COUNT, tick.roundCount)
            intent.putExtra(BR_EXTRA_KEY_SESSION_TOTAL_ROUND, tick.totalRounds)
            broadcaster.sendBroadcast(intent)
        }
    }

    private fun sendFinished() {
        broadcaster.sendBroadcast(Intent(BR_ACTION_TIMER_COMPLETED_RESULT))
    }


    private fun sendError() {
        broadcaster.sendBroadcast(Intent(BR_ACTION_TIMER_ERROR))
    }


    override fun onBind(p0: Intent?): IBinder {
        return binder
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
        stopForeground(true)
        stopSelf()
    }

    override fun onHistorySaveNotAvailable() {
        sendError()
    }

}