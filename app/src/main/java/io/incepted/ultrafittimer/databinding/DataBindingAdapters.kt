package io.incepted.ultrafittimer.databinding

import androidx.databinding.BindingAdapter
import android.graphics.Color
import android.graphics.Paint
import androidx.recyclerview.widget.ItemTouchHelper
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.incepted.ultrafittimer.adapter.PresetAdapter
import io.incepted.ultrafittimer.adapter.SummaryAdapter
import io.incepted.ultrafittimer.db.model.Preset
import io.incepted.ultrafittimer.db.tempmodel.Round
import io.incepted.ultrafittimer.util.SwipeDeleteCallback
import timber.log.Timber

object DataBindingAdapters {

    infix fun Int.with(x: Int) = this.or(x)

    @JvmStatic
    @BindingAdapter("presetData")
    fun setPresetListData(v: androidx.recyclerview.widget.RecyclerView, data: List<Preset>) {
        val adapter: PresetAdapter = v.adapter as PresetAdapter
        adapter.replaceData(data as MutableList<Preset>)
    }

    @JvmStatic
    @BindingAdapter("roundData")
    fun setRoundData(v: RecyclerView, data: List<Round>) {
        val adapter: SummaryAdapter = v.adapter as SummaryAdapter
        adapter.replaceData(data as MutableList<Round>)
        Timber.d("from adapter ${data.size}")
    }


    @JvmStatic
    @BindingAdapter("onFocusChangedListener")
    fun setFocusListener(editText: EditText, listener: View.OnFocusChangeListener) {
        editText.onFocusChangeListener = listener
    }


    @JvmStatic
    @BindingAdapter("customized")
    fun setTextAppearance(v: TextView, isCustomized: Boolean) {
        v.text = if (isCustomized) "CUSTOMIZED" else "CUSTOMIZE"
        if (isCustomized) {
            v.paintFlags = v.paintFlags with Paint.UNDERLINE_TEXT_FLAG
        } else {
            v.paintFlags = v.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
        }
    }


    @JvmStatic
    @BindingAdapter("swipeDeleteListener")
    fun setSwipeDeleteListener(v: androidx.recyclerview.widget.RecyclerView, listener: SwipeDeleteCallback) {
        val itemTouchHelper = ItemTouchHelper(listener)
        itemTouchHelper.attachToRecyclerView(v)
    }


    @JvmStatic
    @BindingAdapter("positionText")
    fun setPositionText(v: TextView, dummy: Int) {

        // If the view we're binding's tag is "Warm Up" or "Cool Down", just set that tag as a text of this view.
        // If the tag is just an Int value, which represents the position of this view in the list,
        // set that position as the view text with the corresponding suffix.

        val tag = ((v.parent as View).tag).toString()
        if (tag == "Warm Up" || tag == "Cool Down") {
            v.text = tag
            return
        }

        val suffix: String = when (tag.toInt().rem(10)) {
            1 -> "st"
            2 -> "nd"
            3 -> "rd"
            else -> "th"
        }
        val result = "$tag$suffix"
        v.text = result
    }


    @JvmStatic
    @BindingAdapter("itemBackground")
    fun setItemBackground(v: androidx.constraintlayout.widget.ConstraintLayout, dummy: Int) {


    }

}

