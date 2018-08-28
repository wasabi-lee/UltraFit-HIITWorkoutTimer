package io.incepted.ultrafittimer.di.module

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import io.incepted.ultrafittimer.timer.BeepHelper

@Module
class TimerServiceModule {

    @Provides
    fun provideBeepHelper(context: Context, sharedPref: SharedPreferences): BeepHelper {
        return BeepHelper(context, sharedPref)
    }

}