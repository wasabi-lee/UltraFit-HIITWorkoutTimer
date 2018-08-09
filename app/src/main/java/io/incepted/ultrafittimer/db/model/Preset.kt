package io.incepted.ultrafittimer.db.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "preset")
class Preset(@PrimaryKey(autoGenerate = true) @ColumnInfo(name = "_id") var id: Long?,
             @ColumnInfo(name = "bookmarked") var bookmarked: Boolean,
             @ColumnInfo(name = "name") var name: String,
             @ColumnInfo(name = "timer_setting_id") var timerSettingId: Int) {

}