package io.incepted.ultrafittimer.di.module

import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import io.incepted.ultrafittimer.di.ViewModelKey
import io.incepted.ultrafittimer.util.ViewModelFactory
import io.incepted.ultrafittimer.viewmodel.CustomizeViewModel
import io.incepted.ultrafittimer.viewmodel.MainViewModel

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
    abstract fun bindCustomizeViewModel(customizeViewModel: CustomizeViewModel) : ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}