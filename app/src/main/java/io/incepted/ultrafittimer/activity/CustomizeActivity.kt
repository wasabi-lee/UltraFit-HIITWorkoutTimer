package io.incepted.ultrafittimer.activity

import android.app.DialogFragment
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import io.incepted.ultrafittimer.R
import io.incepted.ultrafittimer.adapter.RoundAdapter
import io.incepted.ultrafittimer.databinding.ActivityCustomizeBinding
import io.incepted.ultrafittimer.db.tempmodel.Round
import io.incepted.ultrafittimer.fragment.AlertDialogFragment
import io.incepted.ultrafittimer.viewmodel.CustomizeViewModel
import kotlinx.android.synthetic.main.activity_customize.*
import java.util.*
import javax.inject.Inject

class CustomizeActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_KEY_WORKOUT_DETAILS = "extra_key_workout_details"

        const val RESULT_KEY_WORKOUT_NAMES = "res_key_workout_names"
        const val RESULT_KEY_WORKOUT_WORKS = "res_key_workout_works"
        const val RESULT_KEY_WORKOUT_RESTS = "res_key_workout_rests"
        const val RESULT_KEY_WORKOUT_CUSTOMIZED = "res_key_workout_customized"
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var itemAnimator: DefaultItemAnimator

    @Inject
    lateinit var llm: LinearLayoutManager

    lateinit var customizeViewModel: CustomizeViewModel

    var originalList = mutableListOf<Round>()

    var resultList = mutableListOf<Round>()

    lateinit var roundAdapter: RoundAdapter

    lateinit var extra: ArrayList<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityCustomizeBinding = DataBindingUtil.setContentView(this, R.layout.activity_customize)
        customizeViewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(CustomizeViewModel::class.java)
        binding.viewmodel = customizeViewModel


        retrieveExtras()

        initToolbar()

        startViewModel(savedInstanceState)

        initRecyclerView()

        initObservers()


    }

    private fun retrieveExtras() {
        extra = intent.getStringArrayListExtra(EXTRA_KEY_WORKOUT_DETAILS)
    }

    private fun initToolbar() {
        setSupportActionBar(customize_toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun startViewModel(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) customizeViewModel.start()
    }

    private fun initRecyclerView() {

        originalList = customizeViewModel.initRounds(extra)
        val rounds = mutableListOf<Round>()
        rounds.addAll(originalList)

        roundAdapter = RoundAdapter(rounds, customizeViewModel)
        customize_recycler_view.setHasFixedSize(true)
        customize_recycler_view.itemAnimator = itemAnimator
        customize_recycler_view.layoutManager = llm
        customize_recycler_view.adapter = roundAdapter
    }

    private fun initObservers() {
        customizeViewModel.deletedItemPosition.observe(this, Observer {
            roundAdapter.removeAt(it ?: return@Observer)
        })

        customizeViewModel.backToMainWithResult.observe(this, Observer {
            backToMain(it)
        })
    }

    private fun backToMain(bundle: Bundle?) {
        if (bundle == null) return

        val returnIntent = Intent()
        returnIntent.putExtras(bundle)
        setResult(RESULT_OK, returnIntent)
        finish()

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.customize_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_customize_save -> {
                //Implement save function later
                customizeViewModel.organizeResult(originalList, roundAdapter.data)
                true
            }
            android.R.id.home -> {
                launchWarningDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun launchWarningDialog() {
        val dialogFrag = AlertDialogFragment.newInstance(R.string.alert_dialog_exit_warning_title)
        dialogFrag.show(supportFragmentManager, "warning_dialog")
    }

}
