package io.incepted.ultrafittimer.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.incepted.ultrafittimer.activity.MainActivity
import io.incepted.ultrafittimer.activity.SettingsActivity
import io.incepted.ultrafittimer.di.module.MainActivityModule
import io.incepted.ultrafittimer.di.module.SettingsActivityModule

@Module
abstract class ActivityBuilder {

    @ContributesAndroidInjector(modules = [MainActivityModule::class])
    abstract fun contributeMainActivity() : MainActivity

    @ContributesAndroidInjector(modules = [SettingsActivityModule::class])
    abstract fun contributeSettingsActivity() : SettingsActivity
}