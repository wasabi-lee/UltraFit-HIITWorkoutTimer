package io.incepted.ultrafittimer.di.module

import dagger.Module
import dagger.Provides
import java.util.*

@Module
class MainActivityModule {

    @Provides
    fun provideRandom() : Random {
        return Random()
    }
}