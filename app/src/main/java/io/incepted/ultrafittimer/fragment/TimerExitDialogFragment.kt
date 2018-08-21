package io.incepted.ultrafittimer.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import io.incepted.ultrafittimer.R
import io.incepted.ultrafittimer.activity.TimerActivity

class TimerExitDialogFragment : DialogFragment() {
    companion object {
        fun newInstance(): TimerExitDialogFragment {
            return TimerExitDialogFragment()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity)
                .setCancelable(false)
                .setTitle(resources.getString(R.string.alert_dialog_timer_exit_warning_title))
                .setMessage(resources.getString(R.string.alert_dialog_timer_exit_warning_message))
                .setPositiveButton(resources.getString(R.string.alert_dialog_ok)) { dialog, _ ->
                    dialog?.dismiss()
                    (activity as TimerActivity).exitTimer()
                }
                .setNegativeButton(resources.getString(R.string.alert_dialog_cancel)) { dialog, _ ->
                    dialog?.dismiss()
                }
                .create()
    }
}