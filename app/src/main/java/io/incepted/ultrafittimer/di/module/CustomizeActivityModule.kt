package io.incepted.ultrafittimer.di.module

import android.content.Context
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.Module
import dagger.Provides
import io.incepted.ultrafittimer.util.SwipeDeleteCallback

@Module
class CustomizeActivityModule {

    @Provides
    fun provideItemAnimator(): androidx.recyclerview.widget.DefaultItemAnimator {
        return androidx.recyclerview.widget.DefaultItemAnimator()
    }

    @Provides
    fun provideLinearLayoutManager(context: Context): androidx.recyclerview.widget.LinearLayoutManager {
        return androidx.recyclerview.widget.LinearLayoutManager(context)
    }

}