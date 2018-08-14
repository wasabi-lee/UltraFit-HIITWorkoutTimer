package io.incepted.ultrafittimer.db.dao

import android.arch.persistence.room.*
import io.incepted.ultrafittimer.db.model.TimerSetting
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable

@Dao
interface TimerSettingDao {

    @Query("SELECT * FROM timer_setting")
    fun getAllTimerSettings(): Flowable<List<TimerSetting>>

    @Query("SELECT * FROM timer_setting WHERE _id = (:timerSettingId)")
    fun getTimerSettingById(timerSettingId: Long) : Maybe<TimerSetting>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTimerSetting(newTimer: TimerSetting): Long

    @Update
    fun updateTimerSetting(updatedTimer: TimerSetting)

    @Query("DELETE FROM timer_setting WHERE _id = (:timerSettingId)")
    fun deleteTimerSetting(timerSettingId: Int)

    @Query("DELETE FROM timer_setting")
    fun deleteAllTimerSettings()

}