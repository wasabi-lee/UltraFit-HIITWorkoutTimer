package io.incepted.ultrafittimer.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.util.Log
import android.widget.Toast
import javax.inject.Inject

class MainViewModel @Inject constructor(val appContext: Application) : AndroidViewModel(appContext) {

    companion object {
        private val TAG: String = MainViewModel::class.java.simpleName
    }


    public fun doToast() {
        Toast.makeText(appContext, "Hello!", Toast.LENGTH_LONG).show()
    }


}