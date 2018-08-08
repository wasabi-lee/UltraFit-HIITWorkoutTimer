package io.incepted.ultrafittimer.db.model

import android.databinding.BaseObservable
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import io.incepted.ultrafittimer.db.tempmodel.Round
import io.incepted.ultrafittimer.util.DbDelimiter
import io.incepted.ultrafittimer.util.NumberUtil
import io.incepted.ultrafittimer.util.TimerUtil
import io.incepted.ultrafittimer.util.WorkoutSession
import timber.log.Timber

class TimerSettingObservable(val timerSetting: TimerSetting) : BaseObservable() {

    var warmupObservable: ObservableField<String> = ObservableField()
    val workObservable: ObservableField<String> = ObservableField()
    val restObservable: ObservableField<String> = ObservableField()
    val roundCountObservable: ObservableField<String> = ObservableField()
    var cooldownObservable: ObservableField<String> = ObservableField()

    val isCustomizedObservable: ObservableBoolean = ObservableBoolean()
    val totalObservable: ObservableField<String> = ObservableField()

    var mRounds: MutableList<Round> = mutableListOf()


    var finalWarmup: Int = 0
    var finalWorks: String = ""
    var finalRests: String = ""
    var finalWorkName: String = ""
    var finalCooldown: Int = 0
    var customized: Boolean = false


    init {
        val isNewTimer: Boolean = timerSetting.roundNames == "-1"
                || timerSetting.workSeconds == "-1"
                || timerSetting.restSeconds == "-1"

        mRounds = if (isNewTimer) initDefaultRounds() else initRounds(timerSetting)

        warmupObservable.set(TimerUtil.secondsToTimeString(timerSetting.warmupSeconds))
        roundCountObservable.set(mRounds.size.toString())
        workObservable.set(if (mRounds.size != 0) TimerUtil.secondsToTimeString(mRounds[0].workSeconds) else "0")
        restObservable.set(if (mRounds.size != 0) TimerUtil.secondsToTimeString(mRounds[0].restSeconds) else "0")
        cooldownObservable.set(TimerUtil.secondsToTimeString(timerSetting.cooldownSeconds))

        isCustomizedObservable.set(timerSetting.customized)

        calculateTotal()
    }


    private fun initRounds(timerSetting: TimerSetting): MutableList<Round> {
        val splitWork: List<String> = timerSetting.workSeconds.split(DbDelimiter.DELIMITER)
        val splitRest: List<String> = timerSetting.restSeconds.split(DbDelimiter.DELIMITER)
        val splitNames: List<String> = timerSetting.roundNames.split(DbDelimiter.DELIMITER)

        val list: MutableList<Round> = mutableListOf()

        for (i in 0 until splitWork.size)
            list.add(Round(splitNames[i], splitWork[i].toInt(), splitRest[i].toInt()))

        return list
    }

    private fun initDefaultRounds(): MutableList<Round> {
        val list: MutableList<Round> = mutableListOf()
        for (i in 0 until 8)
            list.add(Round("Work", 20, 10))
        return list
    }


    fun handleChange(session: Int, offset: Int) {
        when (session) {
            WorkoutSession.WARMUP -> updateTimeField(warmupObservable, offset)
            WorkoutSession.WORK -> {
                updateTimeField(workObservable, offset)
                updateRounds(workObservable, session)
            }
            WorkoutSession.REST -> {
                updateTimeField(restObservable, offset)
                updateRounds(restObservable, session)
            }
            WorkoutSession.COOLDOWN -> updateTimeField(cooldownObservable, offset)
            WorkoutSession.ROUND -> {
                var newRoundCount = NumberUtil.toValidRoundCount(roundCountObservable.get()
                        ?: "1", offset)
                newRoundCount = if (newRoundCount <= 0) 1 else newRoundCount
                roundCountObservable.set(newRoundCount.toString())
                trimRoundCount(mRounds.size, newRoundCount)
            }
        }
        calculateTotal()
    }

    private fun updateTimeField(fieldToUpdate: ObservableField<String>, offset: Int) {
        val valueToUpdate: String = fieldToUpdate.get() ?: return
        var updatedValue: Int = TimerUtil.stringToSecond(valueToUpdate, offset)
        updatedValue = if (updatedValue < 0) 0 else updatedValue

        fieldToUpdate.set(TimerUtil.secondsToTimeString(updatedValue))
    }

    private fun trimRoundCount(oldRoundCount: Int, newRoundCount: Int) {
        if (oldRoundCount - newRoundCount > 0) subtractRounds(newRoundCount)
        else if (oldRoundCount - newRoundCount < 0) appendRounds(newRoundCount)
    }

    private fun updateRounds(fieldToUpdate: ObservableField<String>, session: Int) {
        val updatedValue = TimerUtil.stringToSecond(fieldToUpdate.get() ?: return, 0)
        for (round in mRounds)
            if (session == WorkoutSession.WORK)
                round.workSeconds = updatedValue
            else
                round.restSeconds = updatedValue
    }

    fun updateRounds(fieldToUpdate: ObservableField<String>, session: Int, offset: Int) {
        val updatedValue = TimerUtil.stringToSecond(fieldToUpdate.get() ?: return, offset)
        for (round in mRounds)
            if (session == WorkoutSession.WORK)
                round.workSeconds = updatedValue
            else
                round.restSeconds = updatedValue
    }


    fun removeRound(index: Int) {
        mRounds.removeAt(index)
    }

    fun insertRound(index: Int) {
        if (mRounds.size != 1)
            mRounds.add(index, mRounds[index - 1])
    }

    private fun removeLast() {
        if (mRounds.size > 1) {
            mRounds.removeAt(mRounds.size - 1)
        }
    }

    private fun subtractRounds(newRoundCount: Int) {
        while (mRounds.size > 1 && mRounds.size != newRoundCount)
            removeLast()
    }

    private fun appendRounds(newRoundCount: Int) {
        while (mRounds.size != newRoundCount) {
            mRounds.add(mRounds.last())
        }
    }

    fun calculateTotal() {
        val rounds: Int = mRounds.size
        val warmup: Int = TimerUtil.stringToSecond(warmupObservable.get() ?: return, 0)
        val work: Int = TimerUtil.stringToSecond(workObservable.get() ?: return, 0)
        val rest: Int = TimerUtil.stringToSecond(restObservable.get() ?: return, 0)
        val cooldown: Int = TimerUtil.stringToSecond(cooldownObservable.get() ?: return, 0)

        val total: Int = warmup + ((work + rest) * rounds) + cooldown
        totalObservable.set(TimerUtil.secondsToTimeString(total))
    }

    fun finalizeDetail() {
        finalWarmup = TimerUtil.stringToSecond(warmupObservable.get() ?: return, 0)
        finalCooldown = TimerUtil.stringToSecond(cooldownObservable.get() ?: return, 0)
        finalWorkName = mRounds.joinToString(DbDelimiter.DELIMITER) { it.workoutName }
        finalWorks = mRounds.joinToString(DbDelimiter.DELIMITER) { it.workSeconds.toString() }
        finalRests = mRounds.joinToString(DbDelimiter.DELIMITER) { it.restSeconds.toString() }
        customized = isCustomizedObservable.get()

        printThis() //TODO Delete this line later
    }

    fun checkIfEdited(): Boolean {
        return !(finalWarmup == timerSetting.warmupSeconds &&
                finalCooldown == timerSetting.cooldownSeconds &&
                finalWorkName == timerSetting.roundNames &&
                finalWorks == timerSetting.workSeconds &&
                finalRests == timerSetting.restSeconds &&
                customized == timerSetting.customized)
    }

    fun getFinalSetting(): TimerSetting {
        return TimerSetting(null, finalWarmup, finalWorkName, finalWorks, finalRests,
                finalCooldown, customized)
    }


    private fun printThis() {
        Timber.d("\n$this.toString()")
        for (i in 0 until mRounds.size)
            Timber.d("\nRound ${i + 1}: ${mRounds[i].workSeconds} - ${mRounds[i].restSeconds}")
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