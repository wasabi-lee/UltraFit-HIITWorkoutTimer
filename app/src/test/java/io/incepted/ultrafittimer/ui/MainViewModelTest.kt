package io.incepted.ultrafittimer.ui

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.nhaarman.mockitokotlin2.*
import io.incepted.ultrafittimer.db.DbDelimiter
import io.incepted.ultrafittimer.db.DbRepository
import io.incepted.ultrafittimer.db.model.Preset
import io.incepted.ultrafittimer.db.model.TimerSetting
import io.incepted.ultrafittimer.db.model.TimerSettingObservable
import io.incepted.ultrafittimer.db.source.LocalDataSource
import io.incepted.ultrafittimer.util.TestUtils
import io.incepted.ultrafittimer.viewmodel.MainViewModel
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

@RunWith(JUnit4::class)
class MainViewModelTest {

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private val repository = mock<DbRepository>()
    private val context = mock<Application>()
    private val viewmodel = MainViewModel(context, repository)

    private val testTimer = TimerSetting(5L, 30,
            "3${DbDelimiter.DELIMITER}2",
            "20${DbDelimiter.DELIMITER}20",
            "30${DbDelimiter.DELIMITER}20",
            3, false)

    private val testPreset = Preset(3L, false, "TITLE", 1L)


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
        viewmodel.loadPreset(testPreset.id ?: 3L)

        val presetCaptor =
                argumentCaptor<LocalDataSource.OnPresetLoadedListener>()

        // is the repository call triggered
        verify(repository).getPresetById(eq(3), presetCaptor.capture())

        // trigger the callback
        presetCaptor.firstValue.onPresetLoaded(testPreset)

        // is the result set to member variables
        assertThat(viewmodel.preset, notNullValue())
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


    @Test
    fun testLoadPreset_error() {

        // setup snackbar observer
        val observer: Observer<Int> = mock()
        viewmodel.snackbarTextRes.observe(TestUtils.TEST_OBSERVER, observer)

        // load preset
        viewmodel.loadPreset(testPreset.id ?: 3L)

        val presetCaptor =
                argumentCaptor<LocalDataSource.OnPresetLoadedListener>()

        // is the repository call triggered
        verify(repository).getPresetById(eq(3), presetCaptor.capture())

        // trigger the callback
        presetCaptor.firstValue.onPresetNotAvailable()

        // verify if the snackbar is shown
        verify(observer, times(1)).onChanged(any())
    }


    @Test
    fun testLoadTimer_valid() {

        // load timer
        viewmodel.loadTimer(5)

        val timerCaptor =
                argumentCaptor<LocalDataSource.OnTimerLoadedListener>()

        // is the repository call triggered
        verify(repository).getTimerById(eq(5), timerCaptor.capture())

        // trigger the callback
        timerCaptor.firstValue.onTimerLoaded(testTimer)

        // is the repository call triggered (chain method call)
        verify(repository).getTimerById(any(), timerCaptor.capture())

        // is the result set to member variables
        assertThat(viewmodel.timer, notNullValue())
        assertThat(viewmodel.timerObsvb, notNullValue())

    }


    @Test
    fun testLoadTimer_error() {

        // setup snackbar observer
        val observer: Observer<Int> = mock()
        viewmodel.snackbarTextRes.observe(TestUtils.TEST_OBSERVER, observer)

        // load timer
        viewmodel.loadTimer(5)

        val timerCaptor =
                argumentCaptor<LocalDataSource.OnTimerLoadedListener>()

        // is the repository call triggered
        verify(repository).getTimerById(eq(5), timerCaptor.capture())

        // trigger the callback
        timerCaptor.firstValue.onTimerNotAvailable()

        // verify if the snackbar is shown
        verify(observer, times(1)).onChanged(any())
    }


    @Test
    fun testSavePreset_valid() {

        val observer: Observer<Int> = mock()
        viewmodel.snackbarTextRes.observe(TestUtils.TEST_OBSERVER, observer)

        val timerObrvb = mock<TimerSettingObservable>()
        whenever(timerObrvb.getFinalSetting()).thenReturn(testTimer)

        viewmodel.timerObsvb.set(timerObrvb)

        viewmodel.saveThisAsPreset("TEST_1")

        val timerCaptor = argumentCaptor<LocalDataSource.OnTimerSavedListener>()

        verify(repository).saveTimer(any(), timerCaptor.capture())

        assertTrue(viewmodel.presetSaveInProgress)

        timerCaptor.firstValue.onTimerSaved(testTimer.id ?: 5L)

        val presetCaptor = argumentCaptor<LocalDataSource.OnPresetSavedListener>()

        verify(repository).savePreset(any(), presetCaptor.capture())

        presetCaptor.firstValue.onPresetSaved(testTimer.id ?: 5L)

        assertFalse(viewmodel.presetSaveInProgress)

        verify(observer, times(1)).onChanged(any())
    }

    @Test
    fun testSavePreset_error() {
        val observer: Observer<Int> = mock()
        viewmodel.snackbarTextRes.observe(TestUtils.TEST_OBSERVER, observer)

        val timerObrvb = mock<TimerSettingObservable>()
        whenever(timerObrvb.getFinalSetting()).thenReturn(testTimer)

        viewmodel.timerObsvb.set(timerObrvb)

        viewmodel.saveThisAsPreset("TEST_1")

        val timerCaptor = argumentCaptor<LocalDataSource.OnTimerSavedListener>()

        verify(repository).saveTimer(any(), timerCaptor.capture())

        assertTrue(viewmodel.presetSaveInProgress)

        timerCaptor.firstValue.onTimerSaveNotAvailable()

        verify(observer, times(1)).onChanged(any())
    }



    @Test
    fun testTimerStartClicked_default_valid() {

        val observer: Observer<Bundle> = mock()
        viewmodel.toTimerActivity.observe(TestUtils.TEST_OBSERVER, observer)

        val timerObrvb = mock<TimerSettingObservable>()
        Mockito.doNothing().whenever(timerObrvb).finalizeDetail()
        whenever(timerObrvb.getFinalSetting()).thenReturn(testTimer)

        viewmodel.fromPreset = false
        viewmodel.fromTemp = false
        viewmodel.timerObsvb.set(timerObrvb)

        viewmodel.onTimerStartClicked()

        val timerCaptor = argumentCaptor<LocalDataSource.OnTimerSavedListener>()

        verify(repository).saveTimer(any(), timerCaptor.capture())

        timerCaptor.firstValue.onTimerSaved(testTimer.id ?: 5L)

        verify(observer).onChanged(any())

    }


    @Test
    fun testTimerStartClicked_default_error() {

        val observer: Observer<Int> = mock()
        viewmodel.snackbarTextRes.observe(TestUtils.TEST_OBSERVER, observer)

        val timerObrvb = mock<TimerSettingObservable>()
        Mockito.doNothing().whenever(timerObrvb).finalizeDetail()
        whenever(timerObrvb.getFinalSetting()).thenReturn(testTimer)

        viewmodel.fromPreset = false
        viewmodel.fromTemp = false
        viewmodel.timerObsvb.set(timerObrvb)

        viewmodel.onTimerStartClicked()

        val timerCaptor = argumentCaptor<LocalDataSource.OnTimerSavedListener>()

        verify(repository).saveTimer(any(), timerCaptor.capture())

        timerCaptor.firstValue.onTimerSaveNotAvailable()

        verify(observer).onChanged(any())

    }

    @Test
    fun testTimerStartClicked_fromPreset_valid() {

        val observer: Observer<Bundle> = mock()
        viewmodel.toTimerActivity.observe(TestUtils.TEST_OBSERVER, observer)

        val timerObrvb = mock<TimerSettingObservable>()
        Mockito.doNothing().whenever(timerObrvb).finalizeDetail()
        whenever(timerObrvb.getFinalSetting()).thenReturn(testTimer)

        viewmodel.fromPreset = true
        viewmodel.fromTemp = false
        viewmodel.timerObsvb.set(timerObrvb)
        viewmodel.preset = testPreset

        viewmodel.onTimerStartClicked()

        verify(observer).onChanged(any())

    }

    @Test
    fun testTimerStartClicked_fromTemp_valid() {

        val observer: Observer<Bundle> = mock()
        viewmodel.toTimerActivity.observe(TestUtils.TEST_OBSERVER, observer)

        val timerObrvb = mock<TimerSettingObservable>()
        Mockito.doNothing().whenever(timerObrvb).finalizeDetail()
        whenever(timerObrvb.getFinalSetting()).thenReturn(testTimer)

        viewmodel.fromPreset = false
        viewmodel.fromTemp = true
        viewmodel.timerObsvb.set(timerObrvb)
        viewmodel.timer = testTimer

        viewmodel.onTimerStartClicked()

        verify(observer).onChanged(any())

    }


}