package io.incepted.ultrafittimer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import android.content.SharedPreferences
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import io.incepted.ultrafittimer.R
import io.incepted.ultrafittimer.activity.CustomizeActivity
import io.incepted.ultrafittimer.db.DbRepository
import io.incepted.ultrafittimer.db.tempmodel.Round
import io.incepted.ultrafittimer.db.DbDelimiter
import io.incepted.ultrafittimer.util.RoundUtil
import io.incepted.ultrafittimer.util.SwipeDeleteCallback
import io.incepted.ultrafittimer.util.TimerSettingChangeChecker
import javax.inject.Inject

class CustomizeViewModel @Inject constructor(val appContext: Application, val repository: DbRepository)
    : AndroidViewModel(appContext) {

    @Inject
    lateinit var sharedPref: SharedPreferences

    val swipeHandler: SwipeDeleteCallback = object : SwipeDeleteCallback(appContext) {
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            removeAt(viewHolder.adapterPosition)
        }
    }

    val backToMainWithResult = MutableLiveData<Bundle>()

    val deletedItemPosition = MutableLiveData<Int>()

    val insertedItemPosition = MutableLiveData<Int>()

    var offset: Int = 1

    fun start() {
        offset = sharedPref
                .getString(appContext
                        .resources
                        .getString(R.string.pref_key_increment_seconds), "1")?.toInt() ?: offset

    }

    fun initRounds(extras: ArrayList<String>): MutableList<Round> {
        // Splitting the delimiter concatenated String into String[] form.
        // i.e.) "20-10-4-50-3..." -> ["20", "10", "4", "50", "3"...]
        val names = extras[0].split(DbDelimiter.DELIMITER)
        val workSeconds = extras[1].split(DbDelimiter.DELIMITER)
        val restSeconds = extras[2].split(DbDelimiter.DELIMITER)

        val l: ArrayList<Round> = arrayListOf()
        (0 until names.size)
                .forEach { i ->
                    l.add(Round(workoutName = names[i],
                            workSeconds = workSeconds[i].toInt(),
                            restSeconds = restSeconds[i].toInt(),
                            offset = offset))
                }
        return l
    }

    fun getCopiedRounds(original: MutableList<Round>): MutableList<Round> {
        val result = mutableListOf<Round>()
        original.forEach {
            result.add(it.copy().also { copied -> copied.offset = offset })
        }
        return result
    }


    fun removeAt(position: Int?) {
        deletedItemPosition.value = position
    }

    fun organizeResult(original: MutableList<Round>, current: MutableList<Round>) {
        val finalSetting = finalizeDetail(current)

        val result = RoundUtil.joinListToString(finalSetting)

        val bundle = Bundle()

        bundle.putString(CustomizeActivity.RESULT_KEY_WORKOUT_NAMES, result[0])
        bundle.putString(CustomizeActivity.RESULT_KEY_WORKOUT_WORKS, result[1])
        bundle.putString(CustomizeActivity.RESULT_KEY_WORKOUT_RESTS, result[2])
        bundle.putBoolean(CustomizeActivity.RESULT_KEY_WORKOUT_CUSTOMIZED, checkIfModified(original, finalSetting))

        backToMainWithResult.value = bundle
    }

    private fun finalizeDetail(result: MutableList<Round>): MutableList<Round> {
        result.forEach { it.trimValues() }
        return result
    }

    fun checkIfModified(original: MutableList<Round>, result: MutableList<Round>): Boolean {
        return TimerSettingChangeChecker.roundChanged(original, result)
    }

}