package io.incepted.ultrafittimer.databinding

import android.databinding.BindingAdapter
import android.databinding.InverseBindingAdapter
import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.support.design.widget.BottomSheetBehavior
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import io.incepted.ultrafittimer.adapter.RoundAdapter
import io.incepted.ultrafittimer.db.tempmodel.Round
import io.incepted.ultrafittimer.util.NumberUtil
import io.incepted.ultrafittimer.util.SwipeDeleteCallback
import io.incepted.ultrafittimer.util.TimerUtil
import timber.log.Timber

object DataBindingAdapters {

    @JvmStatic
    @BindingAdapter("onFocusChangedListener")
    fun setFocusListener(editText: EditText, listener: View.OnFocusChangeListener) {
        editText.onFocusChangeListener = listener
    }


    @JvmStatic
    @BindingAdapter("customized")
    fun setTextAppearance(v: TextView, isCustomized: Boolean) {
        v.text = if (isCustomized) "CUSTOMIZED" else "CUSTOMIZE"
        v.setTextColor(if (isCustomized) Color.GREEN else Color.BLACK)
    }


    @JvmStatic
    @BindingAdapter("swipeDeleteListener")
    fun setSwipeDeleteListener(v: RecyclerView, listener: SwipeDeleteCallback) {
        val itemTouchHelper = ItemTouchHelper(listener)
        itemTouchHelper.attachToRecyclerView(v)
    }


    @JvmStatic
    @BindingAdapter("positionText")
    fun setPositionText(v: TextView, dummy: Int) {
        val position: Int? = (v.parent as View).tag as Int?
        val suffix: String = when (position?.rem(10)) {
            1 -> "st."
            2 -> "nd."
            3 -> "rd."
            else -> "th."
        }
        val result = "$position$suffix"
        v.text = result
    }


    @JvmStatic
    @BindingAdapter("itemBackground")
    fun setItemBackground(v: ConstraintLayout, dummy: Int) {
        val position: Int? = v.tag as Int?

        v.setBackgroundColor(
                if (position?.rem(2) == 0)
                    Color.parseColor("#f8f7f7")
                else Color.WHITE)

    }

}

