package io.incepted.ultrafittimer.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import io.incepted.ultrafittimer.R
import io.incepted.ultrafittimer.databinding.ActivitySummaryBinding
import io.incepted.ultrafittimer.util.SnackbarUtil
import io.incepted.ultrafittimer.viewmodel.SummaryViewModel
import kotlinx.android.synthetic.main.activity_summary.*
import javax.inject.Inject

class SummaryActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_KEY_SUMMARY_PRESET_ID = "extra_key_summary_preset_id"
        const val EXTRA_KEY_SUMMARY_TIMER_ID = "extra_key_summary_timer_id"
    }

    @Inject
    lateinit var viewmodelfactory: ViewModelProvider.Factory
    private lateinit var summaryViewModel: SummaryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivitySummaryBinding = DataBindingUtil.setContentView(this, R.layout.activity_summary)
        summaryViewModel = ViewModelProviders.of(this, viewmodelfactory)
                .get(SummaryViewModel::class.java)
        binding.viewmodel = summaryViewModel

        initToolbar()
        initObservers()
    }

    private fun initToolbar() {
        setSupportActionBar(summary_toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initObservers() {

    }



    private fun showSnackbar(s: String) {
        SnackbarUtil.showSnackBar(findViewById(android.R.id.content), s)
    }

}
