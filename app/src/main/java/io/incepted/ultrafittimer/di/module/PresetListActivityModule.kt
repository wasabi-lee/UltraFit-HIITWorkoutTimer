package io.incepted.ultrafittimer.di.module

import android.content.Context
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import dagger.Module
import dagger.Provides

@Module
class PresetListActivityModule {

    @Provides
    fun provideItemAnimator(): DefaultItemAnimator {
        return DefaultItemAnimator()
    }

    @Provides
    fun provideLinearLayoutManager(context: Context): LinearLayoutManager {
        return LinearLayoutManager(context)
    }

}