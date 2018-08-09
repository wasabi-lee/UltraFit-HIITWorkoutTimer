package io.incepted.ultrafittimer.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.content.SharedPreferences
import android.databinding.ObservableArrayList
import android.databinding.ObservableList
import android.view.View
import io.incepted.ultrafittimer.db.DbRepository
import io.incepted.ultrafittimer.db.tempmodel.Round
import io.incepted.ultrafittimer.util.DbDelimiter
import timber.log.Timber
import javax.inject.Inject

class CustomizeViewModel @Inject constructor(appContext: Application, val repository: DbRepository)
    : AndroidViewModel(appContext) {

    @Inject
    lateinit var sharedPref: SharedPreferences

    val focusListener: View.OnFocusChangeListener = View.OnFocusChangeListener{v, hasFocus ->
        // TODO implement focus change handling function
    }

    var data: ObservableList<Round> = ObservableArrayList()


    fun start(extras: ArrayList<String>) {
        initRounds(extras)
    }

    private fun initRounds(extras: ArrayList<String>) {
        val names = extras[0].split(DbDelimiter.DELIMITER)
        val workSeconds = extras[1].split(DbDelimiter.DELIMITER)
        val restSeconds = extras[2].split(DbDelimiter.DELIMITER)

        val l: ArrayList<Round> = arrayListOf()
        (0 until names.size)
                .forEach { i ->
                    l.add(Round(workoutName = names[i],
                            workSeconds = workSeconds[i].toInt(),
                            restSeconds = restSeconds[i].toInt()))
                }
        populateRoundList(l)
    }

    private fun populateRoundList(l: ArrayList<Round>) {
        data.clear()
        data.addAll(l)
    }




}