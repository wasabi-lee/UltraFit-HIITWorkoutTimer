package io.incepted.ultrafittimer.di.module

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.os.PowerManager
import dagger.Module
import dagger.Provides
import io.incepted.ultrafittimer.timer.BeepHelper
import io.incepted.ultrafittimer.util.NotificationUtil
import javax.inject.Singleton

@Module
class TimerServiceModule {

    @Provides
    fun provideBeepHelper(context: Context, sharedPref: SharedPreferences): BeepHelper {
        return BeepHelper(context, sharedPref)
    }

    @Provides
    fun provideNotificationUtil(context: Context): NotificationUtil {
        return NotificationUtil(context)
    }

    @Provides
    fun provideNotificationManager(app: Application): NotificationManager {
        return app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    @Provides
    fun providePowerManager(app:Application): PowerManager {
        return app.getSystemService(Context.POWER_SERVICE) as PowerManager
    }

    @Provides
    fun provideWakelock(powerManager: PowerManager): PowerManager.WakeLock {
        return powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "io.incepted.ultrafittimer::WakeLogTag")
    }


}