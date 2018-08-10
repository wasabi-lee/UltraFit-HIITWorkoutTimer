package io.incepted.ultrafittimer.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import io.incepted.ultrafittimer.R
import io.incepted.ultrafittimer.activity.CustomizeActivity

class AlertDialogFragment : DialogFragment() {

    companion object {
        fun newInstance(title: Int): AlertDialogFragment {
            val frag = AlertDialogFragment()
            val args = Bundle()
            args.putInt("title", title)
            frag.arguments = args
            return frag
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val title = arguments?.getInt("title")

        return AlertDialog.Builder(activity)
                .setCancelable(false)
                .setTitle(title ?: R.string.alert_dialog_exit_warning_title)
                .setMessage(R.string.alert_dialog_exit_warning_message)
                .setPositiveButton(R.string.alert_dialog_discard) { _, _ -> (activity as CustomizeActivity).onBackPressed() }
                .setNegativeButton(R.string.alert_dialog_cancel) { dialog, _ -> dialog?.dismiss() }.create()
    }
}