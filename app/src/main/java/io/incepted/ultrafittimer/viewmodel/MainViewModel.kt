package io.incepted.ultrafittimer.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.content.SharedPreferences
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.support.design.widget.BottomSheetBehavior
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import io.incepted.ultrafittimer.R
import io.incepted.ultrafittimer.util.NumberUtil
import io.incepted.ultrafittimer.util.TimerUtil
import javax.inject.Inject

class MainViewModel @Inject constructor(val appContext: Application) : AndroidViewModel(appContext) {

    private val TAG: String = MainViewModel::class.java.simpleName

    @Inject
    lateinit var sharedPref: SharedPreferences

    val bottomSheetState: ObservableInt = ObservableInt(BottomSheetBehavior.STATE_COLLAPSED)

    val warmUpSeconds: ObservableField<String> = ObservableField("01:00") //TODO("Init with the initial value later")
    val workSeconds: ObservableField<String> = ObservableField("00:20")
    val restSeconds: ObservableField<String> = ObservableField("01:00")
    val roundCount: ObservableField<String> = ObservableField("8")
    val coolDownSeconds: ObservableField<String> = ObservableField("01:00")

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
            Log.d(TAG, "Bottomsheet clicked $newState")
            if (newState == BottomSheetBehavior.STATE_COLLAPSED || newState == BottomSheetBehavior.STATE_EXPANDED)
                bottomSheetState.set(newState)
        }
    }


    public fun doToast() {
        Toast.makeText(appContext, "Hello!", Toast.LENGTH_LONG).show()
    }

    fun toggleBottomSheet() {
        val newState: Int =
                if (bottomSheetState.get() == BottomSheetBehavior.STATE_COLLAPSED)
                    BottomSheetBehavior.STATE_EXPANDED
                else BottomSheetBehavior.STATE_COLLAPSED

        bottomSheetState.set(newState)
    }

    fun adjustInput(clickedId: Int, increment: Boolean) {
        val isRound = clickedId == R.id.main_session_rounds_plus_iv || clickedId == R.id.main_session_rounds_minus_iv
        val fieldToUpdate = getFieldToUpdate(clickedId, isRound)

        val inputToTrim: String = fieldToUpdate?.get() ?: return
        val offset: Int = (
                if (isRound) 1
                else (sharedPref.getString("pref_key_increment_seconds", "1")).toInt()
                ) * (if (increment) 1 else -1)

        fieldToUpdate.set(if (isRound) getTrimmedRound(inputToTrim, offset) else getTrimmedTime(inputToTrim, offset))

    }

    fun getFieldToUpdate(clickedId: Int, isRound: Boolean): ObservableField<String>? {
        return if (isRound) {
            roundCount
        } else {
            when (clickedId) {
                R.id.main_session_warmup_plus_iv, R.id.main_session_warmup_minus_iv -> warmUpSeconds
                R.id.main_session_work_plus_iv, R.id.main_session_work_minus_iv -> workSeconds
                R.id.main_session_rest_plus_iv, R.id.main_session_rest_minus_iv -> restSeconds
                R.id.main_session_cooldown_plus_iv, R.id.main_session_cooldown_minus_iv -> coolDownSeconds
                else -> null
            }
        }
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
        Log.d(TAG, sharedPref.getBoolean("test", true).toString())
    }

    fun toTimerActivity(timerId: Int) {
        toTimerActivity.value = timerId
    }

    // ------------------ EditText focus change handling -----------------

    private fun handleFocusChange(v: View, hasFocus: Boolean) {
        // Trimming user input after the focus is gone
        if (!hasFocus) {
            if (v.id == R.id.main_session_time_rounds_edit)
                (v as EditText).setText(getTrimmedRound(v.text.toString(), 0))
            else
                (v as EditText).setText(getTrimmedTime(v.text.toString(), 0))
        }
    }


    private fun getTrimmedTime(input: String, offset: Int): String {
        val formatted: String = TimerUtil.convertStringToTimeString(input, offset)
        val default = "00:00"

        if (formatted == "-1") {
            // Invalid input!
            snackbarTextRes.value = R.string.error_invalid_time
            return default
        } else {
            return formatted
        }
    }

    private fun getTrimmedRound(input: String, offset: Int): String {
        val formatted: String = NumberUtil.convertToValidRoundNumber(input, offset).toString()
        val default = "0"

        if (formatted == "-1") {
            // Invalid input!
            snackbarTextRes.value = R.string.error_invalid_round
            return default
        } else {
            return formatted
        }
    }
}