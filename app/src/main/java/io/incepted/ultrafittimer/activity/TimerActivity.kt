package io.incepted.ultrafittimer.activity

import android.content.*
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.gelitenight.waveview.library.WaveView
import io.incepted.ultrafittimer.R
import io.incepted.ultrafittimer.databinding.ActivityTimerBinding
import io.incepted.ultrafittimer.fragment.TimerExitDialogFragment
import io.incepted.ultrafittimer.timer.TimerService
import io.incepted.ultrafittimer.util.SnackbarUtil
import io.incepted.ultrafittimer.view.WaveHelper
import io.incepted.ultrafittimer.viewmodel.TimerViewModel
import kotlinx.android.synthetic.main.activity_timer.*
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

    lateinit var waveHelper: WaveHelper



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityTimerBinding = DataBindingUtil.setContentView(this, R.layout.activity_timer)
        timerViewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(TimerViewModel::class.java)
        binding.viewmodel = timerViewModel

        unpackExtra()

        waveHelper = WaveHelper(timer_wave)
        timer_wave.waterLevelRatio = 0.0f
        timer_wave.setShapeType(WaveView.ShapeType.SQUARE)

        if (savedInstanceState == null) timerViewModel.start()
        else configChanged = true

        initReceiver()

        initObservers()
    }


    override fun onStart() {
        super.onStart()
        Timber.d("trker: onStart")

        initService()

        registerLocalBroadcastReceiver()
    }

    override fun onResume() {
        super.onResume()
        waveHelper.start()
    }

    override fun onPause() {
        super.onPause()
        waveHelper.cancel()
    }

    private fun registerLocalBroadcastReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(TimerService.BR_ACTION_TIMER_TICK_RESULT)
        intentFilter.addAction(TimerService.BR_ACTION_TIMER_COMPLETED_RESULT)
        intentFilter.addAction(TimerService.BR_ACTION_TIMER_RESUME_PAUSE_STATE)
        intentFilter.addAction(TimerService.BR_ACTION_TIMER_TERMINATED)
        intentFilter.addAction(TimerService.BR_ACTION_TIMER_SESSION_SWITCH)
        intentFilter.addAction(TimerService.BR_ACTION_TIMER_ERROR)
        LocalBroadcastManager
                .getInstance(this)
                .registerReceiver(receiver, intentFilter)
    }


    private fun unpackExtra() {
        fromPreset = intent.extras?.getBoolean(EXTRA_KEY_FROM_PRESET) ?: fromPreset
        targetId = intent.extras?.getLong(EXTRA_KEY_ID) ?: targetId
    }

    private fun initReceiver() {
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                timerViewModel.handleBroadcastResult(intent)
            }
        }
    }


    private fun initObservers() {
        timerViewModel.snackbarResource.observe(this, Observer {
            if (it == null) return@Observer
            showSnackBar(resources.getString(it))
        })

        timerViewModel.resumePause.observe(this, Observer {
            timerService?.resumePauseTimer()
        })

        timerViewModel.exitTimer.observe(this, Observer {
            onBackPressed()
        })

        timerViewModel.completeTimer.observe(this, Observer {
            Toast.makeText(this, "Workout Completed", Toast.LENGTH_SHORT).show()
        })

        timerViewModel.finishActivity.observe(this, Observer {
            finish()
        })

        timerViewModel.animateWave.observe(this, Observer {
            waveHelper.liftWave(it)
        })

        timerViewModel.resumePauseWave.observe(this, Observer {
            if (it) waveHelper.pauseWave()
            else waveHelper.resumeWave()
        })
    }


    fun exitTimer() {
        timerService?.terminateTimer()
        finish()
    }

    private fun finishService() {
        timerService?.finish()
    }


    private fun showWarningDialog() {
        val dialogFrag = TimerExitDialogFragment.newInstance()
        dialogFrag.show(supportFragmentManager, "timer_exit_dialog")
    }


    override fun onBackPressed() {
        if (timerService?.timerCompleted == false) showWarningDialog()
        else {
            finishService()
            this.finish()
        }
    }


    private fun initService() {
        // Check if the param bundle and viewmodel bundle are all null.
        // If true, return. If false, init service with the bundle.
        val bundle = Bundle()
        bundle.putBoolean(TimerService.BUNDLE_KEY_IS_PRESET, fromPreset)
        bundle.putLong(TimerService.BUNDLE_KEY_TARGET_ID, targetId)

        if (timerIntent == null) {
            timerIntent = Intent(this, TimerService::class.java)
            timerIntent?.putExtras(bundle)
        }

        bindService(timerIntent, serviceConn, Context.BIND_AUTO_CREATE)

        if (!configChanged && !TimerService.SERVICE_STARTED) {
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

            timerViewModel.setInitialValues(timerService?.lastTick, timerService?.isTimerPaused())
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
