package io.incepted.ultrafittimer.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import io.incepted.ultrafittimer.R
import kotlinx.android.synthetic.main.activity_preset_list.*

class PresetListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preset_list)


        setSupportActionBar(preset_list_toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.preset_list_menu, menu)
        return true
    }
}
