package io.incepted.ultrafittimer.timer

import io.incepted.ultrafittimer.db.tempmodel.Round
import io.incepted.ultrafittimer.util.WorkoutSession
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * A helper class for identifying the current running session
 */

class TimerHelper(val warmupTime: Int, val cooldownTime: Int, val rounds: ArrayList<Round>) {

    // Used for resume / pause
    var resumed = AtomicBoolean(true)

    // Used for terminating timer in the middle
    var stopped = AtomicBoolean(false)

    // Used to normally complete the timer (after the timer runs through all cycle)
    private var completed = AtomicBoolean(false)

    private var switched = AtomicBoolean(false)

    // current position in rounds(ArrayList<Round>)
    var pos = AtomicInteger(0)

    // Session identifier flag (Warm Up? Work? Rest? Cooldown?)
    var curSession = AtomicInteger(0)

    var elapsed = AtomicLong(0)

    var disposable: Disposable? = null

    var totalRounds = 0


    init {
        curSession.set(if (warmupTime != 0) WorkoutSession.WARMUP else WorkoutSession.WORK)
        totalRounds = rounds.size
    }


    fun startTimer(emitter: ObservableEmitter<TickInfo>) {

        disposable = Observable.interval(1000, TimeUnit.MILLISECONDS)
                .startWith(0L)
                .takeWhile { !stopped.get() }
                .filter { resumed.get() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy {

                    // check for session switch
                    val currentSessionTime = getCurrentSessionTime(curSession.get())

                    if (shouldSwitchSession(currentSessionTime)) {
                        // Switching to the next session
                        switched.set(true)
                        when (curSession.get()) {
                            WorkoutSession.WARMUP -> curSession.set(WorkoutSession.WORK)
                            WorkoutSession.COOLDOWN -> completed.set(true) // flag for finishing the timer
                            else ->
                                if (!isLastRound())
                                // switch session between work - rest
                                    switchWorkRest()
                                else
                                // Last round! Finish the timer if the cooldown time is 0
                                    if (cooldownTime == 0) completed.set(true) // flag for finishing the timer
                                    else curSession.set(WorkoutSession.COOLDOWN)
                        }

                        // reset the elapsed time because we switched to the next session
                        if (!completed.get()) elapsed.set(0)
                    }


                    // Send the tick info
                    val session = curSession.get()
                    emitter.onNext(TickInfo(
                            session = session,
                            workoutName = getCurrentSessionName(session),
                            remianingSecs = (getCurrentSessionTime(session) - elapsed.get()),
                            roundCount = getRoundCount(session),
                            totalRounds = totalRounds,
                            switched = switched.get()))

                    // completing the timer after sending the last tick
                    if (completed.get()) {
                        emitter.onComplete()
                        terminateTimer()
                        return@subscribeBy
                    }

                    // After everything is done, increment elapsed time to move on to the next tick
                    elapsed.getAndAdd(1)
                    switched.set(false)
                }
    }


    private fun getCurrentSessionName(currentSession: Int): String {
        return when (currentSession) {
            WorkoutSession.WARMUP -> "Warm Up"
            WorkoutSession.COOLDOWN -> "Cool Down"
            WorkoutSession.WORK -> rounds[pos.get()].workoutName
            WorkoutSession.REST -> "Rest"
            else -> ""
        }
    }


    private fun getCurrentSessionTime(currentSession: Int): Int {
        return when (currentSession) {
            WorkoutSession.WARMUP -> warmupTime
            WorkoutSession.COOLDOWN -> cooldownTime
            WorkoutSession.WORK -> rounds[pos.get()].workSeconds
            WorkoutSession.REST -> rounds[pos.get()].restSeconds
            else -> 0
        }
    }


    private fun getRoundCount(currentSession: Int): Int {
        return when (currentSession) {
            WorkoutSession.WARMUP, WorkoutSession.COOLDOWN -> 0
            WorkoutSession.WORK -> pos.get() + 1
            WorkoutSession.REST -> pos.get() + 1
            else -> 0
        }
    }


    private fun shouldSwitchSession(sessionTime: Int): Boolean {
        return elapsed.get() == (sessionTime.toLong())
    }


    private fun switchWorkRest() {
        when (curSession.get()) {
            WorkoutSession.WORK -> {
                if (isNextRestEmpty()) pos.getAndAdd(1)
                else curSession.set(WorkoutSession.REST)
            }
            WorkoutSession.REST -> {
                curSession.set(WorkoutSession.WORK)
                pos.getAndAdd(1)
            }
        }
    }


    private fun isLastRound(): Boolean {
        return pos.get() == rounds.size - 1 &&
                (curSession.get() == WorkoutSession.REST || isNextRestEmpty())
    }


    private fun isNextRestEmpty(): Boolean {
        return curSession.get() == WorkoutSession.WORK && rounds[pos.get()].restSeconds == 0
    }


    fun setCompletedFlag() {
        completed.set(true)
    }


    fun pauseResumeTimer() {
        resumed.set(!resumed.get())
    }

    fun terminateTimer() {
        stopped.set(true)
        if (disposable?.isDisposed == true) disposable?.dispose()
    }


}