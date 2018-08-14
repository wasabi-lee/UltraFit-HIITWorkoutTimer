package io.incepted.ultrafittimer.util

import android.support.v4.app.DialogFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import io.incepted.ultrafittimer.R
import kotlinx.android.synthetic.main.preset_action_list_dialog.view.*
import timber.log.Timber

class PresetActionDialogFragment : DialogFragment(), AdapterView.OnItemClickListener {

    companion object {
        val ACTION_ITEMS = arrayListOf<String>("Bookmark", "Detail", "Edit", "Delete", "Play Now")
        val ACTION_INDEX_BOOKMARK = 0
        val ACTION_INDEX_DETAIL = 1
        val ACTION_INDEX_EDIT = 2
        val ACTION_INDEX_DELETE = 3
        val ACTION_INDEX_PLAY = 4

        val ARG_KEY_PRESET_ID = "arg_key_preset_id"

        fun newInstance(presetPosition: Long): PresetActionDialogFragment {
            val frag = PresetActionDialogFragment()
            val args = Bundle()
            args.putLong(ARG_KEY_PRESET_ID, presetPosition)
            frag.arguments = args
            return frag
        }
    }

    private var presetID = 0L


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        presetID = arguments?.getLong(ARG_KEY_PRESET_ID, 0L) ?: 0L

        val dialogView: View = activity?.layoutInflater?.inflate(R.layout.preset_action_list_dialog, null, false)
                ?: return View(activity)

        val list: ListView = dialogView.preset_action_list_view

        val adapter: ArrayAdapter<String> = ArrayAdapter(activity, android.R.layout.simple_list_item_1, ACTION_ITEMS)
        list.divider = null
        list.dividerHeight = 0
        list.adapter = adapter
        list.onItemClickListener = this

        return dialogView

    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (position) {
            ACTION_INDEX_BOOKMARK -> Timber.d("$presetID Bookmark!")
            ACTION_INDEX_DETAIL -> Timber.d("$presetID Detail!")
            ACTION_INDEX_EDIT -> Timber.d("$presetID Edit!")
            ACTION_INDEX_DELETE -> Timber.d("$presetID Delete!")
            ACTION_INDEX_PLAY -> Timber.d("$presetID Play!")
        }
    }


}