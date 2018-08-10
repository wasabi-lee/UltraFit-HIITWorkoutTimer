package io.incepted.ultrafittimer.util

import io.incepted.ultrafittimer.db.model.TimerSetting
import io.incepted.ultrafittimer.db.tempmodel.Round

object RoundUtil {

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

    fun getRoundList(timerSetting: TimerSetting): MutableList<Round> {
        return getRoundList(timerSetting.roundNames, timerSetting.workSeconds, timerSetting.restSeconds)
    }

    fun getRoundList(names: String, works: String, rests: String): MutableList<Round> {

        val result = mutableListOf<Round>()

        val nameArr = names.split(DbDelimiter.DELIMITER)
        val workArr = works.split(DbDelimiter.DELIMITER)
        val restArr = rests.split(DbDelimiter.DELIMITER)

        for (i in 0 until nameArr.size)
            result.add(Round(nameArr[i], workArr[i].toInt(), restArr[i].toInt()))

        return result
    }

    fun getDefaultRoundList(): MutableList<Round> {
        val list: MutableList<Round> = mutableListOf()
        for (i in 0 until 8)
            list.add(Round("Work", 20, 10))
        return list
    }

}
