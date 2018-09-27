package io.incepted.ultrafittimer.activity

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.incepted.ultrafittimer.R
import io.incepted.ultrafittimer.databinding.ActivityTimerBinding
import io.incepted.ultrafittimer.fragment.TimerExitDialogFragment
import io.incepted.ultrafittimer.timer.TimerCommunication
import io.incepted.ultrafittimer.timer.TimerService
import io.incepted.ultrafittimer.util.SnackbarUtil
import io.incepted.ultrafittimer.view.ProgressHelper
import io.incepted.ultrafittimer.view.WaveHelper
import io.incepted.ultrafittimer.viewmodel.TimerViewModel
import kotlinx.android.synthetic.main.activity_timer.*
import timber.log.Timber
import javax.inject.Inject
import android.content.Context.NOTIFICATION_SERVICE
import androidx.core.content.ContextCompat.getSystemService
import android.app.NotificationManager



class TimerActivity : BaseActivity() {

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

    private var serviceBound = false

    private lateinit var receiver: BroadcastReceiver

    lateinit var waveHelper: WaveHelper

    lateinit var progressHelper: ProgressHelper


    // ---------------------------------------------- Lifecycle --------------------------------------


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityTimerBinding = DataBindingUtil.setContentView(this, R.layout.activity_timer)
        timerViewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(TimerViewModel::class.java)
        binding.viewmodel = timerViewModel


        unpackExtra()

        if (savedInstanceState == null) timerViewModel.start()

        waveHelper = WaveHelper(timer_wave)
        progressHelper = ProgressHelper(timer_progress_bar)

        initReceiver()
        initObservers()
    }


    override fun onStart() {
        super.onStart()
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
        progressHelper.pauseProgressBar()
    }


    override fun onStop() {
        super.onStop()
        if (serviceBound) {
            unbindService(serviceConn)
            serviceBound = false
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }


    override fun onDestroy() {
        if (isFinishing)
            finishService()
        super.onDestroy()
    }


    // ---------------------------------------------- Initialization --------------------------------------


    private fun unpackExtra() {
        fromPreset = intent.extras?.getBoolean(EXTRA_KEY_FROM_PRESET) ?: fromPreset
        targetId = intent.extras?.getLong(EXTRA_KEY_ID) ?: targetId
    }


    private fun registerLocalBroadcastReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(TimerCommunication.BR_ACTION_TIMER_TICK_RESULT)
        intentFilter.addAction(TimerCommunication.BR_ACTION_TIMER_COMPLETED_RESULT)
        intentFilter.addAction(TimerCommunication.BR_ACTION_TIMER_RESUME_PAUSE_STATE)
        intentFilter.addAction(TimerCommunication.BR_ACTION_TIMER_TERMINATED)
        intentFilter.addAction(TimerCommunication.BR_ACTION_TIMER_SESSION_SWITCH)
        intentFilter.addAction(TimerCommunication.BR_ACTION_TIMER_ERROR)
        LocalBroadcastManager
                .getInstance(this)
                .registerReceiver(receiver, intentFilter)
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
            timerViewModel.completedWhileBound = true
            Toast.makeText(this, "Workout Completed", Toast.LENGTH_SHORT).show()
        })

        timerViewModel.finishActivity.observe(this, Observer {
            wrapUpActivity()
        })

        timerViewModel.animateWave.observe(this, Observer {
            waveHelper.liftWave(it)
            if (it.firstTick) {// the progressbar anim should be fired only once
                progressHelper.animateProgressBar(timerService?.timer?.totalTime,
                        timerService?.totalProgress, timerService?.isTimerPaused())
            }
        })

        timerViewModel.resumePauseWave.observe(this, Observer {
            if (it) {
                waveHelper.pauseWave()
                progressHelper.pauseProgressBar()
            } else {
                waveHelper.resumeWave()
                progressHelper.resumeProgressBar()
            }
        })
    }


    private fun initService() {

        // Only init Service when the timer hasn't been executed.

        val bundle = Bundle()
        bundle.putBoolean(TimerCommunication.BUNDLE_KEY_IS_PRESET, fromPreset)
        bundle.putLong(TimerCommunication.BUNDLE_KEY_TARGET_ID, targetId)

        if (timerIntent == null) {
            timerIntent = Intent(this, TimerService::class.java)
            timerIntent?.putExtras(bundle)
        }

        bindService(timerIntent, serviceConn, Context.BIND_AUTO_CREATE)

        if (!TimerService.SERVICE_STARTED) {
            startService(timerIntent)
        }
    }


    // ---------------------------------------------- User Interaction --------------------------------------


    override fun onBackPressed() {
        if (timerService?.timerTerminated == false) showWarningDialog()
        else wrapUpActivity()

    }


    private fun showWarningDialog() {
        val dialogFrag = TimerExitDialogFragment.newInstance()
        dialogFrag.show(supportFragmentManager, "timer_exit_dialog")
    }


    private fun finishService() {
        TimerService.STOP_SELF = true
        TimerService.SERVICE_STARTED = false
        stopService(timerIntent)
    }


    fun exitTimer() {
        // let service finish itself after logging data to DB
        TimerService.STOP_SELF = true
        timerService?.terminateTimer(false)
        finish()
    }


    private fun wrapUpActivity() {
        finishService()
        cancelNotification()
        this.finish()
    }

    private fun cancelNotification() {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(TimerCommunication.TIMER_NOTIFICATION_ID)
        notificationManager.cancel(TimerCommunication.TIMER_COMPLETE_NOTIFICATION_ID)
    }


    // ---------------------------------------------- Service Configuration --------------------------------------


    private val serviceConn = object : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {
            serviceBound = false
        }

        override fun onServiceConnected(componentName: ComponentName?, binder: IBinder?) {
            setupBoundService(binder)
            checkIfTimerTerminated()
            restoreUIState()
        }
    }


    private fun setupBoundService(binder: IBinder?) {
        val b = binder as TimerService.TimerServiceBinder
        timerService = b.getService()
        serviceBound = true
        TimerService.STOP_SELF = false
    }


    private fun checkIfTimerTerminated() {
        // Finish service if the timer was already completed while the activity was not bound.
        if (timerService?.timerTerminated == true && !timerViewModel.completedWhileBound)
            wrapUpActivity()

    }


    private fun restoreUIState() {
        Timber.d("Progress: restoreUiState")
        timerViewModel.setInitialValues(timerService?.lastTick, timerService?.isTimerPaused())
        waveHelper.setAnimState(timerService?.lastTick, timerService?.isTimerPaused())
        progressHelper.animateProgressBar(timerService?.timer?.totalTime,
                timerService?.totalProgress, timerService?.isTimerPaused())
    }


    private fun showSnackBar(s: String) {
        SnackbarUtil.showSnackBar(findViewById(android.R.id.content), s)
    }


}
