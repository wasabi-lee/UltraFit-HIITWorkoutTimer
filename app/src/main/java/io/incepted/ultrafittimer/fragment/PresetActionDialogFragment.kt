package io.incepted.ultrafittimer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import io.incepted.ultrafittimer.R
import io.incepted.ultrafittimer.activity.PresetListActivity
import kotlinx.android.synthetic.main.preset_action_list_dialog.view.*

class PresetActionDialogFragment : androidx.fragment.app.DialogFragment(), AdapterView.OnItemClickListener {

    companion object {
        val ACTION_ITEMS = arrayListOf("Bookmark / Unbookmark", "Show Detail", "Edit", "Delete", "Play Now")
        const val ACTION_INDEX_BOOKMARK = 0
        const val ACTION_INDEX_DETAIL = 1
        const val ACTION_INDEX_EDIT = 2
        const val ACTION_INDEX_DELETE = 3
        const val ACTION_INDEX_PLAY = 4

        const val ARG_KEY_PRESET_ID = "arg_key_preset_id"

        fun newInstance(presetPosition: Int): PresetActionDialogFragment {
            val frag = PresetActionDialogFragment()
            val args = Bundle()
            args.putInt(ARG_KEY_PRESET_ID, presetPosition)
            frag.arguments = args
            return frag
        }
    }

    private var presetPosition = 0


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        presetPosition = arguments?.getInt(ARG_KEY_PRESET_ID, 0) ?: 0

        val dialogView: View = inflater.inflate(R.layout.preset_action_list_dialog, container, false)
                ?: return View(activity)

        val list: ListView = dialogView.preset_action_list_view

        val adapter: ArrayAdapter<String> = ArrayAdapter(activity, android.R.layout.simple_list_item_1, ACTION_ITEMS)
        list.onItemClickListener = this
        list.adapter = adapter

        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)

        return dialogView

    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        dialog.dismiss()

        when (position) {
            ACTION_INDEX_BOOKMARK -> (activity as PresetListActivity).bookmarkItem(presetPosition)
            ACTION_INDEX_DETAIL -> (activity as PresetListActivity).showPresetDetail(presetPosition)
            ACTION_INDEX_EDIT -> (activity as PresetListActivity).editItem(presetPosition)
            ACTION_INDEX_DELETE -> (activity as PresetListActivity).deleteItem(presetPosition)
            ACTION_INDEX_PLAY -> (activity as PresetListActivity).playPreset(presetPosition)
        }
    }


}