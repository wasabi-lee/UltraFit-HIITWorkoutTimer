package io.incepted.ultrafittimer.db.tempmodel

import androidx.databinding.ObservableField
import android.view.View
import io.incepted.ultrafittimer.util.TimerUtil
import io.incepted.ultrafittimer.util.WorkoutSession
import timber.log.Timber

data class Round(var workoutName: String, var workSeconds: Int, var restSeconds: Int) {

    var offset: Int = 1

    val focusListener = View.OnFocusChangeListener { v, hasFocus ->
        handleFocusChange(v, hasFocus)
    }

    var name: ObservableField<String> = ObservableField()
    var work: ObservableField<String> = ObservableField()
    var rest: ObservableField<String> = ObservableField()

    var isWarmup = false
    var isCooldown = false

    constructor(workoutName: String, workSeconds: Int, restSeconds: Int, offset: Int) : this(workoutName, workSeconds, restSeconds) {
        this.offset = offset
    }

    init {
        name.set(workoutName)
        work.set(TimerUtil.secondsToTimeString(workSeconds))
        rest.set(TimerUtil.secondsToTimeString(restSeconds))
    }


    private fun handleFocusChange(v: View, hasFocus: Boolean) {
        if (!hasFocus) {
            val session: Int = v.tag as Int
            adjustChange(session, 0)
        }
    }

    fun applyOffset(session: Int, increment: Boolean) {
        adjustChange(session, offset * (if (increment) 1 else -1))
    }

    private fun adjustChange(session: Int, offset: Int) {
        val fieldToUpdate = if (session == WorkoutSession.WORK) work else rest
        var trimmed: String = TimerUtil.stringToTimeString(fieldToUpdate.get() ?: return, offset)
        trimmed = if (trimmed == "00:00" && session == WorkoutSession.WORK) "00:01" else trimmed
        fieldToUpdate.set(trimmed)
    }


    fun trimValues() {
        workoutName = name.get()?.trim() ?: workoutName
        workSeconds = TimerUtil.stringToSecond(work.get() ?: "0")
        restSeconds = TimerUtil.stringToSecond(rest.get() ?: "0")
    }


    fun printData() {
        Timber.d("Clicked round: ${work.get()} - ${rest.get()}")
    }


}


