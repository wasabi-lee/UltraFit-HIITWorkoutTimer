package io.incepted.ultrafittimer.ui

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.*
import io.incepted.ultrafittimer.db.DbDelimiter
import io.incepted.ultrafittimer.db.DbRepository
import io.incepted.ultrafittimer.db.model.Preset
import io.incepted.ultrafittimer.db.model.TimerSetting
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
import org.mockito.MockitoAnnotations

@RunWith(JUnit4::class)
class MainViewModelTest {

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private val repository = mock<DbRepository>()
    private val context = mock<Application>()
    private val viewmodel = MainViewModel(context, repository)

    private val testTimer = TimerSetting(1L, 30,
            "3${DbDelimiter.DELIMITER}2",
            "20${DbDelimiter.DELIMITER}20",
            "30${DbDelimiter.DELIMITER}20",
            3, false)


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

        // load preset
        viewmodel.loadPreset(1)

        val presetCaptor =
                argumentCaptor<LocalDataSource.OnPresetLoadedListener>()

        // is the repository call triggered
        verify(repository).getPresetById(eq(1), presetCaptor.capture())

        // trigger the callback
        presetCaptor.firstValue.onPresetLoaded(Preset(1L, false, "TITLE", 1L))

        // is the result set to member variables
        assertThat(viewmodel.preset, notNullValue())
        assertTrue(viewmodel.fromPreset)
        assertThat(viewmodel.presetName.get(), equalTo("TITLE"))

        val timerCaptor =
                argumentCaptor<LocalDataSource.OnTimerLoadedListener>()

        // is the repository call triggered (chain method call)
        verify(repository).getTimerById(any(), timerCaptor.capture())

        // trigger the callback
        timerCaptor.firstValue.onTimerLoaded(testTimer)

        // is the result set to member variables
        assertThat(viewmodel.timer, notNullValue())
        assertThat(viewmodel.timerObsvb, notNullValue())


    }
}