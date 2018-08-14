package io.incepted.ultrafittimer.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.incepted.ultrafittimer.activity.CustomizeActivity
import io.incepted.ultrafittimer.activity.MainActivity
import io.incepted.ultrafittimer.activity.PresetListActivity
import io.incepted.ultrafittimer.activity.SettingsActivity
import io.incepted.ultrafittimer.di.module.CustomizeActivityModule
import io.incepted.ultrafittimer.di.module.MainActivityModule
import io.incepted.ultrafittimer.di.module.PresetListActivityModule
import io.incepted.ultrafittimer.di.module.SettingsActivityModule

@Module
abstract class ActivityBuilder {

    @ContributesAndroidInjector(modules = [MainActivityModule::class])
    abstract fun contributeMainActivity() : MainActivity

    @ContributesAndroidInjector(modules = [SettingsActivityModule::class])
    abstract fun contributeSettingsActivity() : SettingsActivity

    @ContributesAndroidInjector(modules = [CustomizeActivityModule::class])
    abstract fun contributeCustomizeActivity() : CustomizeActivity

    @ContributesAndroidInjector(modules = [PresetListActivityModule::class])
    abstract fun contributePresetActivity() : PresetListActivity
}