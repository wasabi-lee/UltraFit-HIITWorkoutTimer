package io.incepted.ultrafittimer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.RadioButton
import io.incepted.ultrafittimer.R
import io.incepted.ultrafittimer.db.tempmodel.SoundItem
import androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior.setTag
import android.widget.LinearLayout
import android.widget.CheckBox
import android.widget.TextView
import io.incepted.ultrafittimer.generated.callback.OnClickListener
import kotlinx.android.synthetic.main.sound_setting_list_item.view.*


class SoundSettingAdapter(context: Context, val soundList: List<SoundItem>, val callback: SoundSelectCallback)
    : ArrayAdapter<SoundItem>(context, -1){

    interface SoundSelectCallback {
        fun onSoundSelected(selectedVal: Int)
    }


    class ViewHolder {
        lateinit var radioBtn: RadioButton
    }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val currentItem = soundList[position]
        val viewHolder: ViewHolder
        lateinit var cv: View

        if (convertView == null) {
            cv = LayoutInflater.from(context).inflate(R.layout.sound_setting_list_item, parent, false)
            viewHolder = ViewHolder()
            viewHolder.radioBtn = cv.sound_setting_radio_btn
            viewHolder.radioBtn.tag = viewHolder
        } else {
            cv = convertView
            viewHolder = cv.sound_setting_radio_btn.tag as ViewHolder
        }

        viewHolder.radioBtn.text = currentItem.soundName
        viewHolder.radioBtn.isChecked = currentItem.selected
        viewHolder.radioBtn.setOnClickListener { _ ->
            callback.onSoundSelected(currentItem.soundValue)
            swapSeletedSound(soundList[position])
            notifyDataSetChanged()
        }

        return cv
    }

    override fun getCount(): Int {
        return soundList.size
    }

    fun getSelectedSound(): SoundItem {
        return soundList.first { it.selected }
    }

    fun swapSeletedSound(newSelection: SoundItem) {
        getSelectedSound().selected = false
        newSelection.selected = true
    }

}