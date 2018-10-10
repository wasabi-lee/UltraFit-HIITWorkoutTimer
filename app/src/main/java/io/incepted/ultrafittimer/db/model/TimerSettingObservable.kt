package io.incepted.ultrafittimer.db.model

import androidx.databinding.BaseObservable
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import io.incepted.ultrafittimer.OpenForTesting
import io.incepted.ultrafittimer.db.tempmodel.Round
import io.incepted.ultrafittimer.util.*

@OpenForTesting
class TimerSettingObservable(final val timerSetting: TimerSetting) : BaseObservable() {

    final var warmupObservable: ObservableField<String> = ObservableField()
    final val workObservable: ObservableField<String> = ObservableField()
    final val restObservable: ObservableField<String> = ObservableField()
    final val roundCountObservable: ObservableField<String> = ObservableField()
    final var cooldownObservable: ObservableField<String> = ObservableField()

    final val isCustomizedObservable: ObservableBoolean = ObservableBoolean()
    val totalObservable: ObservableField<String> = ObservableField()

    final var mRounds: MutableList<Round> = mutableListOf()
        set(value) {
            field = value
            workObservable.set(TimerUtil.secondsToTimeString(value[0].workSeconds))
            restObservable.set(TimerUtil.secondsToTimeString(value[0].restSeconds))
            roundCountObservable.set(value.size.toString())
            calculateTotal()
        }

    var finalWarmup: Int = 0
    var finalWorks: String = ""
    var finalRests: String = ""
    var finalWorkName: String = ""
    var finalCooldown: Int = 0
    var customized: Boolean = false


    init {
        val isNewTimer: Boolean = timerSetting.defaultTimer

        mRounds = if (isNewTimer) RoundUtil.getDefaultRoundList() else RoundUtil.getRoundList(timerSetting, false)

        warmupObservable.set(TimerUtil.secondsToTimeString(timerSetting.warmupSeconds))
        roundCountObservable.set(mRounds.size.toString())
        workObservable.set(if (mRounds.size != 0) TimerUtil.secondsToTimeString(mRounds[0].workSeconds) else "00:00")
        restObservable.set(if (mRounds.size != 0) TimerUtil.secondsToTimeString(mRounds[0].restSeconds) else "00:00")
        cooldownObservable.set(TimerUtil.secondsToTimeString(timerSetting.cooldownSeconds))

        isCustomizedObservable.set(timerSetting.customized)

        calculateTotal()
    }


    fun handleChange(session: Int, offset: Int) {
        when (session) {
            WorkoutSession.WARMUP ->
                updateTimeField(session, warmupObservable, offset)
            WorkoutSession.WORK -> {
                updateTimeField(session, workObservable, offset)
                updateRounds(workObservable, session)
            }
            WorkoutSession.REST -> {
                updateTimeField(session, restObservable, offset)
                updateRounds(restObservable, session)
            }
            WorkoutSession.COOLDOWN ->
                updateTimeField(session, cooldownObservable, offset)
            WorkoutSession.ROUND ->
                trimRoundCount(offset)
        }
        calculateTotal()
    }


    private fun updateTimeField(session: Int, fieldToUpdate: ObservableField<String>, offset: Int) {

        val valueToUpdate: String = fieldToUpdate.get() ?: return
        var updatedValue: Int = TimerUtil.stringToSecondWithOffset(valueToUpdate, offset)

        updatedValue = if (updatedValue <= 0) {
            if (session == WorkoutSession.WORK) 1 else 0
        } else updatedValue

        fieldToUpdate.set(TimerUtil.secondsToTimeString(updatedValue))
    }


    private fun trimRoundCount(offset: Int) {
        var newRoundCount = NumberUtil.toValidRoundCount(roundCountObservable.get() ?: "1", offset)
        newRoundCount = if (newRoundCount <= 0) 1 else newRoundCount
        roundCountObservable.set(newRoundCount.toString())
        adjustRounds(mRounds.size, newRoundCount)
    }


    private fun adjustRounds(oldRoundCount: Int, newRoundCount: Int) {
        if (oldRoundCount - newRoundCount > 0) subtractRounds(newRoundCount)
        else if (oldRoundCount - newRoundCount < 0) appendRounds(newRoundCount)
    }


    private fun updateRounds(fieldToUpdate: ObservableField<String>, session: Int) {
        if (isCustomizedObservable.get()) {
            // the customized setting gets reset when the user changes the input in the MainActivity again
            isCustomizedObservable.set(false)
            resetRoundNames()
        }

        // Get the value from the EditText and apply the change to the rounds 
        val updatedValue = TimerUtil.stringToSecond(fieldToUpdate.get() ?: return)
        for (round in mRounds)
            if (session == WorkoutSession.WORK)
                round.workSeconds = updatedValue
            else
                round.restSeconds = updatedValue

    }


    private fun resetRoundNames() {
        mRounds.forEach { it.workoutName = "Work" }
    }


    private fun removeLast() {
        if (mRounds.size > 1) mRounds.removeAt(mRounds.size - 1)
    }


    private fun subtractRounds(newRoundCount: Int) {
        while (mRounds.size > 1 && mRounds.size != newRoundCount)
            removeLast()
    }


    private fun appendRounds(newRoundCount: Int) {
        while (mRounds.size != newRoundCount)
            mRounds.add(mRounds.last())
    }


    final fun calculateTotal() {
        val warmup: Int = TimerUtil.stringToSecond(warmupObservable.get() ?: return)
        val cooldown: Int = TimerUtil.stringToSecond(cooldownObservable.get() ?: return)
        val workoutTime = RoundUtil.calculateWorkoutTime(mRounds)
        val total = warmup + workoutTime + cooldown
        totalObservable.set(TimerUtil.secondsToTimeString(total))
    }


    fun finalizeDetail() {
        handleChange(WorkoutSession.WARMUP, 0)
        handleChange(WorkoutSession.COOLDOWN, 0)
        handleChange(WorkoutSession.ROUND, 0)

        if (!isCustomizedObservable.get()) {
            // update the changes in the EditText ONLY when the workout is not customized.
            // When customized, the customized setting is in higher priority
            // therefore the change in the EditText doesn't affect the workout.
            handleChange(WorkoutSession.WORK, 0)
            handleChange(WorkoutSession.REST, 0)
        }

        finalWarmup = TimerUtil.stringToSecond(warmupObservable.get() ?: return)
        finalCooldown = TimerUtil.stringToSecond(cooldownObservable.get() ?: return)
        customized = isCustomizedObservable.get()

        val arr = RoundUtil.joinListToString(mRounds)
        finalWorkName = arr[0]
        finalWorks = arr[1]
        finalRests = arr[2]
    }


    fun checkIfEdited(): Boolean {
        return TimerSettingChangeChecker.timerSettingChanged(
                timerSetting.warmupSeconds, finalWarmup,
                timerSetting.cooldownSeconds, finalCooldown,
                timerSetting.mRounds, mRounds
        )
    }


    fun getFinalSetting(): TimerSetting {
        return TimerSetting(null, finalWarmup, finalWorkName, finalWorks, finalRests,
                finalCooldown, customized)
    }


    override fun toString(): String {
        return """
            warmup: ${warmupObservable.get()}
            roundCount: ${roundCountObservable.get()}
            actualRoundCount: ${mRounds.size}
            work: ${workObservable.get()}
            rest: ${restObservable.get()}
            cooldown: ${cooldownObservable.get()}
        """.trimIndent()
    }
}