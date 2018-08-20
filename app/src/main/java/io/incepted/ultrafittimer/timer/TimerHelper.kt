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

    var resumed = AtomicBoolean(true)

    var stopped = AtomicBoolean(false)

    var pos = AtomicInteger(0)

    var curSession = AtomicInteger(0)

    var elapsed = AtomicLong(0)

    var disposable: Disposable? = null

    init {
        curSession.set(if (warmupTime != 0) WorkoutSession.WARMUP else WorkoutSession.WORK)
    }


    fun startTimer(emitter: ObservableEmitter<RoundInfo>) {

        disposable = Observable.interval(1000, TimeUnit.MILLISECONDS)
                .takeWhile { !stopped.get() }
                .filter { resumed.get() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy {

                    val session = curSession
                    val sessionName = getCurrentSessionName(session.get())
                    val sessionTime = getCurrentSessionTime(session.get())
                    val roundCount = getRoundCount(session.get())

                    // Send the tick info
                    emitter.onNext(RoundInfo(session.get(), sessionName, sessionTime - elapsed.get(), roundCount))

                    // check for session switch
                    if (!shouldSwitchSession(sessionTime)) {
                        elapsed.getAndAdd(1)

                    } else {
                        // reset elapsed time.
                        elapsed.set(0)

                        // switch the round. dispose this observable if the timer reached the end.
                        when (curSession.get()) {
                            WorkoutSession.WARMUP -> curSession.set(WorkoutSession.WORK)
                            WorkoutSession.COOLDOWN -> {
                                emitter.onComplete()
                                disposable?.dispose()
                                return@subscribeBy
                            }
                            else ->
                                if (!isLastRound())
                                // switch session between work - rest
                                    switchWorkRest()
                                else
                                // Last round! Finish the timer if the cooldown time is 0
                                    if (cooldownTime == 0) {
                                        emitter.onComplete()
                                        disposable?.dispose()
                                        return@subscribeBy
                                    } else
                                        curSession.set(WorkoutSession.COOLDOWN)
                        }
                    }
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
        return elapsed.get() == (sessionTime.toLong() - 1L)
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


    fun pauseResumeTimer() {
        resumed.set(!resumed.get())
    }

    fun terminateTimer() {
        stopped.set(true)
    }


}