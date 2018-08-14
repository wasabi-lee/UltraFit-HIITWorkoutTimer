package io.incepted.ultrafittimer.db.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
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