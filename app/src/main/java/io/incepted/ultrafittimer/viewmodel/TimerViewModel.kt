package io.incepted.ultrafittimer.viewmodel

import android.app.Application
import android.content.Intent
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import io.incepted.ultrafittimer.R
import io.incepted.ultrafittimer.db.DbRepository
import io.incepted.ultrafittimer.db.model.Preset
import io.incepted.ultrafittimer.db.model.TimerSetting
import io.incepted.ultrafittimer.timer.TimerService
import io.incepted.ultrafittimer.util.SingleLiveEvent
import io.incepted.ultrafittimer.util.TimerUtil
import io.incepted.ultrafittimer.util.WorkoutSession
import timber.log.Timber
import javax.inject.Inject

class TimerViewModel @Inject constructor(val appContext: Application, val repository: DbRepository)
    : AndroidViewModel(appContext) {


    val snackbarResource = SingleLiveEvent<Int>()

    lateinit var preset: Preset

    lateinit var timer: TimerSetting

    val resumePause = SingleLiveEvent<Void>()

    val exitTimer = SingleLiveEvent<Void>()

    val completeTimer = SingleLiveEvent<Void>()


    // ----------------------- UI field -------------------------

    val session = ObservableField<Int>()

    val workoutName = ObservableField<String>()

    val remainingTime = ObservableField<String>()

    val roundCount = ObservableField<String>()


    fun start() {
        Timber.d("Started")
    }


    // ------------------------------------------ User Interaction -------------------------------------


    fun handleBroadcastResult(intent: Intent?) {
        if (intent == null) return
        when (intent.action) {
            TimerService.BR_ACTION_TIMER_TICK_RESULT -> updateTime(intent)
            TimerService.BR_ACTION_TIMER_COMPLETED_RESULT -> completeTimer.value = null
            TimerService.BR_ACTION_TIMER_ERROR -> handleError()
        }
    }


    fun exitTimer() {
        exitTimer.value = null
    }


    fun resumePauseTimer() {
        resumePause.value = null
    }


    private fun handleError() {
        snackbarResource.value = R.string.error_unexpected
        exitTimer.value = null
    }


    private fun updateTime(intent: Intent) {
        val sess = intent.getIntExtra(TimerService.BR_EXTRA_KEY_SESSION_SESSION, 0)
        val name = intent.getStringExtra(TimerService.BR_EXTRA_KEY_SESSION_NAME) ?: ""
        val remaining = intent.getLongExtra(TimerService.BR_EXTRA_KEY_SESSION_REMAINING_SECS, 0L)
        val round = intent.getIntExtra(TimerService.BR_EXTRA_KEY_SESSION_ROUND_COUNT, 0)
        val totalRounds = intent.getIntExtra(TimerService.BR_EXTRA_KEY_SESSION_TOTAL_ROUND, 0)

        val roundStr = when (sess) {
            WorkoutSession.WARMUP -> "0/$totalRounds"
            WorkoutSession.COOLDOWN -> "$totalRounds/$totalRounds"
            else -> "$round/$totalRounds"
        }

        workoutName.set(name)
        remainingTime.set(TimerUtil.secondsToTimeString(remaining.toInt()))
        roundCount.set(roundStr)

    }

}