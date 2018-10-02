package io.incepted.ultrafittimer.viewmodel

import android.app.Activity.RESULT_OK
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import io.incepted.ultrafittimer.R
import io.incepted.ultrafittimer.activity.CustomizeActivity
import io.incepted.ultrafittimer.activity.MainActivity
import io.incepted.ultrafittimer.activity.TimerActivity
import io.incepted.ultrafittimer.db.DbRepository
import io.incepted.ultrafittimer.db.model.Preset
import io.incepted.ultrafittimer.db.model.TimerSetting
import io.incepted.ultrafittimer.db.model.TimerSettingObservable
import io.incepted.ultrafittimer.db.source.LocalDataSource
import io.incepted.ultrafittimer.timer.TimerService
import io.incepted.ultrafittimer.util.RoundUtil
import io.incepted.ultrafittimer.util.SingleLiveEvent
import io.incepted.ultrafittimer.util.WorkoutSession
import javax.inject.Inject

class MainViewModel @Inject constructor(val appContext: Application, val repository: DbRepository)
    : AndroidViewModel(appContext), LocalDataSource.OnTimerSavedListener, LocalDataSource.OnPresetSavedListener,
        LocalDataSource.OnPresetLoadedListener, LocalDataSource.OnTimerLoadedListener,
        LocalDataSource.OnPresetUpdateListener {

    @Inject
    lateinit var sharedPref: SharedPreferences

    // Activity transition LiveData
    val toPresetActivity: SingleLiveEvent<Void> = SingleLiveEvent()
    val toCustomizeActivity: SingleLiveEvent<ArrayList<String>> = SingleLiveEvent()
    val toSettings: SingleLiveEvent<Void> = SingleLiveEvent()
    val toTimerActivity: SingleLiveEvent<Bundle> = SingleLiveEvent()
    val finishActivity: SingleLiveEvent<Void> = SingleLiveEvent()

    // UI listeners & events
    var presetName = ObservableField<String>("Untitled")
    val snackbarTextRes: SingleLiveEvent<Int> = SingleLiveEvent()
    val focusListener: View.OnFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
        handleFocusChange(v, hasFocus)
    }

    // Timer status flags
    var editMode = ObservableBoolean(false) // Currently modifying an existing preset
    var fromPreset: Boolean = false // This timer is from an existing preset
    var fromTemp: Boolean = false // This timer is from a temporarily saved timer
    var presetSaveInProgress: Boolean = false // Preset is being saved
    var lastPresetId = -1L
    var lastTimerId = -1L


    // Models
    lateinit var preset: Preset
    lateinit var timer: TimerSetting
    var timerObsvb = ObservableField<TimerSettingObservable>()

    var offset: Int = 1
    private var editPresetId = -1L // The id of the preset we're currently modifying


    // ------------------------------------ Initialization --------------------------------------

    fun start(editMode: Boolean, editPresetId: Long) {

        if (checkIfTimerServiceRunning())
            toTimerActivity.value = null

        this.editMode.set(editMode)
        this.editPresetId = editPresetId


        initializeRounds()
    }


    private fun checkIfTimerServiceRunning(): Boolean {
        return TimerService.SERVICE_STARTED
    }


    private fun initializeRounds() {
        when (editMode.get()) {
            true -> // When this is in preset edit mode
                initWithPreset(editPresetId)
            else -> {
                // When this is default mode
                when {
                    lastPresetId != -1L -> initWithPreset(lastPresetId)
                    lastTimerId != -1L -> initWithExistingTimerSetting(lastTimerId)
                    else -> initNewTimer()
                }
            }
        }
    }


    private fun initWithPreset(presetId: Long) {
        loadPreset(presetId)
        fromPreset = true
    }


    private fun initWithExistingTimerSetting(timerId: Long) {
        loadTimer(timerId)
        fromTemp = true
    }


    private fun initNewTimer() {
        timer = TimerSetting() // default value
        timerObsvb.set(TimerSettingObservable(timer))
        timerObsvb.notifyChange()
    }


    public fun loadPreset(presetId: Long) {
        repository.getPresetById(presetId, this)
    }


    public fun loadTimer(timerId: Long) {
        repository.getTimerById(timerId, this)
    }


    fun saveThisAsPreset(presetName: String) {
        this.presetName.set(presetName)
        saveThisAsPreset()
    }


    // ---------------------------------- User interaction ----------------------------------


    fun onTimerStartClicked() {
        val to = timerObsvb.get() ?: return

        // Finalize the setting before moving to the next activity
        to.finalizeDetail()

        // just save this timer and go to the next activity
        if (!fromPreset && !fromTemp)
            repository.saveTimer(to.getFinalSetting(), this)
        else {
            // When anything is edited -> Treat it as a new timer and save it. Pass the id of it as an extra
            // Nothing has been changed, but the timer is from preset -> Pass the preset id as an extra
            // Nothing has been changed, but the timer is from temporarily saved timer -> Pass the timer id as an extra
            when {
                to.checkIfEdited() -> repository.saveTimer(to.getFinalSetting(), this)
                fromPreset -> toTimerActivity(preset.id ?: return, true)
                else -> toTimerActivity(timer.id ?: return, false)
            }
        }
    }


    fun saveThisAsPreset() {
        presetSaveInProgress = true

        val to = timerObsvb.get() ?: return
        to.finalizeDetail()
        repository.saveTimer(to.getFinalSetting(), this)
    }


    fun resetCurrentTimer() {
        initNewTimer()
        snackbarTextRes.value = R.string.loaded_default_timer
    }


    // ---------------------------------- Activity transition ----------------------------------


    fun openPresetActivity() {
        toPresetActivity.value = null
    }


    fun openCustomizeActivity() {
        // sending the delimiter separated workout details as extra
        val to = timerObsvb.get()
        if (to != null) {
            to.finalizeDetail()
            toCustomizeActivity.value = arrayListOf(to.finalWorkName,
                    to.finalWorks,
                    to.finalRests)
        }
    }


    fun openSettings() {
        toSettings.value = null
    }


    private fun toTimerActivity(id: Long, fromPreset: Boolean) {
        val bundle = Bundle()
        bundle.putBoolean(TimerActivity.EXTRA_KEY_FROM_PRESET, fromPreset)
        bundle.putLong(TimerActivity.EXTRA_KEY_ID, id)
        toTimerActivity.value = bundle
    }


    private fun finishActivity() {
        finishActivity.value = null
    }


    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        // Handle data from CustomizeActivity. Parsing new data into timerObservable.

        if (requestCode == MainActivity.RC_CUSTOMIZED && resultCode == RESULT_OK) {
            val result: Bundle = data?.extras ?: return
            val names = result.getString(CustomizeActivity.RESULT_KEY_WORKOUT_NAMES)
                    ?: timer.roundNames
            val works = result.getString(CustomizeActivity.RESULT_KEY_WORKOUT_WORKS)
                    ?: timer.workSeconds
            val rests = result.getString(CustomizeActivity.RESULT_KEY_WORKOUT_RESTS)
                    ?: timer.restSeconds
            val customized = result.getBoolean(CustomizeActivity.RESULT_KEY_WORKOUT_CUSTOMIZED)

            if (customized)
                timerObsvb.get()?.isCustomizedObservable?.set(true)

            timerObsvb.get()?.mRounds = RoundUtil.getRoundList(names, works, rests)
            timerObsvb.get()?.calculateTotal()
        }
    }


    // ------------------------------- EditText value change handling -------------------------------


    // Called when the focus is changed in any EditText (when the user finished editing)

    private fun handleFocusChange(v: View, hasFocus: Boolean) {
        // Trimming user input after the focus is gone
        if (!hasFocus) {
            val session: Int = WorkoutSession.getSessionById(v.id)
            timerObsvb.get()?.handleChange(session, 0)
        }
    }


    // Called when the +/- button is clicked to increment/decrement the time or the number of rounds.

    fun adjustInput(clickedId: Int, increment: Boolean) {
        val session: Int = WorkoutSession.getSessionById(clickedId)
        val curOffset = if (session == WorkoutSession.ROUND) 1 else offset
        timerObsvb.get()?.handleChange(session, curOffset * if (increment) 1 else -1)
    }


    // ----------------------------------------- Callbacks ------------------------------------------

    override fun onTimerSaved(id: Long) {

        val name = presetName.get() ?: "Untitled"

        if (editMode.get()) {
            // Update existing preset
            preset.timerSettingId = id
            preset.name = name
            repository.updatePreset(preset, this)
        } else {
            // Save a new preset
            if (presetSaveInProgress) {
                val newPreset = Preset(null, false, name, id)
                repository.savePreset(newPreset, this)
            } else {
                toTimerActivity(id, false)
            }
        }

    }

    override fun onTimerSaveNotAvailable() {
        snackbarTextRes.value = R.string.error_unexpected
    }


    override fun onPresetSaved(presetId: Long) {
        presetSaveInProgress = false
        snackbarTextRes.value = R.string.preset_save_successful

        loadPreset(presetId)
    }


    override fun onPresetSaveNotAvailable() {
        snackbarTextRes.value = R.string.error_unexpected
    }


    override fun onPresetLoaded(preset: Preset) {
        this.preset = preset
        this.presetName.set(preset.name)

        loadTimer(preset.timerSettingId)
    }


    override fun onPresetNotAvailable() {
        snackbarTextRes.value = R.string.error_unexpected
    }


    override fun onTimerLoaded(timer: TimerSetting) {
        this.timer = timer
        timerObsvb.set(TimerSettingObservable(timer))
        timerObsvb.notifyChange()
    }


    override fun onTimerNotAvailable() {
        snackbarTextRes.value = R.string.error_unexpected
    }


    override fun onPresetUpdated() {
        presetSaveInProgress = false
        finishActivity()
    }


    override fun onPresetUpdateNotAvailable() {
        presetSaveInProgress = false
        snackbarTextRes.value = R.string.error_unexpected
    }


}