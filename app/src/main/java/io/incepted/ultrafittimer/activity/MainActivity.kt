package io.incepted.ultrafittimer.activity

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.*
import android.widget.Toast
import io.incepted.ultrafittimer.R
import io.incepted.ultrafittimer.databinding.ActivityMainBinding
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mainViewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(MainViewModel::class.java)
        binding.viewmodel = mainViewModel

        initToolbar()
        initObservers()
        initActivityTransitionObservers()

    }

    private fun initToolbar() {
        setSupportActionBar(main_toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }


    private fun initObservers() {
        mainViewModel.snackbarTextRes.observe(this, Observer {
            if (it != null) showSnackBar(resources.getString(it))
        })
    }

    private fun initActivityTransitionObservers() {
        mainViewModel.toSettings.observe(this, Observer {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        })

        mainViewModel.toCustomizeActivity.observe(this, Observer { Timber.d("to customize activity") })

        mainViewModel.toPresetActivity.observe(this, Observer { Timber.d("to preset activity") })
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_main_save_preset -> {
                mainViewModel.saveThisAsPreset()
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
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showSnackBar(s: String) {
        SnackbarUtil.showSnackBar(findViewById(android.R.id.content), s)
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.start()
    }

    override fun onBackPressed() {
        if (exit) {
            super.onBackPressed()
        } else {
            Toast.makeText(this, "Press the back button again to exit", Toast.LENGTH_SHORT).show()
            exit = true
            Observable.timer(2, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(onComplete = { exit = false })
        }
    }
}
