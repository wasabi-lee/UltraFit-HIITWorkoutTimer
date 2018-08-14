package io.incepted.ultrafittimer.viewmodel

import android.app.Activity.RESULT_OK
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import io.incepted.ultrafittimer.R
import io.incepted.ultrafittimer.activity.CustomizeActivity
import io.incepted.ultrafittimer.activity.MainActivity
import io.incepted.ultrafittimer.db.DbRepository
import io.incepted.ultrafittimer.db.model.Preset
import io.incepted.ultrafittimer.db.model.TimerSetting
import io.incepted.ultrafittimer.db.model.TimerSettingObservable
import io.incepted.ultrafittimer.db.source.LocalDataSource
import io.incepted.ultrafittimer.db.tempmodel.Round
import io.incepted.ultrafittimer.util.DbDelimiter
import io.incepted.ultrafittimer.util.RoundUtil
import io.incepted.ultrafittimer.util.WorkoutSession
import timber.log.Timber
import javax.inject.Inject

class MainViewModel @Inject constructor(val appContext: Application, val repository: DbRepository)
    : AndroidViewModel(appContext), LocalDataSource.OnTimerSavedListener, LocalDataSource.OnPresetSavedListener,
        LocalDataSource.OnPresetLoadedListener, LocalDataSource.OnTimerLoadedListener {

    @Inject
    lateinit var sharedPref: SharedPreferences

    // Activity transition LiveData
    val toPresetActivity: MutableLiveData<Boolean> = MutableLiveData()

    val toCustomizeActivity: MutableLiveData<ArrayList<String>> = MutableLiveData()

    val toSettings: MutableLiveData<Boolean> = MutableLiveData()

    val toTimerActivity: MutableLiveData<Int> = MutableLiveData()

    val snackbarTextRes: MutableLiveData<Int> = MutableLiveData()

    val focusListener: View.OnFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
        handleFocusChange(v, hasFocus)
    }

    var editMode = ObservableBoolean(false)

    var fromPreset: Boolean = false

    var fromTemp: Boolean = false

    var presetName: String = "Untitled"


    lateinit var timerSetting: TimerSetting

    var timerSettingObservable = ObservableField<TimerSettingObservable>()

    var offset: Int = 1

    var editPresetId = -1L


    fun start(editMode: Boolean, editPresetId: Long) {
        this.editMode.set(editMode)
        this.editPresetId = editPresetId
        this.offset = sharedPref.getString("pref_key_increment_seconds", "1")?.toInt() ?: offset
        initializeRounds()
    }

    private fun initializeRounds() {
        if (editMode.get()) {
            loadPreset(editPresetId)
            fromPreset = true

        } else {
            val presetId: Long = sharedPref.getLong("pref_key_last_used_preset_id", -1)
            val tempTimerId: Long = sharedPref.getLong("pref_key_last_used_timer_id", -1)

            if (presetId != -1L) {
                loadPreset(presetId)
                fromPreset = true
            } else if (tempTimerId != -1L) {
                loadTimer(tempTimerId)
                fromTemp = true
            } else {
                timerSetting = TimerSetting(180, 180) // default value
                timerSettingObservable.set(TimerSettingObservable(timerSetting))
                timerSettingObservable.notifyChange()
            }
        }

    }


    fun onTimerStartClicked() {
        val to = timerSettingObservable.get()
        if (to != null) {
            to.finalizeDetail()
            if (!fromPreset && !fromTemp) {
                // just save this timer and move on
                repository.saveTimer(to.getFinalSetting(), this)
            } else {
                if (to.checkIfEdited()) {
                    repository.saveTimer(to.getFinalSetting(), this)
                } else {
                    if (fromPreset) {
                        // keep this preset id and pass it as an extra
                    } else {
                        // keep this timer id and pass it as an extra
                    }
                }
            }
        }
    }


    private fun loadPreset(presetId: Long) {
        repository.getPresetById(presetId, this)
    }

    private fun loadTimer(timerId: Long) {
        repository.getTimerById(timerId, this)
    }


    fun saveThisAsPreset(presetName: String) {
        val to = timerSettingObservable.get()
        if (to != null) {
            to.finalizeDetail()
            this.presetName = presetName

            repository.saveTimer(to.getFinalSetting(), this)
        }
        // reinitialize the entries with the retrieved values
        fromPreset = true
    }

    fun openPresetActivity() {
        toPresetActivity.value = true
        toPresetActivity.value = false
    }

    fun openCustomizeActivity() {
        // sending the delimiter separated workout details as extra
        val to = timerSettingObservable.get()
        if (to != null) {
            to.finalizeDetail()
            toCustomizeActivity.value = arrayListOf(to.finalWorkName,
                    to.finalWorks,
                    to.finalRests)
            toCustomizeActivity.value = null
        }
    }

    fun openSettings() {
        toSettings.value = true
        toSettings.value = false
    }

    fun toTimerActivity(timerId: Int) {
        toTimerActivity.value = timerId
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (data == null) return

        if (requestCode == MainActivity.RC_CUSTOMIZED && resultCode == RESULT_OK) {
            val result: Bundle? = data.extras

            val names = result?.getString(CustomizeActivity.RESULT_KEY_WORKOUT_NAMES)
                    ?: timerSetting.roundNames
            val works = result?.getString(CustomizeActivity.RESULT_KEY_WORKOUT_WORKS)
                    ?: timerSetting.workSeconds
            val rests = result?.getString(CustomizeActivity.RESULT_KEY_WORKOUT_RESTS)
                    ?: timerSetting.restSeconds
            val customized = result?.getBoolean(CustomizeActivity.RESULT_KEY_WORKOUT_CUSTOMIZED)
                    ?: false

                if (customized)
                    timerSettingObservable.get()?.isCustomizedObservable?.set(true)

                timerSettingObservable.get()?.mRounds = RoundUtil.getRoundList(names, works, rests)
            }

        }


    // ------------------ EditText value change handling -----------------

    /**
     * Called when the focus is changed in any EditText (when the user finished editing)
     */
    private fun handleFocusChange(v: View, hasFocus: Boolean) {
        // Trimming user input after the focus is gone
        if (!hasFocus) {
            val session: Int = WorkoutSession.getSessionById(v.id)
            timerSettingObservable.get()?.handleChange(session, 0)
        }
    }

    /**
     * Called when the +/- button is clicked to increment/decrement the time or the number of rounds.
     * Add/subtract the offset to the current input and trim to user readable string.
     */
    fun adjustInput(clickedId: Int, increment: Boolean) {
        val session: Int = WorkoutSession.getSessionById(clickedId)
        offset = if (session == WorkoutSession.ROUND) 1 else offset
        timerSettingObservable.get()?.handleChange(session, offset * if (increment) 1 else -1)
        timerSettingObservable.get()?.calculateTotal()
    }


    // ------------------ Callbacks -----------------

    override fun onTimerSaved(id: Long) {
        val newPreset = Preset(null, false, presetName, id)
        repository.savePreset(newPreset, this)
    }

    override fun onTimerSaveNotAvailable() {
        snackbarTextRes.value = R.string.error_unexpected
    }

    override fun onPresetSaved() {
        snackbarTextRes.value = R.string.preset_save_successful
    }

    override fun onPresetSaveNotAvailable() {
        snackbarTextRes.value = R.string.error_unexpected
    }

    override fun onPresetLoaded(preset: Preset) {
        loadTimer(preset.timerSettingId)
    }

    override fun onPresetNotAvailable() {
        snackbarTextRes.value = R.string.error_unexpected
    }

    override fun onTimerLoaded(timer: TimerSetting) {
        timerSettingObservable.set(TimerSettingObservable(timer))
        timerSettingObservable.notifyChange()
    }

    override fun onTimerNotAvailable() {
        snackbarTextRes.value = R.string.error_unexpected
    }


}