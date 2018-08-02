package io.incepted.ultrafittimer.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.incepted.ultrafittimer.activity.MainActivity
import io.incepted.ultrafittimer.di.module.MainActivityModule

@Module
abstract class ActivityBuilder {
    @ContributesAndroidInjector(modules = [MainActivityModule::class])
    abstract fun contributeMainActivity() : MainActivity
}