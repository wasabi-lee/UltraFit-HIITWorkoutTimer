package io.incepted.ultrafittimer.db.source

import io.incepted.ultrafittimer.db.model.Preset
import io.incepted.ultrafittimer.db.model.TimerSetting
import io.incepted.ultrafittimer.db.model.WorkoutHistory

interface LocalDataSource {


    // ----------------- Preset callbacks ----------------

    interface OnPresetsLoadedListener {
        fun onPresetsLoaded(presets: List<Preset>)
        fun onPresetsNotAvailable()
    }

    interface OnPresetLoadedListener {
        fun onPresetLoaded(preset: Preset)
        fun onPresetNotAvailable()
    }

    interface OnTimersForPresetsLoadedListener {
        fun onPresetTimerNotAvailable()
        fun onPresetTimerLoadCompleted(presets: List<Preset>)
    }

    interface OnPresetSavedListener {
        fun onPresetSaved(presetId: Long)
        fun onPresetSaveNotAvailable()
    }

    interface OnPresetUpdateListener {
        fun onPresetUpdated()
        fun onPresetUpdateNotAvailable()
    }

    interface OnPresetDeletedListener {
        fun onPresetDeleted()
        fun onPresetDeletionNotAvailable();
    }

    interface OnAllPresetDeletedListener {
        fun onAllPresetsDeleted()
        fun onAllPresetDeletionNotAvailable()
    }



    // ----------------- TimerSetting callbacks ----------------

    interface OnTimerLoadedListener {
        fun onTimerLoaded(timer: TimerSetting)
        fun onTimerNotAvailable()
    }

    interface OnTimerSavedListener {
        fun onTimerSaved(id: Long)
        fun onTimerSaveNotAvailable()
    }

    interface OnTimerUpdateListener {
        fun onTimerUpdated()
        fun onTimerUpdateNotAvailable()
    }


    // ----------------- WorkoutHistory callbacks ----------------


    interface OnHistoriesLoadedListener {
        fun onHistoriesLoaded(histories: List<WorkoutHistory>)
        fun onHistoriesNotAvailable()
    }

    interface OnHistoryLoadedListener {
        fun onHistoryLoaded(history: WorkoutHistory)
        fun onHistoryNotAvailable()
    }

    interface OnHistorySavedListener {
        fun onHistorySaved()
        fun onHistorySaveNotAvailable()
    }

    interface OnHistoryDeletedListener {
        fun onHistoryDeleted()
        fun onHistoryDeletionNotAvailable();
    }

    interface OnAllHistoryDeletedListener {
        fun onAllHistoriesDeleted()
        fun onAllHistoryDeletionNotAvailable()
    }


    // ----------------- abstract methods --------------------

    fun getPresets(callback: OnPresetsLoadedListener)

    fun getPresetById(presetId: Long, callback: OnPresetLoadedListener)

    fun savePreset(newPreset: Preset, callback: OnPresetSavedListener)

    fun updatePreset(updated: Preset, callback:OnPresetUpdateListener)

    fun deletePreset(presetId: Long, callback:OnPresetDeletedListener)

    fun deleteAllPresets(callback: OnAllPresetDeletedListener)




    fun getTimerById(timerId: Long, callback: OnTimerLoadedListener)

    fun saveTimer(newTimer: TimerSetting, callback: OnTimerSavedListener)

    fun updateTimer(updated: TimerSetting, callback:OnTimerUpdateListener)



    fun getWorkoutHistories(callback: OnHistoryLoadedListener)

    fun getWorkoutHistoryById(historyId: Int, callback: OnHistoryLoadedListener)

    fun saveWorkoutHistory(newHistory: WorkoutHistory, callback: OnHistorySavedListener)

    fun deleteWorkoutHistory(historyId: Int, callback:OnHistoryDeletedListener)

    fun deleteAllWorkoutHistories(callback: OnAllHistoryDeletedListener)




}