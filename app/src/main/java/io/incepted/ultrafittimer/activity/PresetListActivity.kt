package io.incepted.ultrafittimer.activity

import android.content.Intent
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.databinding.DataBindingUtil
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import io.incepted.ultrafittimer.R
import io.incepted.ultrafittimer.adapter.PresetAdapter
import io.incepted.ultrafittimer.databinding.ActivityPresetListBinding
import io.incepted.ultrafittimer.fragment.PresetActionDialogFragment
import io.incepted.ultrafittimer.util.SnackbarUtil
import io.incepted.ultrafittimer.viewmodel.PresetListViewModel
import kotlinx.android.synthetic.main.activity_preset_list.*
import javax.inject.Inject

class PresetListActivity : AppCompatActivity() {

    @Inject
    lateinit var viewmodelfactory: ViewModelProvider.Factory

    @Inject
    lateinit var itemAnimator: androidx.recyclerview.widget.DefaultItemAnimator

    @Inject
    lateinit var llm: androidx.recyclerview.widget.LinearLayoutManager

    private lateinit var presetViewModel: PresetListViewModel

    companion object {
        const val RC_PRESET_EDITED = 4334
        const val EXTRA_KEY_PRESET_EDITED = "extra_key_preset_edited"
    }


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

        presetViewModel.openEditScreen.observe(this, Observer {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra(MainActivity.EXTRA_KEY_EDIT_MODE, true)
            intent.putExtra(MainActivity.EXTRA_KEY_EDIT_PRESET_ID, it)
            startActivityForResult(intent, RC_PRESET_EDITED)
        })

        presetViewModel.openSummaryActivity.observe(this, Observer {
            val intent = Intent(this, SummaryActivity::class.java)
            intent.putExtras(it)
            startActivity(intent)
        })

        presetViewModel.finishActivity.observe(this, Observer {
            finish()
        })

        presetViewModel.openTimerActivity.observe(this, Observer {
            val intent = Intent(this, TimerActivity::class.java)
            intent.putExtras(it)
            startActivity(intent)
        })
    }

    private fun initRecyclerView() {

        val presetAdapter = PresetAdapter(mutableListOf(), presetViewModel)

        preset_list_recycler_view.setHasFixedSize(true)
        preset_list_recycler_view.itemAnimator = itemAnimator
        preset_list_recycler_view.layoutManager = llm
        preset_list_recycler_view.adapter = presetAdapter

    }


    private fun launchPresetActionDialog(presetPosition: Int?) {
        if (presetPosition != null) {
            val dialogFrag = PresetActionDialogFragment.newInstance(presetPosition)
            dialogFrag.show(supportFragmentManager, "preset_action_dialog")
        }
    }

    fun bookmarkItem(presetPosition: Int) {
        presetViewModel.bookmarkItem(presetPosition)
    }

    fun editItem(presetPosition: Int) {
        presetViewModel.editItem(presetPosition)
    }

    fun showPresetDetail(presetPosition: Int) {
        presetViewModel.showPresetDetail(presetPosition)
    }

    fun deleteItem(presetPosition: Int) {
        presetViewModel.deleteItem(presetPosition)
    }

    fun playPreset(presetPosition: Int) {
        presetViewModel.playPreset(presetPosition)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        presetViewModel.handleActivityResult(requestCode, resultCode, data)
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
