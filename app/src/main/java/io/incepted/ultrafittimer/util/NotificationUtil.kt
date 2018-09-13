package io.incepted.ultrafittimer.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import io.incepted.ultrafittimer.R
import io.incepted.ultrafittimer.activity.MainActivity
import io.incepted.ultrafittimer.activity.TimerActivity
import io.incepted.ultrafittimer.timer.TickInfo
import io.incepted.ultrafittimer.timer.TimerService

class NotificationUtil(val context: Context) : ContextWrapper(context) {

    companion object {
        const val CHANNEL_ID_TIMER = "io.incepted.ultrafittimer.TIMER"
        const val CHANNEL_NAME_TIMER = "Timer Notifications"

        const val ACTION_LABEL_TIMER_PAUSE = "PAUSE"
        const val ACTION_LABEL_TIMER_RESUME = "RESUME"
        const val ACTION_LABEL_TIMER_DISMISS = "DISMISS"

        const val ACTION_INTENT_FILTER_RESUME = "io.incepted.ultrafittimer.ACTION_RESUME"
        const val ACTION_INTENT_FILTER_PAUSE = "io.incepted.ultrafittimer.ACTION_PAUSE"
        const val ACTION_INTENT_FILTER_DISMISS = "io.incepted.ultrafittimer.ACTION_DISMISS"

    }

    private var mManager: NotificationManager? = null

    private var timerPendingIntent: PendingIntent = PendingIntent.getActivity(context, 0,
            Intent(context, TimerActivity::class.java), 0)

    private var resumeIntent: PendingIntent
    private var pauseIntent: PendingIntent
    private var dismissIntent: PendingIntent

    private var workoutNotifBuilder: NotificationCompat.Builder

    init {


        resumeIntent = PendingIntent.getBroadcast(context, 4,
                Intent(ACTION_INTENT_FILTER_RESUME),
                PendingIntent.FLAG_CANCEL_CURRENT)

        pauseIntent = PendingIntent.getBroadcast(context, 5,
                Intent(ACTION_INTENT_FILTER_PAUSE),
                PendingIntent.FLAG_CANCEL_CURRENT)


        dismissIntent = PendingIntent.getBroadcast(context, 6,
                Intent(ACTION_INTENT_FILTER_DISMISS),
                PendingIntent.FLAG_CANCEL_CURRENT)

        workoutNotifBuilder = NotificationCompat.Builder(context, CHANNEL_ID_TIMER)
                .setSmallIcon(R.drawable.ic_timer_18_white)
                .setAutoCancel(false)
                .addAction(getDismissAction())
                .addAction(getResumePauseAction(false))
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(timerPendingIntent)


        createChannels()
    }


    private fun createChannels() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timerChannel = NotificationChannel(CHANNEL_ID_TIMER,
                    CHANNEL_NAME_TIMER, NotificationManager.IMPORTANCE_DEFAULT)
            timerChannel.enableLights(false)
            timerChannel.setSound(null, null)
            timerChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            getManager().createNotificationChannel(timerChannel)
        }

    }


    fun getCompleteNotification(): Notification {

        val contentIntent = PendingIntent.getActivity(context, 1, Intent(context, TimerActivity::class.java), 0)

        val completeNotifBuilder = NotificationCompat.Builder(context, CHANNEL_ID_TIMER)
                .setSmallIcon(R.drawable.ic_timer_18_white)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setContentTitle("All Done!")
                .setContentText("Workout Complete")
                .setContentIntent(contentIntent)

        return completeNotifBuilder.build()
    }


    fun getTimerNotificationToggleResumePause(paused: Boolean): Notification {
        workoutNotifBuilder.mActions[1] = getResumePauseAction(paused)
        return workoutNotifBuilder.build()
    }


    fun getTimerNotification(tickInfo: TickInfo?): Notification {
        if (tickInfo != null) {
            val notifTitle = "${tickInfo.workoutName} - ${tickInfo.roundCount}/${tickInfo.totalRounds}"
            val notifText = TimerUtil.secondsToTimeString(tickInfo.remianingSecs.toInt())
            workoutNotifBuilder
                    .setContentTitle(notifTitle)
                    .setContentText(notifText)
        }
        return workoutNotifBuilder.build()
    }


    private fun getResumePauseAction(paused: Boolean): NotificationCompat.Action {
        val label: String
        val icon: Int
        val intent: PendingIntent
        if (paused) {
            label = ACTION_LABEL_TIMER_RESUME
            icon = R.drawable.ic_play_arrow_36_black
            intent = resumeIntent
        } else {
            label = ACTION_LABEL_TIMER_PAUSE
            icon = R.drawable.ic_pause_36_black
            intent = pauseIntent
        }
        return NotificationCompat.Action.Builder(icon, label, intent).build()
    }


    private fun getDismissAction(): NotificationCompat.Action {
        return NotificationCompat.Action.Builder(R.drawable.ic_close_36_black,
                ACTION_LABEL_TIMER_DISMISS, dismissIntent).build()
    }


    fun getManager(): NotificationManager {
        if (mManager == null)
            mManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return mManager as NotificationManager
    }


}