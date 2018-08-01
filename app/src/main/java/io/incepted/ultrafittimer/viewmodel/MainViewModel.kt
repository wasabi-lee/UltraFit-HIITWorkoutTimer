package io.incepted.ultrafittimer.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.widget.Toast

class MainViewModel(private val appContext: Application) : AndroidViewModel(appContext) {

    public fun doToast() {
        Toast.makeText(appContext, "yea", Toast.LENGTH_LONG)
    }


}