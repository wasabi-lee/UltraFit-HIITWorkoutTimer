package io.incepted.ultrafittimer.di.module

import android.app.Application
import androidx.room.Room
import android.content.SharedPreferences
import android.preference.PreferenceManager
import dagger.Module
import dagger.Provides
import io.incepted.ultrafittimer.db.AppDatabase
import io.incepted.ultrafittimer.db.dao.PresetDao
import io.incepted.ultrafittimer.db.dao.TimerSettingDao
import io.incepted.ultrafittimer.db.dao.WorkoutHistoryDao
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

}