package io.incepted.ultrafittimer.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.support.v4.app.DialogFragment
import android.os.Bundle
import android.view.View
import android.widget.EditText
import io.incepted.ultrafittimer.R
import io.incepted.ultrafittimer.activity.MainActivity

class PresetSaveDialogFragment() : DialogFragment() {

    companion object {
        fun newInstance(): PresetSaveDialogFragment {
            return PresetSaveDialogFragment()

        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        val dialogView: View = activity?.layoutInflater?.inflate(R.layout.dialog_preset_save, null)
                ?: return builder.create()

        builder.setView(dialogView)

        val editText: EditText = dialogView.findViewById(R.id.dialog_preset_name_edit)

        builder.setCancelable(true)
                .setTitle(resources.getString(R.string.alert_dialog_preset_save_title))
                .setPositiveButton(resources.getString(R.string.alert_dialog_ok)) { dialog, _ ->
                    (activity as MainActivity).savePreset(editText.text.toString())
                    dialog?.dismiss()
                }.setNegativeButton(resources.getString(R.string.alert_dialog_cancel)) { dialog, _ ->
                    dialog?.dismiss()
                }

        return builder.create()
    }
}