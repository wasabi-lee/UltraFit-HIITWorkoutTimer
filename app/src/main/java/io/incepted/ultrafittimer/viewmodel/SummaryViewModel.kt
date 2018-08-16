package io.incepted.ultrafittimer.viewmodel

import android.app.Application
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableLong
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import io.incepted.ultrafittimer.R
import io.incepted.ultrafittimer.db.DbRepository
import io.incepted.ultrafittimer.db.model.Preset
import io.incepted.ultrafittimer.db.model.TimerSetting
import io.incepted.ultrafittimer.db.model.WorkoutHistory
import io.incepted.ultrafittimer.db.source.LocalDataSource
import io.incepted.ultrafittimer.db.tempmodel.Round
import io.incepted.ultrafittimer.util.RoundUtil
import javax.inject.Inject

class SummaryViewModel @Inject constructor(val appContext: Application, val repository: DbRepository)
    : AndroidViewModel(appContext), LocalDataSource.OnPresetLoadedListener, LocalDataSource.OnTimerLoadedListener,
        LocalDataSource.OnHistoryLoadedListener {

    val showProgress = ObservableBoolean(false)

    val snackbarResource = MutableLiveData<Int>()

    val roundList = ObservableArrayList<Round>()

    var fromPreset = false

    var presetId = -1L

    var historyId = -1L

    var completed = ObservableBoolean(false)

    var timestamp = ObservableLong()

    fun start(isPreset: Boolean, targetId: Long) {
        this.fromPreset = isPreset
        if (isPreset) presetId = targetId else historyId = targetId

        loadData()

    }

    private fun loadData() {
        if (fromPreset) {
            repository.getPresetById(presetId, this)
        } else {
            repository.getWorkoutHistoryById(historyId, this)
        }
    }

    private fun initRounds(timer: TimerSetting) {
        // init rounds
        val rounds = RoundUtil.getRoundList(timer, true)
        populateList(rounds)
    }


    private fun populateList(l: List<Round>) {
        roundList.clear()
        roundList.addAll(l)
    }

    // ------------------------------- Callbacks ----------------------------------

    override fun onPresetLoaded(preset: Preset) {
        repository.getTimerById(preset.timerSettingId, this)
    }

    override fun onPresetNotAvailable() {
        snackbarResource.value = R.string.error_unexpected
    }

    override fun onTimerLoaded(timer: TimerSetting) {
        initRounds(timer)
    }

    override fun onTimerNotAvailable() {
        snackbarResource.value = R.string.error_unexpected
    }

    override fun onHistoryLoaded(history: WorkoutHistory) {
        val presetId = history.presetId
        val timerId = history.timer_id
        when {
            presetId != null -> repository.getPresetById(presetId, this)
            timerId != null -> repository.getTimerById(timerId, this)
            else -> snackbarResource.value = R.string.error_unexpected
        }
    }

    override fun onHistoryNotAvailable() {
        snackbarResource.value = R.string.error_unexpected
    }

}