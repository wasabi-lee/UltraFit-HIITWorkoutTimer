package io.incepted.ultrafittimer.ui

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.argumentCaptor
import io.incepted.ultrafittimer.db.DbRepository
import io.incepted.ultrafittimer.db.model.Preset
import io.incepted.ultrafittimer.db.source.LocalDataSource
import io.incepted.ultrafittimer.viewmodel.MainViewModel
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito
import org.mockito.Captor
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import kotlin.properties.Delegates

@RunWith(JUnit4::class)
class MainViewModelTest {

    private fun <T> any(): T {
        Mockito.any<T>()
        return uninitialized()
    }

    private fun <T> uninitialized(): T = null as T

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private val repository = mock(DbRepository::class.java)
    private val context = mock(Application::class.java)
    private val viewmodel = MainViewModel(context, repository)



    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }


    @Test
    fun testNull() {
        assertThat(viewmodel, notNullValue())
    }

    @Test
    fun testLoadPreset_valid() {
        viewmodel.loadPreset(1L)

        val captor =
                argumentCaptor<LocalDataSource.OnPresetLoadedListener>()

        verify(repository).getPresetById(1L, captor.capture())

        captor.firstValue.onPresetLoaded(Preset(1L, false, "TITLE", 1L))

        assertThat(viewmodel.preset, notNullValue())
        assertTrue(viewmodel.fromPreset)
        assertThat(viewmodel.presetName.get(), equalTo("TITLE"))
    }
}