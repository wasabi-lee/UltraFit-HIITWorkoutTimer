package io.incepted.ultrafittimer.util

import android.graphics.Color
import android.support.design.widget.Snackbar
import android.view.View
import android.widget.TextView

object SnackbarUtil {
    fun showSnackBar(v: View?, snackbarText: String?) {
        if (v == null || snackbarText == null) return
        val snackbar: Snackbar = Snackbar.make(v, snackbarText, Snackbar.LENGTH_LONG)
        val snackbarTextId: Int = android.support.design.R.id.snackbar_text
        val textView: TextView = snackbar.view.findViewById(snackbarTextId)
        textView.setTextColor(Color.WHITE)
        snackbar.show()
    }
}