package io.incepted.ultrafittimer.databinding

import android.databinding.BindingAdapter
import android.view.View
import android.widget.EditText

object DataBindingAdapters {

    @JvmStatic
    @BindingAdapter("onFocusChangedListener")
    fun setFocusListener(editText: EditText, listener: View.OnFocusChangeListener) {
        editText.onFocusChangeListener = listener
    }


}