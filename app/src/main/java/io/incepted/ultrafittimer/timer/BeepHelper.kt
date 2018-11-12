package io.incepted.ultrafittimer.timer

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import io.incepted.ultrafittimer.R
import io.reactivex.Single
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

class BeepHelper(val context: Context, val sharedPref: SharedPreferences) {

    private var soundPool: SoundPool? = null

    private val v: Vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    private var cueSoundRes: Int = 0
    private var beepSoundRes: Int = 0

    private var cueSoundId = 0
    private var beepSoundId = 0

    private var vibrationEnabled = true

    companion object {
        const val FLAG_BEEP = 0
        const val FLAG_CUE = 1
        const val FLAG_FINISH = 2

        private const val MAX_STREAM = 3

        val VIBRATION_PATTERN_BEEP_TIMING = longArrayOf(0L, 400L, 100L, 200L)
        val VIBRATION_PATTERN_BEEP_AMPLITUDE = intArrayOf(0, 200, 0, 200)
        val VIBRATION_PATTERN_FINISH_TIMING = longArrayOf(0L, 800L, 100L, 800L, 100L, 800L)
        val VIBRATION_PATTERN_FINISH_AMPLITUDE = intArrayOf(0, 200, 0, 200, 0, 200)

        const val VIBRATION_CUE_DURATION = 200L
        const val VIBRATION_CUE_AMPLITUDE = 80
    }


    private var requestedFlag = FLAG_BEEP


    init {
        initSoundPool()
        getSettingValues(sharedPref)
        loadSound()
    }

    private fun initSoundPool() {
        soundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val audioAttrs: AudioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

            SoundPool.Builder()
                    .setMaxStreams(MAX_STREAM)
                    .setAudioAttributes(audioAttrs)
                    .build()

        } else {
            SoundPool(MAX_STREAM, AudioManager.STREAM_MUSIC, 0)
        }

    }


    private fun getSettingValues(sharedPref: SharedPreferences) {
        val beepPrefKey = context.resources.getString(R.string.pref_key_beep_sound)
        val cuePrefKey = context.resources.getString(R.string.pref_key_cue_sound)
        val vibrationPrefKey = context.resources.getString(R.string.pref_key_vibration)

        beepSoundRes = SoundResSwitcher
                .beepResSwitcher((sharedPref.getString(beepPrefKey, "0") ?: "0").toInt())
        cueSoundRes = SoundResSwitcher
                .cueResSwitcher((sharedPref.getString(cuePrefKey, "0") ?: "0").toInt())
        vibrationEnabled = sharedPref.getBoolean(vibrationPrefKey, false)
    }


    private fun loadSound() {
        cueSoundId = soundPool?.load(context, cueSoundRes, 1) ?: 0;
        beepSoundId = soundPool?.load(context, beepSoundRes, 1) ?: 0;
    }


    /**
     * Triggered when the app needs to play the sound effect
     *
     * @param session Current WorkoutSession
     * @param flag BEEP = 0, CUE = 1
     */
    fun requestFire(flag: Int) {
        try {
            playBeep(when (flag) {
                FLAG_CUE -> cueSoundId
                FLAG_BEEP, FLAG_FINISH -> beepSoundId
                else -> 0
            })

        } catch (e: Resources.NotFoundException) {
            e.printStackTrace()
        }
    }


    private fun playBeep(soundId: Int) {
        soundPool?.play(soundId, 1f, 1f, 1, 0, 1f)
        if (vibrationEnabled) vibrateDevice()
    }


    private fun vibrateDevice() {
        if (Build.VERSION.SDK_INT >= 26) {
            when (requestedFlag) {
                FLAG_BEEP ->
                    v.vibrate(VibrationEffect.createWaveform(VIBRATION_PATTERN_BEEP_TIMING,
                            VIBRATION_PATTERN_BEEP_AMPLITUDE,
                            -1))

                FLAG_CUE ->
                    v.vibrate(VibrationEffect.createOneShot(VIBRATION_CUE_DURATION,
                            VIBRATION_CUE_AMPLITUDE))

                FLAG_FINISH ->
                    v.vibrate(VibrationEffect.createWaveform(VIBRATION_PATTERN_FINISH_TIMING,
                            VIBRATION_PATTERN_FINISH_AMPLITUDE,
                            -1))
            }
        } else {
            when (requestedFlag) {
                FLAG_BEEP -> v.vibrate(VIBRATION_PATTERN_BEEP_TIMING, -1)
                FLAG_CUE -> v.vibrate(VIBRATION_CUE_DURATION)
                FLAG_FINISH -> v.vibrate(VIBRATION_PATTERN_FINISH_TIMING, -1)
            }
        }
    }


    fun release() {
        // Delaying SoundPool release() to prevent the last sound from clipping
        Single.timer(3, TimeUnit.SECONDS)
                .subscribeBy {
                    soundPool?.release()
                    soundPool = null
                }
    }
}