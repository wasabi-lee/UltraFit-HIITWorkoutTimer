package io.incepted.ultrafittimer.activity

import android.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceFragment
import io.incepted.ultrafittimer.R
import io.incepted.ultrafittimer.fragment.SettingsFragment
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        initToolbar()

        // Adding preference fragment
        if (savedInstanceState == null) {
            val prefFragment = SettingsFragment()
            val ft: android.support.v4.app.FragmentTransaction? = supportFragmentManager.beginTransaction()
            ft?.add(R.id.settings_frag_container, prefFragment)
            ft?.commit()
        }
    }

    private fun initToolbar() {
        setSupportActionBar(settings_toolbar)
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

}
