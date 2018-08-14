package io.incepted.ultrafittimer.db.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import io.incepted.ultrafittimer.util.DbDelimiter
import timber.log.Timber

@Entity(tableName = "preset")
class Preset(@PrimaryKey(autoGenerate = true) @ColumnInfo(name = "_id") var id: Long?,
             @ColumnInfo(name = "bookmarked") var bookmarked: Boolean,
             @ColumnInfo(name = "name") var name: String,
             @ColumnInfo(name = "timer_setting_id") var timerSettingId: Long) {

    @Ignore
    var timerSetting: TimerSetting = TimerSetting(0, 0)
    set(value) {
        field = value
        printThis()
    }


    fun printThis() {
        Timber.d("$timerSetting")
    }



}