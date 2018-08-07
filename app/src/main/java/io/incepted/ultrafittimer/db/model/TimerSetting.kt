package io.incepted.ultrafittimer.db.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import io.incepted.ultrafittimer.db.tempmodel.Round
import io.incepted.ultrafittimer.util.DbDelimiter

@Entity(tableName = "timer_setting")
class TimerSetting(@PrimaryKey(autoGenerate = true) @ColumnInfo(name = "_id") val id: Long?,
                   @ColumnInfo(name = "sec_warm_up") var warmupSeconds: Int,
                   @ColumnInfo(name = "round_names") var roundNames: String,
                   @ColumnInfo(name = "sec_work") var workSeconds: String,
                   @ColumnInfo(name = "sec_rest") var restSeconds: String,
                   @ColumnInfo(name = "sec_cool_down") var cooldownSeconds: Int) {

    val mRounds: MutableList<Round> = mutableListOf()

    // Called when no initial value is given
    constructor(warmupSeconds: Int,
                cooldownSeconds: Int)
            : this(null, warmupSeconds, "-1", "-1",
            "-1", cooldownSeconds)

    init {
        if (roundNames == "-1" || workSeconds == "-1" || restSeconds == "-1") {
            // No initial value is given. Initialize the timer with default value
            initDefaultRounds()
        } else {
            // Initialize rounds with the given values
            initRounds()
        }
    }


    private fun initRounds() {
        val splitWork: List<String> = workSeconds.split(DbDelimiter.DELIMITER)
        val splitRest: List<String> = restSeconds.split(DbDelimiter.DELIMITER)
        val splitNames: List<String> = roundNames.split(DbDelimiter.DELIMITER)

        for (i in 0 until splitWork.size)
            mRounds.add(Round(splitNames[i], splitWork[i].toInt(), splitRest[i].toInt()))
    }

    private fun initDefaultRounds() {
        for (i in 0..7)
            mRounds.add(Round("Work", 20, 10))
    }

    fun parseRounds() {
        roundNames = mRounds.joinToString(DbDelimiter.DELIMITER) { it.workoutName }
        workSeconds = mRounds.joinToString(DbDelimiter.DELIMITER) { it.workSeconds.toString() }
        restSeconds = mRounds.joinToString(DbDelimiter.DELIMITER) { it.restSeconds.toString() }
    }

    fun removeRound(index: Int) {
        mRounds.removeAt(index)
    }

    fun insertRound(index: Int) {
        if (mRounds.size != 1)
            mRounds.add(index, mRounds[index - 1])
    }

    fun calculateTotal(): Int {
        val rounds: Int = mRounds.size
        val warmup: Int =warmupSeconds
        val work: Int = mRounds[0].workSeconds
        val rest: Int = mRounds[0].restSeconds
        val cooldown: Int = cooldownSeconds

        return warmup + ((work + rest) * rounds) + cooldown

    }

    override fun toString(): String {
        return """warmupSeconds: $warmupSeconds
            workSeconds: $workSeconds
            restSeconds: $restSeconds
            roundCount: ${mRounds.size}
            cooldownSeconds: $cooldownSeconds"""
    }

}
