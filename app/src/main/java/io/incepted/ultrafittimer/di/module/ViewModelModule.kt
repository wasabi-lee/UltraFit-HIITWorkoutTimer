package io.incepted.ultrafittimer.di.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import io.incepted.ultrafittimer.di.ViewModelKey
import io.incepted.ultrafittimer.util.ViewModelFactory
import io.incepted.ultrafittimer.viewmodel.*

@Suppress("unused")
@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun bindMainViewModel(mainViewModel: MainViewModel): ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(CustomizeViewModel::class)
    abstract fun bindCustomizeViewModel(customizeViewModel: CustomizeViewModel): ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(PresetListViewModel::class)
    abstract fun bindPresetListViewModel(presetListViewModel: PresetListViewModel): ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(SummaryViewModel::class)
    abstract fun bindSummaryViewModel(summaryViewModel: SummaryViewModel): ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(TimerViewModel::class)
    abstract fun bindTimerViewModel(timerViewModel: TimerViewModel): ViewModel


    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}