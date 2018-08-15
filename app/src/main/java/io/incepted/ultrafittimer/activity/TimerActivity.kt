package io.incepted.ultrafittimer.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import io.incepted.ultrafittimer.R
import kotlinx.android.synthetic.main.activity_timer.*

class TimerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_KEY_FROM_PRESET = "extra_key_from_preset"
        const val EXTRA_KEY_ID = "extra_key_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

    }


}
