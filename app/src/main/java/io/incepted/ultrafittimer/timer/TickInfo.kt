package io.incepted.ultrafittimer.timer

import timber.log.Timber

/**
 * A wrapper class that contains the round info that needs to be updated on the UI for every tick.
 */

class TickInfo(val session: Int, val workoutName: String,
               val remianingSecs: Long, val roundTotalSecs: Long, val roundCount: Int,
               val totalRounds: Int, val switched: Boolean) {

    constructor(session: Int, remainingSecs: Long, roundTotalSecs: Long) :
            this(session, "", remainingSecs, roundTotalSecs,
                    0, 0, false)

    constructor(session: Int, roundTotalSecs: Long, switched: Boolean) :
            this(session, "", 0, roundTotalSecs,
                    0, 0, switched)

    constructor(session: Int, roundTotalSecs: Long) :
            this(session, "", 0, roundTotalSecs,
                    0, 0, false)

    override fun toString(): String {
        return """Session: $session
            name: $workoutName
            remainingSec: $remianingSecs
            roundTotal: $roundTotalSecs
            Round: $roundCount
        """.trimMargin()
    }
}