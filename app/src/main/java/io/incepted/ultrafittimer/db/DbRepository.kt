package io.incepted.ultrafittimer.db

import io.incepted.ultrafittimer.db.dao.PresetDao
import io.incepted.ultrafittimer.db.dao.TimerSettingDao
import io.incepted.ultrafittimer.db.dao.WorkoutHistoryDao
import io.incepted.ultrafittimer.db.model.Preset
import io.incepted.ultrafittimer.db.model.TimerSetting
import io.incepted.ultrafittimer.db.model.WorkoutHistory
import io.incepted.ultrafittimer.db.source.LocalDataSource
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Action
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Consumer
import io.reactivex.functions.Function
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
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

        try {
            presetDao.getAllPresets()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                            onNext = { callback.onPresetsLoaded(it) },
                            onError = {
                                it.printStackTrace()
                                callback.onPresetsNotAvailable()
                            }
                    )
        } catch (e: Exception) {
            e.printStackTrace()
            callback.onPresetsNotAvailable()
        }

    }

    override fun getPresetById(presetId: Long, callback: LocalDataSource.OnPresetLoadedListener) {
        try {
            presetDao.getPresetById(presetId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                            onSuccess = { callback.onPresetLoaded(it) },
                            onError = {
                                it.printStackTrace()
                                callback.onPresetNotAvailable()
                            }
                    )
        } catch (e: Exception) {
            e.printStackTrace()
            callback.onPresetNotAvailable()
        }
    }

    override fun savePreset(newPreset: Preset, callback: LocalDataSource.OnPresetSavedListener) {
        try {
            Completable.fromAction { presetDao.insertPreset(newPreset) }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                            onComplete = { callback.onPresetSaved() },
                            onError = {
                                it.printStackTrace()
                                callback.onPresetSaveNotAvailable()
                            }
                    )
        } catch (e: Exception) {
            e.printStackTrace()
            callback.onPresetSaveNotAvailable()
        }
    }


    fun getTimerSettingsForPresets(presets: List<Preset>, callback: LocalDataSource.OnTimersForPresetsLoadedListener) {

        val res = mutableListOf<Preset>()

        try {
            Observable.fromIterable(presets)
                    .flatMap {
                        return@flatMap Observable.zip(Observable.just(it),
                                timerSettingDao.getTimerSettingById(it.timerSettingId).toObservable(),
                                BiFunction<Preset, TimerSetting, Preset> { preset, timer ->
                                    preset.timerSetting = timer
                                    preset
                                })
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                            onNext = { res.add(it) },
                            onComplete = { callback.onPresetTimerLoadCompleted(res) },
                            onError = {
                                it.printStackTrace()
                                callback.onPresetTimerNotAvailable()
                            }
                    )
        } catch (e: Exception) {
            e.printStackTrace()
            callback.onPresetTimerNotAvailable()
        }
    }


    override fun updatePreset(updated: Preset, callback: LocalDataSource.OnPresetUpdateListener) {
        try {
            Completable.fromAction { presetDao.updatePreset(updated) }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                            onComplete = { callback.onPresetUpdated() },
                            onError = {
                                it.printStackTrace()
                                callback.onPresetUpdateNotAvailable()
                            }
                    )
        } catch (e: Exception) {
            e.printStackTrace()
            callback.onPresetUpdateNotAvailable()
        }

    }

    override fun deletePreset(presetId: Long, callback: LocalDataSource.OnPresetDeletedListener) {
        try {
            Completable.fromAction { presetDao.deletePreset(presetId) }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                            onComplete = { callback.onPresetDeleted() },
                            onError = {
                                it.printStackTrace()
                                callback.onPresetDeletionNotAvailable()
                            }
                    )
        } catch (e: Exception) {
            e.printStackTrace()
            callback.onPresetDeletionNotAvailable()
        }
    }

    override fun deleteAllPresets(callback: LocalDataSource.OnAllPresetDeletedListener) {

    }


    // ---------- TimerSetting operations ----------


    override fun getTimerById(timerId: Long, callback: LocalDataSource.OnTimerLoadedListener) {
        try {
            timerSettingDao.getTimerSettingById(timerId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                            onSuccess = { callback.onTimerLoaded(it) },
                            onError = {
                                it.printStackTrace()
                                callback.onTimerNotAvailable()
                            }
                    )
        } catch (e: Exception) {
            e.printStackTrace()
            callback.onTimerNotAvailable()
        }
    }

    override fun saveTimer(newTimer: TimerSetting, callback: LocalDataSource.OnTimerSavedListener) {
        try {
            Single.fromCallable { timerSettingDao.insertTimerSetting(newTimer) }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                            onSuccess = { callback.onTimerSaved(it) },
                            onError = {
                                it.printStackTrace()
                                callback.onTimerSaveNotAvailable()
                            }
                    )
        } catch (e: Exception) {
            e.printStackTrace()
            callback.onTimerSaveNotAvailable()
        }
    }


    override fun updateTimer(updated: TimerSetting, callback: LocalDataSource.OnTimerUpdateListener) {

    }


    // ---------- WorkoutHistory operations ----------

    override fun getWorkoutHistories(callback: LocalDataSource.OnHistoryLoadedListener) {

    }

    override fun getWorkoutHistoryById(historyId: Int, callback: LocalDataSource.OnHistoryLoadedListener) {

    }

    override fun saveWorkoutHistory(newHistory: WorkoutHistory, callback: LocalDataSource.OnHistorySavedListener) {

    }

    override fun deleteWorkoutHistory(historyId: Int, callback: LocalDataSource.OnHistoryDeletedListener) {

    }

    override fun deleteAllWorkoutHistories(callback: LocalDataSource.OnAllHistoryDeletedListener) {

    }
}