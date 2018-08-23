package io.incepted.ultrafittimer.util

import android.content.Context
import androidx.core.content.ContextCompat
import io.incepted.ultrafittimer.R

object WorkoutSession {
    const val WARMUP = 0
    const val WORK = 1
    const val REST = 2
    const val COOLDOWN = 3
    const val ROUND = 4
    const val COMPLETED = 5

    var SESSION_COLOR_WARMUP: Int? = null
    var SESSION_COLOR_WORK: Int? = null
    var SESSION_COLOR_REST: Int? = null
    var SESSION_COLOR_COOLDOWN: Int? = null


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

    fun getSessionColor(context: Context, session: Int): Int {

        if (SESSION_COLOR_WARMUP == null) SESSION_COLOR_WARMUP = ContextCompat.getColor(context, R.color.state_warmup)
        if (SESSION_COLOR_WORK == null) SESSION_COLOR_WORK = ContextCompat.getColor(context, R.color.state_work)
        if (SESSION_COLOR_REST == null) SESSION_COLOR_REST = ContextCompat.getColor(context, R.color.state_rest)
        if (SESSION_COLOR_COOLDOWN == null) SESSION_COLOR_COOLDOWN = ContextCompat.getColor(context, R.color.state_cooldown)

        return when (session) {
            WARMUP -> SESSION_COLOR_WARMUP ?: ContextCompat.getColor(context, R.color.state_warmup)
            WORK -> SESSION_COLOR_WORK ?: ContextCompat.getColor(context, R.color.state_work)
            REST -> SESSION_COLOR_REST ?: ContextCompat.getColor(context, R.color.state_rest)
            COOLDOWN -> SESSION_COLOR_COOLDOWN ?: ContextCompat.getColor(context, R.color.state_cooldown)
            else -> android.R.color.transparent
        }

    }
}