package io.incepted.ultrafittimer.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.incepted.ultrafittimer.di.module.TimerServiceModule
import io.incepted.ultrafittimer.timer.TimerService

@Module
abstract class ServiceBuilder {
    @ContributesAndroidInjector(modules = [TimerServiceModule::class])
    abstract fun contributeTimerService(): TimerService
}