package io.incepted.ultrafittimer.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import io.incepted.ultrafittimer.R
import kotlinx.android.synthetic.main.activity_feedback.*
import android.os.Build
import android.content.pm.PackageManager
import android.content.pm.PackageInfo
import android.content.Intent
import android.view.MenuItem


class FeedbackActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        initToolbar()

        feedback_send_button.setOnClickListener {
            sendFeedback()
        }
    }

    private fun initToolbar() {
        setSupportActionBar(feedback_toolbar)
        if (supportActionBar != null) {
            supportActionBar?.title = ""
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
    }


    private fun sendFeedback() {
        val subject = getString(R.string.app_name) + " Feedback"
        val header = "Device: " + getDeviceModelNumber() +
                "\nOS Version: " + getDeviceOSVersion() +
                "\nApp Version: " + getAppVersion()

        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.type = "text/plain"
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("inceptedapps@gmail.com"))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
        emailIntent.putExtra(Intent.EXTRA_TEXT, header)
        if (emailIntent.resolveActivity(packageManager) != null) {
            startActivity(emailIntent)
        }
    }

    private fun getDeviceModelNumber(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.startsWith(manufacturer)) {
            capitalize(model)
        } else {
            capitalize(manufacturer) + " " + model
        }
    }

    private fun getAppVersion(): String {
        val pInfo: PackageInfo?
        return try {
            pInfo = packageManager.getPackageInfo(
                    packageName, PackageManager.GET_META_DATA)
            if (pInfo != null) pInfo.versionName else ""
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            ""
        }

    }

    private fun getDeviceOSVersion(): String {
        val versionCode = Build.VERSION.RELEASE
        val sdkVersion = Build.VERSION.SDK_INT
        return if (versionCode != null) {
            "$versionCode / $sdkVersion"
        } else {
            "" + sdkVersion
        }
    }

    private fun capitalize(s: String?): String {
        if (s == null || s.isEmpty()) {
            return ""
        }
        val first = s[0]
        return if (Character.isUpperCase(first)) {
            s
        } else {
            Character.toUpperCase(first) + s.substring(1)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
