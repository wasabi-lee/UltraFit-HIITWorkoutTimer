package io.incepted.ultrafittimer.util

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import io.incepted.ultrafittimer.viewmodel.MainViewModel

class ViewModelFactory private constructor(): ViewModelProvider.Factory {

    lateinit var application: Application

    private object Holder {
        val INSTANCE = ViewModelFactory()
    }

    companion object {
        val instance : ViewModelFactory by lazy {
            Holder.INSTANCE
        }
    }

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(application) as T
        }
        throw IllegalStateException("Unknown ViewModel class")
    }
}