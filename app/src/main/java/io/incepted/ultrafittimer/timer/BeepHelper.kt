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
import timber.log.Timber

class BeepHelper(val context: Context, val sharedPref: SharedPreferences) : SoundPool.OnLoadCompleteListener {

    private var soundPool: SoundPool? = null

    private val v: Vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    private var cueSoundRes: Int = 0
    private var beepSoundRes: Int = 0

    private val soundIds = IntArray(3)
    private val isLoaded = BooleanArray(3)

    private var vibrationEnabled = true

    companion object {
        const val FLAG_BEEP = 0
        const val FLAG_CUE = 1
        const val FLAG_FINISH = 2

        private const val MAX_STREAM = 3

        val VIBRATION_PATTERN_BEEP_TIMING = longArrayOf(0L, 400L, 100L, 200L)
        val VIBRATION_PATTERN_BEEP_AMPLITUDE = intArrayOf(0, 255, 0, 255)

        val VIBRATION_PATTERN_FINISH_TIMING = longArrayOf(0L, 800L, 100L, 800L, 100L, 800L)
        val VIBRATION_PATTERN_FINISH_AMPLITUDE = intArrayOf(0, 255, 0, 255, 0, 255)

        const val VIBRATION_CUE_DURATION = 200L
        const val VIBRATION_CUE_AMPLITUDE = 180
    }


    private var requestedFlag = FLAG_BEEP


    init {
        Timber.d("INIT!")
        initSoundPool()
        getSettingValues(sharedPref)
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

        soundPool?.setOnLoadCompleteListener(this)
    }


    private fun getSettingValues(sharedPref: SharedPreferences) {
        val beepPrefKey = context.resources.getString(R.string.pref_key_beep_sound)
        val cuePrefKey = context.resources.getString(R.string.pref_key_cue_sound)
        val vibrationPrefKey = context.resources.getString(R.string.pref_key_vibration)

        beepSoundRes = SoundResSwitcher.beepResSwitcher((sharedPref.getString(beepPrefKey, "0")
                ?: "0").toInt())
        cueSoundRes = SoundResSwitcher.cueResSwitcher((sharedPref.getString(cuePrefKey, "0")
                ?: "0").toInt())
        vibrationEnabled = sharedPref.getBoolean(vibrationPrefKey, false)
    }


    /**
     * Triggered when the app needs to play the sound effect
     *
     * @param session Current WorkoutSession
     * @param flag BEEP = 0, CUE = 1
     */
    fun requestFire(flag: Int) {
        try {
            requestedFlag = flag
            if (isLoaded[flag]) {
                // sound is loaded.
                playBeep(soundIds[flag])
            } else {
                soundIds[flag] = soundPool?.load(context,
                        if (flag == FLAG_BEEP || flag == FLAG_FINISH) beepSoundRes
                        else cueSoundRes, 1) ?: 0
            }
        } catch (e: Resources.NotFoundException) {
            Timber.d("Resource not found")
        }
    }


    override fun onLoadComplete(soundPool: SoundPool?, soundId: Int, status: Int) {
        if (status == 0) {
            if (soundId == soundIds[requestedFlag]) {
                isLoaded[requestedFlag] = true
                playBeep(soundId)
            }
        }
    }


    private fun playBeep(soundId: Int) {
        soundPool?.play(soundId, 1f, 1f, 1, 0, 1f)
        if (vibrationEnabled) vibrateDevice()
    }


    private fun vibrateDevice() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
        soundPool?.release()
        soundPool = null
    }

}