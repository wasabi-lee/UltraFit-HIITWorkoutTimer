package io.incepted.ultrafittimer.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.content.SharedPreferences
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.support.design.widget.BottomSheetBehavior
import android.view.View
import android.widget.EditText
import android.widget.Toast
import io.incepted.ultrafittimer.R
import io.incepted.ultrafittimer.db.DbRepository
import io.incepted.ultrafittimer.db.model.Preset
import io.incepted.ultrafittimer.db.model.TimerSetting
import io.incepted.ultrafittimer.db.source.LocalDataSource
import io.incepted.ultrafittimer.db.tempmodel.Round
import io.incepted.ultrafittimer.util.NumberUtil
import io.incepted.ultrafittimer.util.TimerUtil
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.round

class MainViewModel @Inject constructor(val appContext: Application, val repository: DbRepository) : AndroidViewModel(appContext) {

    @Inject
    lateinit var sharedPref: SharedPreferences

    val bottomSheetState: ObservableInt = ObservableInt(BottomSheetBehavior.STATE_COLLAPSED)

    val warmUpSeconds: ObservableField<String> = ObservableField("01:00") //TODO("Init with the initial value later")

    val workSeconds: ObservableField<String> = ObservableField("00:20")

    val restSeconds: ObservableField<String> = ObservableField("01:00")

    val roundCount: ObservableField<String> = ObservableField("8")

    val coolDownSeconds: ObservableField<String> = ObservableField("01:00")

    val totalSeconds: ObservableField<String> = ObservableField("")

    // Activity transition LiveData
    val toPresetActivity: MutableLiveData<Void> = MutableLiveData()

    val toCustomizeActivity: MutableLiveData<Void> = MutableLiveData()

    val toSettings: MutableLiveData<Void> = MutableLiveData()

    val toTimerActivity: MutableLiveData<Int> = MutableLiveData()

    val snackbarTextRes: MutableLiveData<Int> = MutableLiveData()

    val focusListener: View.OnFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
        handleFocusChange(v, hasFocus)
    }

    val bottomSheetListener: BottomSheetBehavior.BottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_COLLAPSED || newState == BottomSheetBehavior.STATE_EXPANDED)
                bottomSheetState.set(newState)
        }
    }

    lateinit var timerSetting: TimerSetting
    var rounds: MutableList<Round> = mutableListOf()


    fun start() {
        initializeRounds()
        calculateTotal()
    }

    private fun initializeRounds() {
        val presetId: Int = sharedPref.getInt("pref_key_last_used_preset_id", -1)
        val tempTimerId: Int = sharedPref.getInt("pref_key_last_used_timer_id", -1)

        if (presetId == -1) {
            TODO("Get the initial value from the preset table")
        } else if (tempTimerId == -1) {
            TODO("Get the initial value from the timer_setting table")
        } else {
            timerSetting = TimerSetting(180, 180) // default value
        }

        warmUpSeconds.set(timerSetting.warmupSeconds.toString())
        workSeconds.set(timerSetting.mRounds[0].workSeconds.toString())
        restSeconds.set(timerSetting.mRounds[0].restSeconds.toString())
        roundCount.set(timerSetting.mRounds.size.toString())
        coolDownSeconds.set(timerSetting.cooldownSeconds.toString())
    }


    fun doToast() {
        timerSetting.parseRounds()
        Timber.d(timerSetting.toString())
    }

    fun isBottomSheetExpanded(): Boolean {
        return bottomSheetState.get() == BottomSheetBehavior.STATE_EXPANDED
    }

    fun toggleBottomSheet() {
        val newState: Int =
                if (!isBottomSheetExpanded())
                    BottomSheetBehavior.STATE_EXPANDED
                else BottomSheetBehavior.STATE_COLLAPSED

        bottomSheetState.set(newState)
    }


    fun onTimerStartClicked() {
        // gather observable field data, save to DB, extract the id and move to the next activity.
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

    // ------------------ EditText focus change handling -----------------

    /**
     * Called when the focus is changed in any EditText (when the user finished editing)
     */
    private fun handleFocusChange(v: View, hasFocus: Boolean) {
        // Trimming user input after the focus is gone
        if (!hasFocus) {
            if (v.id == R.id.main_session_time_rounds_edit)
                (v as EditText).setText(getTrimmedRound(v.text.toString(), 0))
            else
                (v as EditText).setText(getTrimmedTime(v.text.toString(), 0))
            calculateTotal()
        }
    }


    /**
     * Called when the +/- button is clicked to increment/decrement the time or the number of rounds.
     * Add/subtract the offset to the current input and trim to user readable string.
     */
    fun adjustInput(clickedId: Int, increment: Boolean) {
        val isRound = (clickedId == R.id.main_session_rounds_plus_iv || clickedId == R.id.main_session_rounds_minus_iv)
        val fieldToUpdate = getFieldToUpdate(clickedId)
        val inputToTrim: String = fieldToUpdate?.get() ?: return
        val offset: Int = (
                if (isRound) 1
                else (sharedPref.getString("pref_key_increment_seconds", "1")).toInt()
                ) * (if (increment) 1 else -1)

        fieldToUpdate.set(if (isRound) getTrimmedRound(inputToTrim, offset) else getTrimmedTime(inputToTrim, offset))
        calculateTotal()
    }

    /**
     * Get the corresponding ObservableField for the clicked view id.
     */


    private fun getFieldToUpdate(clickedId: Int): ObservableField<String>? {
        return when (clickedId) {
            R.id.main_session_warmup_plus_iv, R.id.main_session_warmup_minus_iv -> warmUpSeconds
            R.id.main_session_work_plus_iv, R.id.main_session_work_minus_iv -> workSeconds
            R.id.main_session_rest_plus_iv, R.id.main_session_rest_minus_iv -> restSeconds
            R.id.main_session_rounds_plus_iv, R.id.main_session_rounds_minus_iv -> roundCount
            R.id.main_session_cooldown_plus_iv, R.id.main_session_cooldown_minus_iv -> coolDownSeconds
            else -> null
        }
    }

    private fun getTrimmedTime(input: String, offset: Int): String {
        val formatted: String = TimerUtil.convertStringToTimeString(input, offset)
        val default = "00:00"

        return if (formatted == "-1") { // Invalid input!
            snackbarTextRes.value = R.string.error_invalid_time
            default
        } else {
            formatted
        }
    }

    private fun getTrimmedRound(input: String, offset: Int): String {
        val formatted: String = NumberUtil.convertToValidRoundNumber(input, offset).toString()
        val default = "0"

        return if (formatted == "-1") { // Invalid input!
            snackbarTextRes.value = R.string.error_invalid_round
            default
        } else {
            formatted
        }
    }

    private fun calculateTotal() {
        totalSeconds.set(TimerUtil.convertSecondsToTimeString(timerSetting.calculateTotal()))
    }
}