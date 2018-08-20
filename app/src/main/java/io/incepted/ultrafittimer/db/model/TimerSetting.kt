package io.incepted.ultrafittimer.db.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import io.incepted.ultrafittimer.db.tempmodel.Round
import io.incepted.ultrafittimer.util.RoundUtil

@Entity(tableName = "timer_setting")
class TimerSetting(@PrimaryKey(autoGenerate = true) @ColumnInfo(name = "_id") var id: Long?,
                   @ColumnInfo(name = "sec_warm_up") var warmupSeconds: Int,
                   @ColumnInfo(name = "round_names") var roundNames: String,
                   @ColumnInfo(name = "sec_work") var workSeconds: String,
                   @ColumnInfo(name = "sec_rest") var restSeconds: String,
                   @ColumnInfo(name = "sec_cool_down") var cooldownSeconds: Int,
                   @ColumnInfo(name = "customized") var customized: Boolean) {

    @Ignore
    lateinit var mRounds: MutableList<Round>

    // Called when no initial value is given
    @Ignore
    constructor(warmupSeconds: Int,
                cooldownSeconds: Int)
            : this(null, warmupSeconds, "-1", "-1",
            "-1", cooldownSeconds, false)

    init {
        val isNewTimer = roundNames == "-1" || workSeconds == "-1" || restSeconds == "-1"
        mRounds = if (isNewTimer) RoundUtil.getDefaultRoundList() else RoundUtil.getRoundList(this, false)
    }


    override fun toString(): String {
        return """warmupSeconds: $warmupSeconds
            workSeconds: $workSeconds
            restSeconds: $restSeconds
            roundCount: ${mRounds.size}
            cooldownSeconds: $cooldownSeconds"""
    }

}
