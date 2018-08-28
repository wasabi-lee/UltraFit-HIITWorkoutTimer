package io.incepted.ultrafittimer.activity

import android.content.SharedPreferences
import android.os.Bundle
import android.os.PersistableBundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import io.incepted.ultrafittimer.R
import timber.log.Timber
import javax.inject.Inject

open class BaseActivity : AppCompatActivity() {


    @Inject
    lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isDarkTheme = sharedPref
                .getBoolean(resources.getString(R.string.pref_key_theme), false)
        setTheme(if (isDarkTheme) R.style.AppTheme_dark else R.style.AppTheme_light)
    }

}