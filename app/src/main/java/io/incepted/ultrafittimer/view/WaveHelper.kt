package io.incepted.ultrafittimer.view

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
import com.gelitenight.waveview.library.WaveView
import io.incepted.ultrafittimer.timer.TickInfo
import io.incepted.ultrafittimer.util.WorkoutSession


class WaveHelper(private val mWaveView: WaveView) {

    private var mAnimatorSet: AnimatorSet? = null

    private var waterLevelAnim: ObjectAnimator? = null

    private var pausedTime: Long = 0L

    init {
        initAnimation()
    }

    fun start() {
        mWaveView.isShowWave = true
        if (mAnimatorSet != null) {
            mAnimatorSet!!.start()
        }
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

        mWaveView.setWaveColor(WorkoutSession.getSessionColor(mWaveView.context, session),
                WorkoutSession.getSecondarySessionColor(mWaveView.context, session))

        waterLevelAnim = ObjectAnimator.ofFloat(
                mWaveView, "waterLevelRatio", 0f, 1f)

        waterLevelAnim?.duration = max * 1000
        waterLevelAnim?.interpolator = LinearInterpolator()
        waterLevelAnim?.start()

    }


    fun resumeWave() {
        waterLevelAnim?.start()
        waterLevelAnim?.currentPlayTime = pausedTime
    }

    fun pauseWave() {
        pausedTime = waterLevelAnim?.currentPlayTime ?: 0L
        waterLevelAnim?.cancel()
    }


    fun cancel() {
        if (mAnimatorSet != null) {
            //            mAnimatorSet.cancel();
            mAnimatorSet!!.end()
        }
    }
}