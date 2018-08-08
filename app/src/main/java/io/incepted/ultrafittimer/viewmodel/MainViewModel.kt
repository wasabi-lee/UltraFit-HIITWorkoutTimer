package io.incepted.ultrafittimer.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.content.SharedPreferences
import android.view.View
import io.incepted.ultrafittimer.db.DbRepository
import io.incepted.ultrafittimer.db.model.Preset
import io.incepted.ultrafittimer.db.model.TimerSetting
import io.incepted.ultrafittimer.db.model.TimerSettingObservable
import io.incepted.ultrafittimer.db.source.LocalDataSource
import io.incepted.ultrafittimer.db.tempmodel.Round
import io.incepted.ultrafittimer.util.WorkoutSession
import timber.log.Timber
import javax.inject.Inject

class MainViewModel @Inject constructor(val appContext: Application, val repository: DbRepository)
    : AndroidViewModel(appContext), LocalDataSource.OnTimerSavedListener {

    @Inject
    lateinit var sharedPref: SharedPreferences

    // Activity transition LiveData
    val toPresetActivity: MutableLiveData<Void> = MutableLiveData()
    val toCustomizeActivity: MutableLiveData<Void> = MutableLiveData()
    val toSettings: MutableLiveData<Void> = MutableLiveData()
    val toTimerActivity: MutableLiveData<Int> = MutableLiveData()
    val snackbarTextRes: MutableLiveData<Int> = MutableLiveData()
    val focusListener: View.OnFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
        handleFocusChange(v, hasFocus)
    }

    var fromPreset: Boolean = false
    var fromTemp: Boolean = false


    lateinit var timerSetting: TimerSetting
    lateinit var timerSettingObservable: TimerSettingObservable
    var rounds: MutableList<Round> = mutableListOf()

    var offset: Int = 1

    fun start() {
        offset = sharedPref.getString("pref_key_increment_seconds", "1").toInt()
        initializeRounds()
    }

    private fun initializeRounds() {
        val presetId: Int = sharedPref.getInt("pref_key_last_used_preset_id", -1)
        val tempTimerId: Int = sharedPref.getInt("pref_key_last_used_timer_id", -1)

        if (presetId != -1) {
            // TODO: "Get the initial value from the preset table"
            fromPreset = true
        } else if (tempTimerId != -1) {
            // TODO: "Get the initial value from the timer_setting table"
            fromTemp = true
        } else {
            timerSetting = TimerSetting(180, 180) // default value
        }

        timerSettingObservable = TimerSettingObservable(timerSetting)
    }


    fun doToast() {
        timerSetting.parseRounds()
        Timber.d(timerSettingObservable.toString())
    }


    fun onTimerStartClicked() {
        timerSettingObservable.finalizeDetail()
        // gather observable field data, save to DB, extract the id and move to the next activity.
        if (!fromPreset && !fromTemp) {
            // just save this timer and move on
            repository.saveTimer(timerSettingObservable.getFinalSetting(), this)
        } else {
            if (timerSettingObservable.checkIfEdited()) {
                repository.saveTimer(timerSettingObservable.getFinalSetting(), this)
            } else {
                if (fromPreset) {
                    // keep this preset id and pass it as an extra
                } else {
                    // keep this timer id and pass it as an extra
                }
            }
        }
    }

    fun saveThisAsPreset() {
        timerSettingObservable.finalizeDetail()
        fromPreset = true
        // reinitialize the entries with the retrieved values
    }

    fun openPresetActivity() {
        toPresetActivity.value = null
    }

    fun openCustomizeActivity() {
        toCustomizeActivity.value = null
    }

    fun openSettings() {
        toSettings.value = null
    }

    fun toTimerActivity(timerId: Int) {
        toTimerActivity.value = timerId
    }

    // ------------------ EditText value change handling -----------------

    /**
     * Called when the focus is changed in any EditText (when the user finished editing)
     */
    private fun handleFocusChange(v: View, hasFocus: Boolean) {
        // Trimming user input after the focus is gone
        if (!hasFocus) {
            val session: Int = WorkoutSession.getSessionById(v.id)
            timerSettingObservable.handleChange(session, 0)
        }
    }

    /**
     * Called when the +/- button is clicked to increment/decrement the time or the number of rounds.
     * Add/subtract the offset to the current input and trim to user readable string.
     */
    fun adjustInput(clickedId: Int, increment: Boolean) {
        val session: Int = WorkoutSession.getSessionById(clickedId)
        offset = if (session == WorkoutSession.ROUND) 1 else offset
        timerSettingObservable.handleChange(session, offset * if (increment) 1 else -1)
        timerSettingObservable.calculateTotal()
    }


    // ------------------ Callbacks -----------------

    override fun onTimerSaved() {
        Timber.d("saved!")
    }

    override fun onTimerSaveNotAvailable() {
        Timber.d("failed!")
    }

}