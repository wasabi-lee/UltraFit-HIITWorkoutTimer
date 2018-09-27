package io.incepted.ultrafittimer.timer

/**
 * A wrapper class that contains the round info that needs to be updated on the UI for every tick.
 */

class TickInfo(val session: Int, val workoutName: String,
               val remianingSecs: Long, val roundTotalSecs: Long, val roundCount: Int,
               val totalRounds: Int, val switched: Boolean, val firstTick: Boolean) {

    constructor(session: Int, roundTotalSecs: Long, switched: Boolean, firstTick: Boolean) :
            this(session, "", 0, roundTotalSecs,
                    0, 0, switched, firstTick)

    constructor(session: Int, roundTotalSecs: Long) :
            this(session, "", 0, roundTotalSecs,
                    0, 0, false, false)

    constructor(session: Int, roundTotalSecs: Long, firstTick: Boolean) :
            this(session, "", 0, roundTotalSecs,
                    0, 0, false, firstTick)

    override fun toString(): String {
        return """Session: $session
            name: $workoutName
            remainingSec: $remianingSecs
            roundTotal: $roundTotalSecs
            Round: $roundCount
        """.trimMargin()
    }
}