package io.incepted.ultrafittimer.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.incepted.ultrafittimer.activity.*
import io.incepted.ultrafittimer.di.module.*

@Module
abstract class ActivityBuilder {

    @ContributesAndroidInjector(modules= [BaseActivityModule::class])
    abstract fun contributeBaseActivity(): BaseActivity

    @ContributesAndroidInjector(modules = [MainActivityModule::class])
    abstract fun contributeMainActivity(): MainActivity

    @ContributesAndroidInjector(modules = [SettingsActivityModule::class])
    abstract fun contributeSettingsActivity(): SettingsActivity

    @ContributesAndroidInjector(modules = [CustomizeActivityModule::class])
    abstract fun contributeCustomizeActivity(): CustomizeActivity

    @ContributesAndroidInjector(modules = [PresetListActivityModule::class])
    abstract fun contributePresetActivity(): PresetListActivity

    @ContributesAndroidInjector(modules = [SummaryActivityModule::class])
    abstract fun contributeSummaryActivity(): SummaryActivity

    @ContributesAndroidInjector(modules = [TimerActivityModule::class])
    abstract fun contributeTimerActivity(): TimerActivity

}