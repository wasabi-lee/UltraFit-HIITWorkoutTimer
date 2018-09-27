package io.incepted.ultrafittimer.util

import io.incepted.ultrafittimer.db.DbDelimiter
import io.incepted.ultrafittimer.db.model.TimerSetting
import io.incepted.ultrafittimer.db.tempmodel.Round

object RoundUtil {


    fun getRoundList(timerSetting: TimerSetting, includeWarmupCooldown: Boolean): MutableList<Round> {
        if (includeWarmupCooldown)
            return getFullRoundList(timerSetting)
        return getRoundList(timerSetting.roundNames, timerSetting.workSeconds, timerSetting.restSeconds)
    }


    /**
     *  Returns the rounds as a list including warmup and cooldown in the first and last position.
     */
    private fun getFullRoundList(timerSetting: TimerSetting): MutableList<Round> {
        val result =
                getRoundList(timerSetting.roundNames, timerSetting.workSeconds, timerSetting.restSeconds)

        val warmupRound = Round("Warm Up", timerSetting.warmupSeconds, 0)
        val cooldownRound = Round("Cool Down", timerSetting.cooldownSeconds, 0)

        warmupRound.isWarmup = true
        cooldownRound.isCooldown = true

        result.add(0, warmupRound)
        result.add(cooldownRound)

        return result
    }


    /**
     * Returns the list of rounds from the given String values.
     * The parameters are String values representing each round's detail,
     * concatenated with a specified delimiter.
     */
    fun getRoundList(names: String, works: String, rests: String): MutableList<Round> {

        val result = mutableListOf<Round>()

        val nameArr = names.split(DbDelimiter.DELIMITER)
        val workArr = works.split(DbDelimiter.DELIMITER)
        val restArr = rests.split(DbDelimiter.DELIMITER)

        for (i in 0 until nameArr.size)
            result.add(Round(nameArr[i], workArr[i].toInt(), restArr[i].toInt()))

        return result
    }


    fun joinListToString(l: List<Round>): Array<String> {

        val res = Array(3) { "" }
        val names = l.joinToString(DbDelimiter.DELIMITER) { it.workoutName }
        val works = l.joinToString(DbDelimiter.DELIMITER) { it.workSeconds.toString() }
        val rests = l.joinToString(DbDelimiter.DELIMITER) { it.restSeconds.toString() }

        res[0] = names
        res[1] = works
        res[2] = rests

        return res
    }


    fun getDefaultRoundList(): MutableList<Round> {
        val list: MutableList<Round> = mutableListOf()
        for (i in 0 until 8)
            list.add(Round(
                    TimerSetting.DEFAULT_WORK_NAME,
                    TimerSetting.DEFAULT_WORK_SECONDS,
                    TimerSetting.DEFAULT_REST_SECONDS))
        return list
    }


    fun calculateWorkoutTime(rounds: List<Round>): Int {
        val works = rounds.joinToString(DbDelimiter.DELIMITER) { it.workSeconds.toString() }
        val rests = rounds.joinToString(DbDelimiter.DELIMITER) { it.restSeconds.toString() }
        return calculateSessionTime(works) + calculateSessionTime(rests)
    }

    fun calculateWorkoutTime(works: String, rests: String): Int {
        return calculateSessionTime(works) + calculateSessionTime(rests)
    }


    fun calculateSessionTime(session: String): Int {
        val arr = session.split(DbDelimiter.DELIMITER)
        var sum = 0
        arr.forEach {
            sum += it.toInt()
        }
        return sum
    }


}
