package io.incepted.ultrafittimer.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import io.incepted.ultrafittimer.R
import kotlinx.android.synthetic.main.activity_customize.*

class CustomizeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customize)

        setSupportActionBar(customize_toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.customize_menu, menu)
        return true
    }
}
