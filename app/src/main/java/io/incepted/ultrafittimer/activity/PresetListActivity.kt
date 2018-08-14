package io.incepted.ultrafittimer.activity

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import io.incepted.ultrafittimer.R
import io.incepted.ultrafittimer.adapter.PresetAdapter
import io.incepted.ultrafittimer.databinding.ActivityPresetListBinding
import io.incepted.ultrafittimer.util.PresetActionDialogFragment
import io.incepted.ultrafittimer.util.SnackbarUtil
import io.incepted.ultrafittimer.viewmodel.PresetListViewModel
import kotlinx.android.synthetic.main.activity_preset_list.*
import javax.inject.Inject

class PresetListActivity : AppCompatActivity() {

    @Inject
    lateinit var viewmodelfactory: ViewModelProvider.Factory

    @Inject
    lateinit var itemAnimator: DefaultItemAnimator

    @Inject
    lateinit var llm: LinearLayoutManager

    private lateinit var presetViewModel: PresetListViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityPresetListBinding = DataBindingUtil.setContentView(this, R.layout.activity_preset_list)
        presetViewModel = ViewModelProviders.of(this, viewmodelfactory)
                .get(PresetListViewModel::class.java)
        binding.viewmodel = presetViewModel

        if (savedInstanceState == null) presetViewModel.start()

        initToolbar()
        initSnackbar()
        initObservers()
        initRecyclerView()

    }

    private fun initToolbar() {
        setSupportActionBar(preset_list_toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initSnackbar() {
        presetViewModel.snackbarResource.observe(this, Observer {
            showSnackbar(resources.getString(it ?: return@Observer))
        })
    }

    private fun initObservers() {
        presetViewModel.presetActionEvent.observe(this, Observer {
            launchPresetActionDialog(it)
        })
    }

    private fun initRecyclerView() {

        val presetAdapter = PresetAdapter(mutableListOf(), presetViewModel)

        preset_list_recycler_view.setHasFixedSize(true)
        preset_list_recycler_view.itemAnimator = itemAnimator
        preset_list_recycler_view.layoutManager = llm
        preset_list_recycler_view.adapter = presetAdapter

    }


    private fun launchPresetActionDialog(presetId: Long?) {
        if (presetId != null) {
            val dialogFrag = PresetActionDialogFragment.newInstance(presetId)
            dialogFrag.show(supportFragmentManager, "preset_action_dialog")
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.preset_list_menu, menu)
        return true
    }

    private fun showSnackbar(s: String) {
        SnackbarUtil.showSnackBar(findViewById(android.R.id.content), s)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.menu_preset_list_add -> {
                //TODO Implement later
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


}
