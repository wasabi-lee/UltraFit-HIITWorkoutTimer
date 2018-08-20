package io.incepted.ultrafittimer.timer

/**
 * A wrapper class that contains the round info that needs to be updated on the UI for every tick.
 */

class RoundInfo(val session: Int, val workoutName: String, val remianingSecs: Long, val roundCount: Int) {

    override fun toString(): String {
        return """Session: $session
            name: $workoutName
            remainingSec: $remianingSecs
            Round: $roundCount
        """.trimMargin()
    }
}