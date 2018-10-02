package io.incepted.ultrafittimer.activity

import android.os.Bundle
import android.view.MenuItem
import io.incepted.ultrafittimer.R
import io.incepted.ultrafittimer.fragment.SettingsFragment
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        initToolbar()

        // Adding preference fragment
        if (savedInstanceState == null) {
            val prefFragment = SettingsFragment()
            val ft: androidx.fragment.app.FragmentTransaction? = supportFragmentManager.beginTransaction()
            ft?.add(R.id.settings_frag_container, prefFragment)
            ft?.commit()
        }
    }

    private fun initToolbar() {
        setSupportActionBar(settings_toolbar)
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
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
