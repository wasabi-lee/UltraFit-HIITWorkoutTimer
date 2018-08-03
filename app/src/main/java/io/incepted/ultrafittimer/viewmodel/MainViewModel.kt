package io.incepted.ultrafittimer.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.content.SharedPreferences
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

    val bottomSheetToggle: MutableLiveData<Void> = MutableLiveData()

    // Activity transition LiveData
    val toPresetActivity: MutableLiveData<Void> = MutableLiveData()
    val toCustomizeActivity: MutableLiveData<Void> = MutableLiveData()
    val toSettings: MutableLiveData<Void> = MutableLiveData()
    val toTimerActivity: MutableLiveData<Int> = MutableLiveData()

    val snackbarTextRes: MutableLiveData<Int> = MutableLiveData()

    val focusListener: View.OnFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
        handleFocusChange(v, hasFocus)
    }


    public fun doToast() {
        Toast.makeText(appContext, "Hello!", Toast.LENGTH_LONG).show()
    }

    fun toggleBottomSheet() {
        bottomSheetToggle.value = null
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
        if (!hasFocus) {
            if (v.id == R.id.main_session_time_rounds_edit)
                trimRounds(v)
            else
                trimTime(v)
        }
    }

    private fun trimTime(v: View) {
        val editText: EditText = v as EditText
        val formatted: String = TimerUtil.convertStringToTimeString(editText.text.toString())
        val default = "00:00"

        if (formatted == "-1") {
            // Invalid input!
            snackbarTextRes.value = R.string.error_invalid_time
            editText.setText(default)
        } else {
            editText.setText(formatted)
        }

    }

    private fun trimRounds(v: View) {
        val editText: EditText = v as EditText
        val formatted: String = NumberUtil.convertToValidRoundNumber(editText.text.toString()).toString()
        val default = "0"

        if (formatted == "-1") {
            // Invalid input!
            snackbarTextRes.value = R.string.error_invalid_round
            editText.setText(default)
        } else {
            editText.setText(formatted)
        }
    }
}