package io.incepted.ultrafittimer.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.content.SharedPreferences
import android.databinding.ObservableArrayList
import android.databinding.ObservableList
import android.support.v7.widget.RecyclerView
import android.view.View
import io.incepted.ultrafittimer.db.DbRepository
import io.incepted.ultrafittimer.db.tempmodel.Round
import io.incepted.ultrafittimer.util.DbDelimiter
import io.incepted.ultrafittimer.util.SwipeDeleteCallback
import timber.log.Timber
import javax.inject.Inject

class CustomizeViewModel @Inject constructor(appContext: Application, val repository: DbRepository)
    : AndroidViewModel(appContext) {

    @Inject
    lateinit var sharedPref: SharedPreferences

    val swipeHandler: SwipeDeleteCallback = object : SwipeDeleteCallback(appContext) {
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
            removeAt(viewHolder?.adapterPosition)
        }
    }

    val deletedItemPosition = MutableLiveData<Int>()

    val insertedItemPosition = MutableLiveData<Int>()

    var offset: Int = 1

    fun start() {
        offset = sharedPref.getString("pref_key_increment_seconds", "1").toInt()

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
        l.add(Round("", 0, 0, offset)) // dummy item for footer layout

        return l
    }


    fun removeAt(position: Int?) {
        deletedItemPosition.value = position
    }





}