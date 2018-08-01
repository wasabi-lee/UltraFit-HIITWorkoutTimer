package io.incepted.ultrafittimer.activity

import android.arch.lifecycle.ViewModelProviders
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.view.Menu
import io.incepted.ultrafittimer.R
import io.incepted.ultrafittimer.util.Injection
import io.incepted.ultrafittimer.util.ViewModelFactory
import io.incepted.ultrafittimer.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel : MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(main_toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        viewModel = obtainViewModel(this)
    }

    private fun obtainViewModel(fragmentActivity: FragmentActivity) : MainViewModel{
        val factory : ViewModelFactory = Injection.provideViewModelFactory(fragmentActivity.application)
        return ViewModelProviders.of(fragmentActivity, factory).get(MainViewModel::class.java)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

}
