package io.incepted.ultrafittimer.activity

import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.incepted.ultrafittimer.R
import io.incepted.ultrafittimer.databinding.ActivityTimerBinding
import io.incepted.ultrafittimer.timer.TimerService
import io.incepted.ultrafittimer.util.SnackbarUtil
import io.incepted.ultrafittimer.viewmodel.TimerViewModel
import timber.log.Timber
import javax.inject.Inject

class TimerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_KEY_FROM_PRESET = "extra_key_from_preset"
        const val EXTRA_KEY_ID = "extra_key_id"
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var timerViewModel: TimerViewModel

    private var fromPreset = false

    private var targetId = -1L

    private var timerService: TimerService? = null

    private var timerIntent: Intent? = null

    private var configChanged = false

    private var serviceBound = false

    private var serviceStarted = false

    lateinit var receiver: BroadcastReceiver


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityTimerBinding = DataBindingUtil.setContentView(this, R.layout.activity_timer)
        timerViewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(TimerViewModel::class.java)
        binding.viewmodel = timerViewModel

        unpackExtra()

        if (savedInstanceState == null) timerViewModel.start(fromPreset, targetId)
        else configChanged = true

        initReceiver()

        initObservers()
    }


    override fun onStart() {
        super.onStart()
        Timber.d("trker: onStart")
        initService(null)
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, IntentFilter(TimerService.BR_ACTION_TIMER_RESULT))
    }


    private fun unpackExtra() {
        fromPreset = intent.extras?.getBoolean(EXTRA_KEY_FROM_PRESET) ?: fromPreset
        targetId = intent.extras?.getLong(EXTRA_KEY_ID) ?: targetId
    }

    private fun initReceiver() {
        receiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                timerViewModel.updateTime(intent)
            }
        }
    }


    private fun initObservers() {
        timerViewModel.snackbarResource.observe(this, Observer {
            if (it == null) return@Observer
            showSnackBar(resources.getString(it))
        })

        timerViewModel.onTimerReady.observe(this, Observer {
            initService(it)
        })

        timerViewModel.resumePause.observe(this, Observer {
            timerService?.resumePauseTimer()
        })

        timerViewModel.terminateTimer.observe(this, Observer {
            timerService?.terminateTimer()
            finish()
        })
    }


    override fun onResume() {
        super.onResume()
        Timber.d("trker: OnResume")
    }


    private fun initService(bundle: Bundle?) {
        // Check if the param bundle and viewmodel bundle are all null.
        // If true, return. If false, init service with the bundle.
        val extras: Bundle = (bundle ?: timerViewModel.onTimerReady.value) ?: return

        if (timerIntent == null) {
            timerIntent = Intent(this, TimerService::class.java)
            timerIntent?.putExtras(extras)
        }

        bindService(timerIntent, serviceConn, Context.BIND_AUTO_CREATE)

        if (!configChanged && !serviceStarted) {
            serviceStarted = true
            Timber.d("Start service!")
            startService(timerIntent)
        }
    }


    private val serviceConn = object : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {
            Timber.d("trker: Service disconnected")
            serviceBound = false
        }

        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            Timber.d("trker: Service connected")
            val binder = p1 as TimerService.TimerServiceBinder
            timerService = binder.getService()
            serviceBound = true
        }
    }


    private fun showSnackBar(s: String) {
        SnackbarUtil.showSnackBar(findViewById(android.R.id.content), s)
    }


    override fun onStop() {
        super.onStop()
        if (serviceBound) {
            unbindService(serviceConn)
            serviceBound = false
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }

}
