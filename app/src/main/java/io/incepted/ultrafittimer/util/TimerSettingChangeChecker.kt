package io.incepted.ultrafittimer.util

import io.incepted.ultrafittimer.db.tempmodel.Round

object TimerSettingChangeChecker {

    fun timerSettingChanged(originalWarmup: Int, resultWarmup: Int,
                            originalCooldown: Int, resultCooldown: Int,
                            originalRound: List<Round>, resultRound: List<Round>): Boolean {

        return roundChanged(originalRound, resultRound) &&
                originalWarmup == resultWarmup &&
                originalCooldown == resultCooldown
    }

    fun roundChanged(original: List<Round>, result: List<Round>): Boolean {

        if (original.size != result.size) return true

        for (i in 0 until original.size) {
            if ((original[i].workoutName != result[i].workoutName) ||
                    (original[i].workSeconds != result[i].workSeconds) ||
                    (original[i].restSeconds != result[i].restSeconds)) {
                return true
            } else continue
        }
        return false
    }

}