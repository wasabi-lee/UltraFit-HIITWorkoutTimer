package io.incepted.ultrafittimer.util

import android.graphics.Color
import com.google.android.material.snackbar.Snackbar
import android.view.View
import android.widget.TextView

object SnackbarUtil {
    fun showSnackBar(v: View?, snackbarText: String?) {
        if (v == null || snackbarText == null) return
        val snackbar: com.google.android.material.snackbar.Snackbar = com.google.android.material.snackbar.Snackbar.make(v, snackbarText, com.google.android.material.snackbar.Snackbar.LENGTH_LONG)
        val snackbarTextId: Int = com.google.android.material.R.id.snackbar_text
        val textView: TextView = snackbar.view.findViewById(snackbarTextId)
        textView.setTextColor(Color.WHITE)
        snackbar.show()
    }
}