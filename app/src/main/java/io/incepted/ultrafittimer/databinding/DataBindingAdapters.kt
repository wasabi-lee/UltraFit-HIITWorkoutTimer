package io.incepted.ultrafittimer.databinding

import android.databinding.BindingAdapter
import android.graphics.Color
import android.support.design.widget.BottomSheetBehavior
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import io.incepted.ultrafittimer.adapter.RoundAdapter
import io.incepted.ultrafittimer.db.tempmodel.Round

object DataBindingAdapters {

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

    @JvmStatic
    @BindingAdapter("customized")
    fun setTextAppearance(v: TextView, isCustomized: Boolean) {
        v.text = if (isCustomized) "CUSTOMIZED" else "CUSTOMIZE"
        v.setTextColor(if (isCustomized) Color.GREEN else Color.BLACK)
    }

    @JvmStatic
    @BindingAdapter("roundItems")
    fun setRoundItems(v: RecyclerView, items: List<Round>) {
        val adapter: RoundAdapter = v.adapter as RoundAdapter
        adapter.replaceData(items)
    }

}

