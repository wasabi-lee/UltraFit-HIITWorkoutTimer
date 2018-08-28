package io.incepted.ultrafittimer.di

import android.app.Application
import android.content.Context
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import io.incepted.ultrafittimer.UltraFitApp
import io.incepted.ultrafittimer.di.module.AppModule
import io.incepted.ultrafittimer.timer.TimerService
import io.incepted.ultrafittimer.view.CustomPrefCategory
import javax.inject.Singleton

@Singleton
@Component(modules = [AndroidInjectionModule::class,
    AppModule::class,
    ActivityBuilder::class,
    ServiceBuilder::class])

interface AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        @BindsInstance
        fun appContext(appContext: Context): Builder

        fun build(): AppComponent
    }

    fun inject(ultraFitApp: UltraFitApp)
    fun inject(prefCategory: CustomPrefCategory)
    fun inject(timerService: TimerService)
}