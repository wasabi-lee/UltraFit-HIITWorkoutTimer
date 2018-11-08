package io.incepted.ultrafittimer.viewmodel

import android.app.Application
import android.content.Intent
import android.graphics.Color
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import io.incepted.ultrafittimer.R
import io.incepted.ultrafittimer.db.DbRepository
import io.incepted.ultrafittimer.db.model.Preset
import io.incepted.ultrafittimer.db.model.TimerSetting
import io.incepted.ultrafittimer.timer.TickInfo
import io.incepted.ultrafittimer.timer.TimerCommunication
import io.incepted.ultrafittimer.util.SingleLiveEvent
import io.incepted.ultrafittimer.util.TimerUtil
import io.incepted.ultrafittimer.util.WorkoutSession
import javax.inject.Inject

class TimerViewModel @Inject constructor(val appContext: Application, val repository: DbRepository)
    : AndroidViewModel(appContext) {


    val snackbarResource = SingleLiveEvent<Int>()

    lateinit var preset: Preset

    lateinit var timer: TimerSetting

    val resumePause = SingleLiveEvent<Void>()

    val exitTimer = SingleLiveEvent<Void>()

    val completeTimer = SingleLiveEvent<Boolean>()

    // Default MutableLiveData because we need to keep the
    val finishActivity = SingleLiveEvent<Void>()

    val animateWave = SingleLiveEvent<TickInfo>()

    val resumePauseWave = SingleLiveEvent<Boolean>()


    // ----------------------- UI field -------------------------

    val session = ObservableField<Int>()

    val workoutName = ObservableField<String>()

    val remainingTime = ObservableField<String>()

    val roundCount = ObservableField<String>()

    val paused = ObservableBoolean(false)

    val locked = ObservableBoolean(false)

    val backgroundColor = ObservableField<Int>(Color.parseColor("#ffffff"))

    init {
        completeTimer.value = false
    }

    fun start() {
    }


    // ------------------------------------------ User Interaction -------------------------------------


    fun handleBroadcastResult(intent: Intent?) {
        if (intent == null) return
        when (intent.action) {
            TimerCommunication.BR_ACTION_TIMER_TICK_RESULT -> updateTime(intent)
            TimerCommunication.BR_ACTION_TIMER_COMPLETED_RESULT -> handleTimerCompletion()
            TimerCommunication.BR_ACTION_TIMER_RESUME_PAUSE_STATE -> adjustUIforResumePause(intent)
            TimerCommunication.BR_ACTION_TIMER_SESSION_SWITCH -> handleSessionSwitch(intent)
            TimerCommunication.BR_ACTION_TIMER_TERMINATED -> finishActivity.value = null
            TimerCommunication.BR_ACTION_TIMER_ERROR -> handleError()
        }
    }


    fun setInitialValues(tickInfo: TickInfo?, timerPaused: Boolean?) {
        // Called when the activity is re launched from the foreground notification
        // Updates the UI with the latest value that TimerService holds
        if (tickInfo != null) {
            workoutName.set(tickInfo.workoutName)
            remainingTime.set(TimerUtil.secondsToTimeString(tickInfo.remianingSecs.toInt()))
            roundCount.set(getRoundDisplayValue(tickInfo.session, tickInfo.roundCount, tickInfo.totalRounds))
            backgroundColor.set(WorkoutSession.getSessionColor(appContext, tickInfo.session))
        }
        paused.set(timerPaused ?: false)

    }


    fun exitTimer() {
        exitTimer.value = null
    }


    fun resumePauseTimer() {
        resumePause.value = null
    }

    fun toggleScreenInteraction() {
        locked.set(!locked.get())
    }


    private fun handleError() {
        snackbarResource.value = R.string.error_unexpected
        finishActivity.value = null
    }


    private fun updateTime(intent: Intent) {
        val sess = intent.getIntExtra(TimerCommunication.BR_EXTRA_KEY_TICK_SESSION_SESSION, 0)
        val name = intent.getStringExtra(TimerCommunication.BR_EXTRA_KEY_TICK_SESSION_NAME) ?: ""
        val remaining = intent.getLongExtra(TimerCommunication.BR_EXTRA_KEY_TICK_SESSION_REMAINING_SECS, 0L)
        val round = intent.getIntExtra(TimerCommunication.BR_EXTRA_KEY_TICK_SESSION_ROUND_COUNT, 0)
        val totalRounds = intent.getIntExtra(TimerCommunication.BR_EXTRA_KEY_TICK_SESSION_TOTAL_ROUND, 0)

        val roundStr = getRoundDisplayValue(sess, round, totalRounds)

        workoutName.set(name)
        remainingTime.set(TimerUtil.secondsToTimeString(remaining.toInt()))
        roundCount.set(roundStr)

    }


    private fun adjustUIforResumePause(intent: Intent) {
        val state = intent.getBooleanExtra(TimerCommunication.BR_EXTRA_KEY_RESUME_PAUSE_STATE, false)
        paused.set(state) // adjusting pause/resume icon on the screen accordingly
        resumePauseWave.value = state // pause/resume wave animation accordingly
    }


    private fun handleSessionSwitch(intent: Intent) {
        val sess = intent.getIntExtra(TimerCommunication.BR_EXTRA_KEY_TICK_SESSION_SESSION, 0)
        val roundTotal = intent.getLongExtra(TimerCommunication.BR_EXTRA_KEY_TICK_SESSION_ROUND_TOTAL_SECS, 0L)
        val firstTick = intent.getBooleanExtra(TimerCommunication.BR_EXTRA_KEY_TICK_SESSION_FIRST_TICK, false);
        animateWave.value = TickInfo(sess, roundTotal, firstTick)
        backgroundColor.set(WorkoutSession.getSessionColor(appContext, sess))
    }


    private fun handleTimerCompletion() {
        completeTimer.value = true
        locked.set(true) // disabling further interaction
    }


    private fun getRoundDisplayValue(session: Int, currentRound: Int, totalRounds: Int): String {
        return when (session) {
            WorkoutSession.WARMUP -> "0/$totalRounds"
            WorkoutSession.COOLDOWN -> "$totalRounds/$totalRounds"
            else -> "$currentRound/$totalRounds"
        }
    }

}