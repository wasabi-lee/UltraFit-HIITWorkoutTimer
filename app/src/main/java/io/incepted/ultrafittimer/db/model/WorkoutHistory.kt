package io.incepted.ultrafittimer.db.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "workout_history")
class WorkoutHistory(@PrimaryKey(autoGenerate = true) @ColumnInfo(name = "_id") val id: Long,
                     @ColumnInfo(name = "timestamp") var timestamp: Long,
                     @ColumnInfo(name = "preset_id") var presetId: Long?,
                     @ColumnInfo(name = "timer_id") var timer_id: Long?,
                     @ColumnInfo(name = "stopped_round") var stoppedRound: Int?,
                     @ColumnInfo(name = "stopped_second") var stoppedSecond: Int?,
                     @ColumnInfo(name = "stopped_session") var stoppedSession: Int?,
                     @ColumnInfo(name = "completed") var completed: Boolean) {

    object Session {
        const val SESSION_WARMUP = 0
        const val SESSION_WORK = 1
        const val SESSION_REST = 2
        const val SESSION_COOLDOWN = 3
    }

}