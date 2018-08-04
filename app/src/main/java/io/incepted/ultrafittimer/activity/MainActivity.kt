package io.incepted.ultrafittimer.activity

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.View
import io.incepted.ultrafittimer.R
import io.incepted.ultrafittimer.databinding.ActivityMainBinding
import io.incepted.ultrafittimer.util.SnackbarUtil
import io.incepted.ultrafittimer.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.main_bottom_sheet.*
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var mainViewModel: MainViewModel

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

        mainViewModel.toCustomizeActivity.observe(this, Observer { TODO("placeholder") })

        mainViewModel.toPresetActivity.observe(this, Observer { TODO("placeholder") })
    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    private fun showSnackBar(s: String) {
        SnackbarUtil.showSnackBar(findViewById(android.R.id.content), s)
    }

}
