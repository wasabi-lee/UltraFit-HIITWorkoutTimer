package io.incepted.ultrafittimer.view

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import android.view.animation.LinearInterpolator
import com.gelitenight.waveview.library.WaveView
import io.incepted.ultrafittimer.timer.TickInfo
import io.incepted.ultrafittimer.util.WorkoutSession
import timber.log.Timber


class WaveHelper(private val mWaveView: WaveView) {

    private var mAnimatorSet: AnimatorSet? = null

    private var waterLevelAnim: ObjectAnimator? = null

    private var pausedTime: Long = 0L


    private var lastWaterLevel = 0f
    private var currentMaxWaterLevel = 0f
    private var currentSession = 0
    private var paused = false


    init {
        mWaveView.setWaveColor(Color.parseColor("#0Bffffff"), Color.parseColor("#1Fffffff"))
        initState()
        initAnimation()
    }


    fun start() {
        mWaveView.isShowWave = true
        if (mAnimatorSet != null) {
            mAnimatorSet!!.start()
        }
    }


    private fun initState() {
        mWaveView.waterLevelRatio = 0.0f
        mWaveView.setShapeType(WaveView.ShapeType.SQUARE)
    }


    private fun initAnimation() {
        val animators = arrayListOf<Animator>()

        // horizontal animation.
        // wave waves infinitely.
        val waveShiftAnim = ObjectAnimator.ofFloat(
                mWaveView, "waveShiftRatio", 0f, 1f)
        waveShiftAnim.repeatCount = ValueAnimator.INFINITE
        waveShiftAnim.duration = 1000
        waveShiftAnim.interpolator = LinearInterpolator()
        animators.add(waveShiftAnim)


        // amplitude animation.
        // wave grows big then grows small, repeatedly
        val amplitudeAnim = ObjectAnimator.ofFloat(
                mWaveView, "amplitudeRatio", 0.0005f, 0.02f)
        amplitudeAnim.repeatCount = ValueAnimator.INFINITE
        amplitudeAnim.repeatMode = ValueAnimator.REVERSE
        amplitudeAnim.duration = 5000
        amplitudeAnim.interpolator = LinearInterpolator()
        animators.add(amplitudeAnim)

        mAnimatorSet = AnimatorSet()
        mAnimatorSet!!.playTogether(animators)
    }

    fun liftWave(tickInfo: TickInfo) {

        val session = tickInfo.session
        val max = tickInfo.roundTotalSecs
        liftWave(session, max, 0, false)

    }


    private fun liftWave(session: Int, max: Long, progress: Long, paused: Boolean) {

        val startingWaterLevel = progress.toFloat() / max.toFloat()
        val duration = (max - progress) * 1000

        mWaveView.waterLevelRatio = startingWaterLevel

        waterLevelAnim = ObjectAnimator.ofFloat(
                mWaveView, "waterLevelRatio", startingWaterLevel, 1f)

        waterLevelAnim?.duration = duration
        waterLevelAnim?.interpolator = LinearInterpolator()

        if (!paused)
            waterLevelAnim?.start()


    }


    fun setAnimState(tickInfo: TickInfo?, paused: Boolean?) {
        if (tickInfo != null && paused != null) {

            val session = tickInfo.session
            val max = tickInfo.roundTotalSecs
            val progress = (max - tickInfo.remianingSecs)

            liftWave(session, max, progress, paused)
        }
    }


    fun resumeWave() {
        waterLevelAnim?.start()
        waterLevelAnim?.currentPlayTime = pausedTime
        paused = false
    }


    fun pauseWave() {
        pausedTime = waterLevelAnim?.currentPlayTime ?: 0L
        waterLevelAnim?.cancel()
        paused = true
        lastWaterLevel = mWaveView.waterLevelRatio
    }


    fun cancel() {
        if (mAnimatorSet != null && waterLevelAnim != null) {
            //            mAnimatorSet.cancel();
            mAnimatorSet!!.end()
            pauseWave()
        }
    }
}