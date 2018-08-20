package io.incepted.ultrafittimer.timer

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.os.Binder
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.incepted.ultrafittimer.UltraFitApp
import io.incepted.ultrafittimer.db.tempmodel.Round
import io.incepted.ultrafittimer.util.NotificationUtil
import io.incepted.ultrafittimer.util.RoundUtil
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class TimerService : Service() {

    @Inject
    lateinit var sharedPref: SharedPreferences

    @Inject
    lateinit var notificationUtil: NotificationUtil

    @Inject
    lateinit var broadcaster: LocalBroadcastManager

    private val binder = TimerServiceBinder()

    companion object {
        const val BUNDLE_KEY_WARMUP_TIME = "bundle_key_warmup_time"
        const val BUNDLE_KEY_WORK_NAMES = "bundle_key_work_name"
        const val BUNDLE_KEY_WORK_TIME = "bundle_key_work_time"
        const val BUNDLE_KEY_REST_TIME = "bundle_key_rest_time"
        const val BUNDLE_KEY_COOLDOWN_TIME = "bundle_key_cooldown_time"

        const val TIMER_NOTIFICATION_ID = 4343122

        const val BR_ACTION_TIMER_RESULT = "io.incepted.ultrafittimer.timer.TIMER_RESULT"
        const val BR_EXTRA_KEY_SESSION_NAME = "io.incepted.ultrafittimer.timer.SESSION_NAME"
        const val BR_EXTRA_KEY_SESSION_REMAINING_SECS = "io.incepted.ultrafittimer.timer.SESSION_REMAINING_SEC"
        const val BR_EXTRA_KEY_SESSION_SESSION = "io.incepted.ultrafittimer.timer.SESSION_SESSION"
        const val BR_EXTRA_KEY_SESSION_ROUND_COUNT = "io.incepted.ultrafittimer.timer.SESSION_COUNT"
    }


    private var warmup = 0

    private var cooldown = 0

    private var mRounds = ArrayList<Round>()

    private var timerHelper: TimerHelper? = null

    private var disposable: Disposable? = null


    inner class TimerServiceBinder : Binder() {
        fun getService(): TimerService {
            return this@TimerService
        }
    }


    override fun onCreate() {
        super.onCreate()

        (application as UltraFitApp).getAppComponent().inject(this)

        val notif: Notification = notificationUtil.getTimerNotification().build()
        startForeground(TIMER_NOTIFICATION_ID, notif)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        unpackExtra(intent)
        startTimer()
        return super.onStartCommand(intent, flags, startId)
    }


    private fun unpackExtra(intent: Intent?) {
        if (intent != null && intent.extras != null) {
            warmup = intent.extras!!.getInt(BUNDLE_KEY_WARMUP_TIME)
            cooldown = intent.extras!!.getInt(BUNDLE_KEY_COOLDOWN_TIME)

            val roundNames = intent.extras!!.getString(BUNDLE_KEY_WORK_NAMES) ?: "-"
            val workSeconds = intent.extras!!.getString(BUNDLE_KEY_WORK_TIME) ?: "0"
            val restSeconds = intent.extras!!.getString(BUNDLE_KEY_REST_TIME) ?: "0"
            mRounds = RoundUtil.getRoundList(roundNames, workSeconds, restSeconds) as ArrayList<Round>
        }
    }


    private fun startTimer() {
        if (timerHelper == null) timerHelper = TimerHelper(warmup, cooldown, mRounds)

        disposable = Observable.create<RoundInfo> { it -> timerHelper?.startTimer(it) }
                .doOnDispose { Timber.d("disposed!") }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy {
                    sendTick(it)
                }
    }


    fun resumePauseTimer() {
        timerHelper?.pauseResumeTimer()
    }


    fun terminateTimer() {
        timerHelper?.terminateTimer()
        disposable?.dispose()
        stopForeground(true)
        stopSelf()
    }


    fun sendTick(tick: RoundInfo?) {
        if (tick != null) {
            val intent = Intent(BR_ACTION_TIMER_RESULT)
            intent.putExtra(BR_EXTRA_KEY_SESSION_SESSION, tick.session)
            intent.putExtra(BR_EXTRA_KEY_SESSION_NAME, tick.workoutName)
            intent.putExtra(BR_EXTRA_KEY_SESSION_REMAINING_SECS, tick.remianingSecs)
            intent.putExtra(BR_EXTRA_KEY_SESSION_ROUND_COUNT, tick.roundCount)
            broadcaster.sendBroadcast(intent)
        }
    }


    override fun onBind(p0: Intent?): IBinder {
        return binder
    }


}