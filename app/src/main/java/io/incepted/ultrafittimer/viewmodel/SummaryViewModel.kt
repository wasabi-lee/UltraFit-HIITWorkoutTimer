package io.incepted.ultrafittimer.viewmodel

import android.app.Application
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import io.incepted.ultrafittimer.R
import io.incepted.ultrafittimer.db.DbRepository
import io.incepted.ultrafittimer.db.model.Preset
import io.incepted.ultrafittimer.db.model.TimerSetting
import io.incepted.ultrafittimer.db.source.LocalDataSource
import timber.log.Timber
import javax.inject.Inject

class SummaryViewModel @Inject constructor(val appContext: Application, val repository: DbRepository)
    : AndroidViewModel(appContext), LocalDataSource.OnPresetLoadedListener, LocalDataSource.OnTimerLoadedListener {

    val showProgress = ObservableBoolean(false)

    private val snackbarResource = MutableLiveData<Int>()

    fun start() {
        Timber.d("Started!")
    }



    // ------------------------------- Callbacks ----------------------------------

    override fun onPresetLoaded(preset: Preset) {

    }

    override fun onPresetNotAvailable() {
        snackbarResource.value = R.string.error_unexpected
    }

    override fun onTimerLoaded(timer: TimerSetting) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onTimerNotAvailable() {
        snackbarResource.value = R.string.error_unexpected
    }

}