package io.incepted.ultrafittimer.di.module

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import androidx.room.Room
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dagger.Module
import dagger.Provides
import io.incepted.ultrafittimer.db.AppDatabase
import io.incepted.ultrafittimer.db.dao.PresetDao
import io.incepted.ultrafittimer.db.dao.TimerSettingDao
import io.incepted.ultrafittimer.db.dao.WorkoutHistoryDao
import io.incepted.ultrafittimer.util.NotificationUtil
import javax.inject.Singleton

@Module(includes = [ViewModelModule::class])
class AppModule {

    @Singleton
    @Provides
    fun provideSharedPreference(app: Application): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(app)
    }

    @Singleton
    @Provides
    fun provideDb(app: Application): AppDatabase {
        return Room
                .databaseBuilder(app, AppDatabase::class.java, "ultrafit_database.db")
                .fallbackToDestructiveMigration()
                .build()
    }

    @Singleton
    @Provides
    fun providePresetDao(db: AppDatabase): PresetDao {
        return db.presetDao()
    }

    @Singleton
    @Provides
    fun provideTimerSetting(db: AppDatabase): TimerSettingDao {
        return db.timerSettingDao()
    }

    @Singleton
    @Provides
    fun provideWorkoutHistoryDao(db: AppDatabase): WorkoutHistoryDao {
        return db.workoutHistoryDao()
    }

    @Singleton
    @Provides
    fun provideNotificationUtil(app: Application): NotificationUtil {
        return NotificationUtil(app.applicationContext)
    }

    @Singleton
    @Provides
    fun provideLocalBroadcastManager(app: Application): LocalBroadcastManager {
        return LocalBroadcastManager.getInstance(app.applicationContext)
    }

    @Singleton
    @Provides
    fun provideNotificationManager(app: Application): NotificationManager {
        return app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

}