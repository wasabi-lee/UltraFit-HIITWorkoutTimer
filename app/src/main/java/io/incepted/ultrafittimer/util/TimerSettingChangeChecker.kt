package io.incepted.ultrafittimer.util

import io.incepted.ultrafittimer.db.tempmodel.Round

object TimerSettingChangeChecker {

    fun timerSettingChanged(originalWarmup: Int, resultWarmup: Int,
                            originalCooldown: Int, resultCooldown: Int,
                            originalRound: List<Round>, resultRound: List<Round>) : Boolean {

        return roundChanged(originalRound, resultRound) &&
                originalWarmup == resultWarmup &&
                originalCooldown == resultCooldown
    }

    fun roundChanged(original: List<Round>, result: List<Round>): Boolean {

        val originalStrs = joinListToString(original)
        val resultStrs = joinListToString(result)

        return originalStrs[0] == resultStrs[0] &&
                originalStrs[1] == resultStrs[1] &&
                originalStrs[2] == resultStrs[2]
    }


    fun joinListToString(l: List<Round>): Array<String> {

        val res = Array<String>(3) { "" }
        val names = l.joinToString(DbDelimiter.DELIMITER) { it.workoutName }
        val works = l.joinToString(DbDelimiter.DELIMITER) { it.workSeconds.toString() }
        val rests = l.joinToString(DbDelimiter.DELIMITER) { it.restSeconds.toString() }

        res[0] = names
        res[1] = works
        res[2] = rests

        return res
    }
}