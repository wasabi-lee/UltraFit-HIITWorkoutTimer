package io.incepted.ultrafittimer.db

import io.incepted.ultrafittimer.db.dao.PresetDao
import io.incepted.ultrafittimer.db.dao.TimerSettingDao
import io.incepted.ultrafittimer.db.dao.WorkoutHistoryDao
import io.incepted.ultrafittimer.db.model.Preset
import io.incepted.ultrafittimer.db.source.LocalDataSource
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class DbRepository @Inject constructor(
        private val db: AppDatabase,
        private val presetDao: PresetDao,
        private val timerSettingDao: TimerSettingDao,
        private val workoutHistoryDao: WorkoutHistoryDao
) : LocalDataSource {


    // ---------- Preset operations ----------

    override fun getPresets(callback: LocalDataSource.OnPresetsLoadedListener) {
        Timber.d("checking injection... \n" +
                "db: ${db.toString()} \n" +
                "presetDao: ${presetDao.toString()} \n" +
                "timerSettingDao: ${timerSettingDao.toString()} \n" +
                "workoutHistoryDao: ${workoutHistoryDao.toString()} \n")
    }

    override fun getPresetById(presetId: Int, callback: LocalDataSource.OnPresetLoadedListener) {

    }

    override fun savePreset(newPreset: Preset, callback: LocalDataSource.OnPresetSavedListener) {

    }

    override fun updatePreset(updated: Preset, callback: LocalDataSource.OnPresetUpdateListener) {

    }

    override fun deletePreset(presetId: Int, callback: LocalDataSource.OnPresetDeletedListener) {

    }

    override fun deleteAllPresets(callback: LocalDataSource.OnAllPresetDeletedListener) {

    }


    // ---------- TimerSetting operations ----------


    override fun getTimerById(timerId: Int, callback: LocalDataSource.OnTimerLoadedListener) {

    }

    override fun saveTimer(newTimer: Preset, callback: LocalDataSource.OnTimerSavedListener) {

    }

    override fun updateTimer(updated: Preset, callback: LocalDataSource.OnTimerUpdateListener) {

    }


    // ---------- WorkoutHistory operations ----------

    override fun getWorkoutHistories(callback: LocalDataSource.OnHistoryLoadedListener) {

    }

    override fun getWorkoutHistoryById(historyId: Int, callback: LocalDataSource.OnHistoryLoadedListener) {

    }

    override fun saveWorkoutHistory(newHistory: Preset, callback: LocalDataSource.OnHistorySavedListener) {

    }

    override fun deleteWorkoutHistory(historyId: Int, callback: LocalDataSource.OnHistoryDeletedListener) {

    }

    override fun deleteAllWorkoutHistories(callback: LocalDataSource.OnAllHistoryDeletedListener) {

    }
}