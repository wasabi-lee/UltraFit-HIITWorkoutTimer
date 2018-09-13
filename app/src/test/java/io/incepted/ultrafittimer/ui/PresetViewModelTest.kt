package io.incepted.ultrafittimer.ui

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import io.incepted.ultrafittimer.db.DbRepository
import io.incepted.ultrafittimer.db.source.LocalDataSource
import io.incepted.ultrafittimer.viewmodel.PresetListViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations


@RunWith(JUnit4::class)
class PresetViewModelTest {

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private val repository = mock<DbRepository>()
    private val context = mock<Application>()
    private val viewmodel = PresetListViewModel(context, repository)


    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }


    @Test
    fun testLoadPresets_valid() {
        viewmodel.loadPresets()

        val captor =
                argumentCaptor<LocalDataSource.OnPresetsLoadedListener>()

        verify(repository).getPresets(captor.capture())


    }
}

