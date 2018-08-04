package io.incepted.ultrafittimer.databinding

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.databinding.BindingAdapter
import android.os.SystemClock
import android.support.design.widget.BottomSheetBehavior
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.EditText
import android.widget.ImageView

object DataBindingAdapters {

    private val TAG: String = DataBindingAdapters::class.java.simpleName

    @JvmStatic
    @BindingAdapter("onFocusChangedListener")
    fun setFocusListener(editText: EditText, listener: View.OnFocusChangeListener) {
        editText.onFocusChangeListener = listener
    }

    @JvmStatic
    @BindingAdapter("bottomSheetListener")
    fun setCallback(v: View, listener: BottomSheetBehavior.BottomSheetCallback) {
        val behavior: BottomSheetBehavior<View> = BottomSheetBehavior.from(v)
        behavior.setBottomSheetCallback(listener)
    }

    @JvmStatic
    @BindingAdapter("bottomSheetState")
    fun setState(v: View, state: Int) {
        val behavior: BottomSheetBehavior<View> = BottomSheetBehavior.from(v)
        behavior.state = state
    }

    @JvmStatic
    @BindingAdapter("bottomSheetStateRotate")
    fun rotateIndicator(v: ImageView, state: Int) {
        v.animate()
                .setDuration(50)
                .rotation(if (state == BottomSheetBehavior.STATE_EXPANDED) 180F else 0F)
                .start()
    }




}