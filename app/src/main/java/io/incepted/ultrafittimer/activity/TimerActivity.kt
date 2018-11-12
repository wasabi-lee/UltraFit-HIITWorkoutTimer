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
import javax.inject.Inject
import android.app.NotificationManager
import io.reactivex.Single
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import java.util.concurrent.TimeUnit


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

        Timber.d("oncreate")

        unpackExtra()

        if (savedInstanceState == null) timerViewModel.start()

        waveHelper = WaveHelper(timer_wave)
        progressHelper = ProgressHelper(timer_progress_bar)

        initReceiver()
        initObservers()
    }


    override fun onStart() {
        super.onStart()
        Timber.d("onstart")

        // finish activity if the timer was already completed
        if (TimerService.TIMER_TERMINATED) {
            timerViewModel.showCompletedScreen()
            Single.timer(5, TimeUnit.SECONDS)
                    .subscribeBy {
                        finish()
                    }
        } else {
            initService()
            registerLocalBroadcastReceiver()
        }
    }


    override fun onResume() {
        super.onResume()
        Timber.d("onresume")
        waveHelper.start()
    }


    override fun onPause() {
        super.onPause()
        Timber.d("onpause")
        waveHelper.cancel()
        progressHelper.pauseProgressBar()
    }


    override fun onStop() {
        super.onStop()
        Timber.d("onStop")

        if (isFinishing)
            resetService()

        if (serviceBound) {
            unbindService(serviceConn)
            serviceBound = false
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)

    }


    override fun onDestroy() {
        Timber.d("ondestroy")
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
            if (it)
                Toast.makeText(applicationContext, "Workout Completed", Toast.LENGTH_SHORT).show()
        })

        timerViewModel.finishActivity.observe(this, Observer {
            finish()
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
        if (!TimerService.TIMER_TERMINATED)
            showWarningDialog()
        else
            finish()
    }


    private fun showWarningDialog() {
        val dialogFrag = TimerExitDialogFragment.newInstance()
        dialogFrag.show(supportFragmentManager, "timer_exit_dialog")
    }


    // called when the activity gets destroyed to reset the flags to the default values
    private fun resetService() {
        if (TimerService.SERVICE_STARTED) {
            // terminate the timer when the app is getting closed while the service is alive
            timerService?.terminateTimer(false)
        }
        TimerService.SERVICE_STARTED = false
        TimerService.TIMER_TERMINATED = false
        cancelNotification()
    }


    // Timer stopping method called from the dialog fragment by clicking the positive button
    fun stopTimerFromDialog() {
        // let service finish itself after logging data to DB
        timerService?.terminateTimer(false)
        finish()
    }


    private fun cancelNotification() {
        val notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
                        as NotificationManager
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
            restoreUIState()
        }
    }


    private fun setupBoundService(binder: IBinder?) {
        val b = binder as TimerService.TimerServiceBinder
        timerService = b.getService()
        serviceBound = true
    }


    private fun restoreUIState() {
        timerViewModel.setInitialValues(timerService?.lastTick, timerService?.isTimerPaused())
        waveHelper.setAnimState(timerService?.lastTick, timerService?.isTimerPaused())
        progressHelper.animateProgressBar(timerService?.timer?.totalTime,
                timerService?.totalProgress, timerService?.isTimerPaused())
    }


    private fun showSnackBar(s: String) {
        SnackbarUtil.showSnackBar(findViewById(android.R.id.content), s)
    }


}
