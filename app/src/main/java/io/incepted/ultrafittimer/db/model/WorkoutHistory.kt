package io.incepted.ultrafittimer.db.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "workout_history")
class WorkoutHistory(@PrimaryKey(autoGenerate = true) @ColumnInfo(name = "_id") var id: Long?,
                     @ColumnInfo(name = "timestamp") var timestamp: Long,
                     @ColumnInfo(name = "preset_id") var presetId: Long?,
                     @ColumnInfo(name = "timer_id") var timer_id: Long?,
                     @ColumnInfo(name = "stopped_round") var stoppedRound: Int?,
                     @ColumnInfo(name = "stopped_second") var stoppedSecond: Int?,
                     @ColumnInfo(name = "stopped_session") var stoppedSession: Int?,
                     @ColumnInfo(name = "completed") var completed: Boolean) {


    // Constructor for the completed workout
    @Ignore
    constructor(timestamp: Long, presetId: Long?, timer_id: Long?) :
            this(null, timestamp, presetId, timer_id,
                    null, null, null,
                    true)


    // Constructor for the unfinished workout
    @Ignore
    constructor(timestamp: Long, presetId: Long?, timer_id: Long?,
                stoppedRound: Int?, stoppedSecond: Int?, stoppedSession: Int?) :
            this(null, timestamp, presetId, timer_id,
                    stoppedRound, stoppedSecond, stoppedSession,
                    false)
}