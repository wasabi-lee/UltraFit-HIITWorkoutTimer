package io.incepted.ultrafittimer.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.incepted.ultrafittimer.timer.TimerService

@Module
abstract class ServiceBuilder {
    @ContributesAndroidInjector
    abstract fun contributeTimerService(): TimerService
}