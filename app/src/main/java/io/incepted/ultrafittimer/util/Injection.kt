package io.incepted.ultrafittimer.util

import android.app.Application
import android.content.Context

object Injection {
    fun provideViewModelFactory(application : Application) : ViewModelFactory {
        val vf :ViewModelFactory = ViewModelFactory.instance
        vf.application = application
        return vf
    }
}