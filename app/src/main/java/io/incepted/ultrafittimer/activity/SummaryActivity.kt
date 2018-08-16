package io.incepted.ultrafittimer.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import io.incepted.ultrafittimer.R
import io.incepted.ultrafittimer.adapter.SummaryAdapter
import io.incepted.ultrafittimer.databinding.ActivitySummaryBinding
import io.incepted.ultrafittimer.util.SnackbarUtil
import io.incepted.ultrafittimer.viewmodel.SummaryViewModel
import kotlinx.android.synthetic.main.activity_summary.*
import javax.inject.Inject

class SummaryActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_KEY_SUMMARY_ID = "extra_key_summary_preset_id"
        const val EXTRA_KEY_SUMMARY_IS_PRESET = "extra_key_summary_is_preset"
    }

    @Inject
    lateinit var viewmodelfactory: ViewModelProvider.Factory

    @Inject
    lateinit var itemAnimator: androidx.recyclerview.widget.DefaultItemAnimator

    @Inject
    lateinit var llm: androidx.recyclerview.widget.LinearLayoutManager

    private lateinit var summaryViewModel: SummaryViewModel

    private var fromPreset = false

    private var targetDataId = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivitySummaryBinding = DataBindingUtil.setContentView(this, R.layout.activity_summary)
        summaryViewModel = ViewModelProviders.of(this, viewmodelfactory)
                .get(SummaryViewModel::class.java)
        binding.viewmodel = summaryViewModel


        unpackExtra()

        if (savedInstanceState == null) summaryViewModel.start(fromPreset, targetDataId)

        initToolbar()
        initObservers()
        initRecyclerView()
    }


    private fun unpackExtra() {
        val extras = intent.extras ?: return
        fromPreset = extras.getBoolean(EXTRA_KEY_SUMMARY_IS_PRESET, false)
        targetDataId = extras.getLong(EXTRA_KEY_SUMMARY_ID, -1L)
    }


    private fun initToolbar() {
        setSupportActionBar(summary_toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }


    private fun initObservers() {
        summaryViewModel.snackbarResource.observe(this, Observer {
            showSnackbar(resources.getString(it ?: return@Observer))
        })
    }


    private fun initRecyclerView() {
        val summaryAdapter = SummaryAdapter(mutableListOf(), summaryViewModel)
        summary_recycler_view.setHasFixedSize(true)
        summary_recycler_view.itemAnimator = itemAnimator
        summary_recycler_view.layoutManager = llm
        summary_recycler_view.adapter = summaryAdapter
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun showSnackbar(s: String) {
        SnackbarUtil.showSnackBar(findViewById(android.R.id.content), s)
    }

}
