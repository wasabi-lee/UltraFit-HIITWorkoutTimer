package io.incepted.ultrafittimer.activity

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import android.content.Intent
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.*
import android.widget.Toast
import io.incepted.ultrafittimer.R
import io.incepted.ultrafittimer.databinding.ActivityMainBinding
import io.incepted.ultrafittimer.fragment.PresetSaveDialogFragment
import io.incepted.ultrafittimer.util.SnackbarUtil
import io.incepted.ultrafittimer.viewmodel.MainViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class MainActivity : AppCompatActivity() {


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var mainViewModel: MainViewModel

    private var exit: Boolean = false

    private var editMode: Boolean = false

    private var editPresetId: Long = -1L

    companion object {
        const val RC_CUSTOMIZED = 1001
        const val EXTRA_KEY_EDIT_MODE = "extra_key_edit_mode"
        const val EXTRA_KEY_EDIT_PRESET_ID = "extra_key_edit_preset_id"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mainViewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(MainViewModel::class.java)
        binding.viewmodel = mainViewModel

        unpackExtra()

        if (savedInstanceState == null) mainViewModel.start(editMode, editPresetId)

        initToolbar()
        initObservers()
        initActivityTransitionObservers()

    }

    private fun unpackExtra() {
        editMode = intent.getBooleanExtra(EXTRA_KEY_EDIT_MODE, false)
        editPresetId = intent.getLongExtra(EXTRA_KEY_EDIT_PRESET_ID, -1L)
    }

    private fun initToolbar() {
        setSupportActionBar(main_toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        if (editMode) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
    }


    private fun initObservers() {
        mainViewModel.snackbarTextRes.observe(this, Observer {
            if (it != null) showSnackBar(resources.getString(it))
        })
    }

    private fun initActivityTransitionObservers() {
        mainViewModel.toSettings.observe(this, Observer {
            if (it == false) return@Observer
            val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
        })

        mainViewModel.toCustomizeActivity.observe(this, Observer {
            if (it == null) return@Observer
            val intent = Intent(this, CustomizeActivity::class.java)
            intent.putStringArrayListExtra(CustomizeActivity.EXTRA_KEY_WORKOUT_DETAILS, it)
            startActivityForResult(intent, RC_CUSTOMIZED)
        })

        mainViewModel.toPresetActivity.observe(this, Observer {
            if (it == false) return@Observer
            val intent = Intent(this, PresetListActivity::class.java)
            startActivity(intent)
        })
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuId = if(editMode) R.menu.menu_main_edit else R.menu.menu_main
        menuInflater.inflate(menuId, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_main_save_preset -> {
                launchSaveDialog()
                true
            }
            R.id.menu_main_load_presets -> {
                mainViewModel.openPresetActivity()
                true
            }
            R.id.menu_main_settings -> {
                mainViewModel.openSettings()
                true
            }
            R.id.menu_main_edit_save -> {
                //TODO Save the changed preset
                true
            }
            android.R.id.home -> {
                onBackPressed()
                true            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showSnackBar(s: String) {
        SnackbarUtil.showSnackBar(findViewById(android.R.id.content), s)
    }


    private fun launchSaveDialog() {
        val dialogFrag = PresetSaveDialogFragment.newInstance()
        dialogFrag.show(supportFragmentManager, "preset_dialog")
    }


    fun savePreset(presetName: String) {
        mainViewModel.saveThisAsPreset(presetName)
    }


    override fun onBackPressed() {
        if (!editMode) {
            if (!exit) {
                Toast.makeText(this, "Press the back button again to exit", Toast.LENGTH_SHORT).show()
                exit = true
                Observable.timer(2, TimeUnit.SECONDS)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeBy(onComplete = { exit = false })
            } else {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mainViewModel.handleActivityResult(requestCode, resultCode, data)
    }
}
