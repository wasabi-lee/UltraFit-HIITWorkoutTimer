package io.incepted.ultrafittimer.util

import io.incepted.ultrafittimer.R

object WorkoutSession {
    const val WARMUP = 0
    const val WORK = 1
    const val REST = 2
    const val COOLDOWN = 3
    const val ROUND = 4


    fun getSessionById(viewId: Int): Int {
        return when (viewId) {
            R.id.main_session_warmup_minus_iv, R.id.main_session_warmup_plus_iv, R.id.main_session_time_warmup_edit
                -> WARMUP
            R.id.main_session_work_minus_iv, R.id.main_session_work_plus_iv, R.id.main_session_time_work_edit
                -> WORK
            R.id.main_session_rest_minus_iv, R.id.main_session_rest_plus_iv, R.id.main_session_time_rest_edit
                -> REST
            R.id.main_session_cooldown_minus_iv, R.id.main_session_cooldown_plus_iv, R.id.main_session_time_cooldown_edit
                -> COOLDOWN
            R.id.main_session_rounds_minus_iv, R.id.main_session_rounds_plus_iv, R.id.main_session_time_rounds_edit
                -> ROUND
            else -> 0
        }
    }
}