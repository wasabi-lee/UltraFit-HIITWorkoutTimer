package io.incepted.ultrafittimer.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import io.incepted.ultrafittimer.R
import io.incepted.ultrafittimer.activity.CustomizeActivity

class ExitWarningDialogFragment : androidx.fragment.app.DialogFragment() {

    companion object {
        fun newInstance(title: Int): ExitWarningDialogFragment {
            val frag = ExitWarningDialogFragment()
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
                .setPositiveButton(R.string.alert_dialog_discard) { dialog, _ ->
                    dialog?.dismiss()
                    (activity as CustomizeActivity).discardThisSetting()
                }
                .setNegativeButton(R.string.alert_dialog_cancel) { dialog, _ -> dialog?.dismiss() }.create()
    }
}