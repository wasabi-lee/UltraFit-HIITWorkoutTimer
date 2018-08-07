package io.incepted.ultrafittimer.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import io.incepted.ultrafittimer.db.dao.PresetDao
import io.incepted.ultrafittimer.db.dao.TimerSettingDao
import io.incepted.ultrafittimer.db.dao.WorkoutHistoryDao
import io.incepted.ultrafittimer.db.model.Preset
import io.incepted.ultrafittimer.db.model.TimerSetting
import io.incepted.ultrafittimer.db.model.WorkoutHistory

@Database(
        entities = [
        Preset::class,
        TimerSetting::class,
        WorkoutHistory::class],
        version = 1,
        exportSchema = false
)

abstract class AppDatabase: RoomDatabase() {

    abstract fun presetDao(): PresetDao

    abstract fun timerSettingDao(): TimerSettingDao

    abstract fun workoutHistoryDao(): WorkoutHistoryDao
}