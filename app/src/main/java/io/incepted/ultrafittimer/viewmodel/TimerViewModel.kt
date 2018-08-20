package io.incepted.ultrafittimer.viewmodel

import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import io.incepted.ultrafittimer.R
import io.incepted.ultrafittimer.db.DbRepository
import io.incepted.ultrafittimer.db.model.Preset
import io.incepted.ultrafittimer.db.model.TimerSetting
import io.incepted.ultrafittimer.db.source.LocalDataSource
import io.incepted.ultrafittimer.timer.TimerService
import io.incepted.ultrafittimer.util.SingleLiveEvent
import io.incepted.ultrafittimer.util.TimerUtil
import timber.log.Timber
import javax.inject.Inject

class TimerViewModel @Inject constructor(val appContext: Application, val repository: DbRepository)
    : AndroidViewModel(appContext), LocalDataSource.OnTimerLoadedListener, LocalDataSource.OnPresetLoadedListener {


    val onTimerReady = SingleLiveEvent<Bundle>()

    val snackbarResource = SingleLiveEvent<Int>()

    lateinit var preset: Preset

    lateinit var timer: TimerSetting

    val resumePause = SingleLiveEvent<Void>()

    val terminateTimer = SingleLiveEvent<Void>()


    // ----------------------- UI field -------------------------

    val session = ObservableField<Int>()

    val workoutName = ObservableField<String>()

    val remainingTime = ObservableField<String>()

    val roundCount = ObservableField<String>()


    fun start(isPreset: Boolean, targetId: Long) {
        Timber.d("Started")

        loadTimer(isPreset, targetId)
    }


    private fun loadTimer(isPreset: Boolean, targetId: Long) {
        if (isPreset) repository.getPresetById(targetId, this)
        else repository.getTimerById(targetId, this)
    }


    private fun createTimerInfoBundle(timer: TimerSetting): Bundle {
        val timerInfo = Bundle()
        timerInfo.putInt(TimerService.BUNDLE_KEY_WARMUP_TIME, timer.warmupSeconds)
        timerInfo.putString(TimerService.BUNDLE_KEY_WORK_NAMES, timer.roundNames)
        timerInfo.putString(TimerService.BUNDLE_KEY_WORK_TIME, timer.workSeconds)
        timerInfo.putString(TimerService.BUNDLE_KEY_REST_TIME, timer.restSeconds)
        timerInfo.putInt(TimerService.BUNDLE_KEY_COOLDOWN_TIME, timer.cooldownSeconds)
        return timerInfo
    }



    // ------------------------------------------ User Interaction -------------------------------------


    fun updateTime(intent: Intent?) {
        if (intent == null) return
        val sess = intent.getIntExtra(TimerService.BR_EXTRA_KEY_SESSION_SESSION, 0)
        val name = intent.getStringExtra(TimerService.BR_EXTRA_KEY_SESSION_NAME) ?: ""
        val remaining = intent.getLongExtra(TimerService.BR_EXTRA_KEY_SESSION_REMAINING_SECS, 0L)
        val round = intent.getIntExtra(TimerService.BR_EXTRA_KEY_SESSION_ROUND_COUNT, 0)

        workoutName.set(name)
        remainingTime.set(TimerUtil.secondsToTimeString(remaining.toInt()))
        roundCount.set("Round $round")

    }

    fun terminateTimer() {
        terminateTimer.value = null
    }


    fun resumePauseTimer() {
        resumePause.value = null
    }



    // ------------------------------------------ Callbacks -----------------------------------------

    override fun onTimerLoaded(timer: TimerSetting) {
        this.timer = timer
        onTimerReady.value = createTimerInfoBundle(timer)
    }


    override fun onTimerNotAvailable() {
        snackbarResource.value = R.string.error_unexpected
    }


    override fun onPresetLoaded(preset: Preset) {
        this.preset = preset
        repository.getTimerById(preset.timerSettingId, this)
    }


    override fun onPresetNotAvailable() {
        snackbarResource.value = R.string.error_unexpected
    }


}