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

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mainViewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(MainViewModel::class.java)
        binding.viewmodel = mainViewModel

        initToolbar()
        initBottomSheet()
        initObservers()
        initActivityTransitionObservers()

    }

    private fun initToolbar() {
        setSupportActionBar(main_toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun initBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet_container)
        bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                /* empty */
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                main_bottom_sheet_arrow_indicator_iv
                        .animate()
                        .setDuration(100)
                        .rotation(if (newState == BottomSheetBehavior.STATE_EXPANDED) 180F else 0F)
                        .start()
            }
        })
    }

    private fun initObservers() {
        mainViewModel.bottomSheetToggle.observe(this, Observer { toggleBottomSheet() })
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


    private fun toggleBottomSheet() {
        bottomSheetBehavior.state =
                when (bottomSheetBehavior.state) {
                    BottomSheetBehavior.STATE_EXPANDED -> BottomSheetBehavior.STATE_COLLAPSED
                    BottomSheetBehavior.STATE_COLLAPSED -> BottomSheetBehavior.STATE_EXPANDED
                    else -> bottomSheetBehavior.state
                }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    private fun showSnackBar(s: String) {
        SnackbarUtil.showSnackBar(findViewById(android.R.id.content), s)
    }

}
