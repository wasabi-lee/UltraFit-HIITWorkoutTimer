package io.incepted.ultrafittimer.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.core.app.NotificationCompat
import io.incepted.ultrafittimer.R

class NotificationUtil(val context: Context) : ContextWrapper(context) {

    companion object {
        const val CHANNEL_ID_TIMER = "io.incepted.ultrafittimer.TIMER"
        const val CHANNEL_NAME_TIMER = "Timer Notifications"
    }

    private var mManager: NotificationManager? = null

    init {
        createChannels()
    }

    fun createChannels() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timerChannel = NotificationChannel(CHANNEL_ID_TIMER,
                    CHANNEL_NAME_TIMER, NotificationManager.IMPORTANCE_HIGH)
            timerChannel.enableLights(false)
            timerChannel.enableVibration(true)
            timerChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            getManager().createNotificationChannel(timerChannel)
        }

    }


    fun getTimerNotification(): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, CHANNEL_ID_TIMER)
                .setContentTitle("Workout Started")
                .setContentText("YAYYY")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setAutoCancel(false)
    }


    fun getManager(): NotificationManager {
        if (mManager == null)
            mManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return mManager as NotificationManager
    }

}