package io.incepted.ultrafittimer.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import io.incepted.ultrafittimer.R
import kotlinx.android.synthetic.main.activity_summary.*

class SummaryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)


        setSupportActionBar(summary_toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}
