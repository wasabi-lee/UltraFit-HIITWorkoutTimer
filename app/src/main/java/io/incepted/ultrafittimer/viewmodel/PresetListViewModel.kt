package io.incepted.ultrafittimer.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.databinding.ObservableArrayList
import android.databinding.ObservableBoolean
import android.view.View
import io.incepted.ultrafittimer.R
import io.incepted.ultrafittimer.db.DbRepository
import io.incepted.ultrafittimer.db.model.Preset
import io.incepted.ultrafittimer.db.source.LocalDataSource
import timber.log.Timber
import javax.inject.Inject

class PresetListViewModel @Inject constructor(appContext: Application, val repository: DbRepository)
    : AndroidViewModel(appContext), LocalDataSource.OnPresetsLoadedListener, LocalDataSource.OnTimersForPresetsLoadedListener {

    val snackbarResource = MutableLiveData<Int>()

    val presetActionEvent = MutableLiveData<Long>()

    var presets = ObservableArrayList<Preset>()

    var presetsExist = ObservableBoolean(false)


    fun start() {
        loadPresets()
    }

    private fun loadPresets() {
        repository.getPresets(this)
    }

    private fun populateList(l: List<Preset>) {
        presets.clear()
        presets.addAll(l)
    }

    fun onLongClick(presetId: Long) {
        if (presetId == -1L) return
        presetActionEvent.value = presetId
    }


    // ------------------------------ Callbacks ----------------------------

    override fun onPresetsLoaded(presets: List<Preset>) {
        presetsExist.set(presets.isNotEmpty())
        populateList(presets)
        repository.getTimerSettingsForPresets(presets, this)
    }

    override fun onPresetsNotAvailable() {
        snackbarResource.value = R.string.error_unexpected
    }

    override fun onPresetTimerLoadCompleted(presets: List<Preset>) {
        presetsExist.set(presets.isNotEmpty())
        populateList(presets)
    }

    override fun onPresetTimerNotAvailable() {
        snackbarResource.value = R.string.error_unexpected
    }


}
