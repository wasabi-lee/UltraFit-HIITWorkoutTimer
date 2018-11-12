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

    var colorsLoaded = false

    var SESSION_COLOR_WARMUP: Int? = null
    var SESSION_COLOR_WORK: Int? = null
    var SESSION_COLOR_REST: Int? = null
    var SESSION_COLOR_COOLDOWN: Int? = null
    var SESSION_COLOR_COMPLETED: Int? = null

    var SESSION_COLOR_WARMUP_SECONDARY: Int? = null
    var SESSION_COLOR_WORK_SECONDARY: Int? = null
    var SESSION_COLOR_REST_SECONDARY: Int? = null
    var SESSION_COLOR_COOLDOWN_SECONDARY: Int? = null


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

        if (!colorsLoaded)
            initSessionColors(context, session)

        return when (session) {
            WARMUP -> SESSION_COLOR_WARMUP ?: ContextCompat.getColor(context, R.color.state_warmup)
            WORK -> SESSION_COLOR_WORK ?: ContextCompat.getColor(context, R.color.state_work)
            REST -> SESSION_COLOR_REST ?: ContextCompat.getColor(context, R.color.state_rest)
            COOLDOWN -> SESSION_COLOR_COOLDOWN
                    ?: ContextCompat.getColor(context, R.color.state_cooldown)
            COMPLETED -> SESSION_COLOR_COMPLETED
                    ?: ContextCompat.getColor(context, R.color.state_completed)
            else -> android.R.color.transparent
        }

    }


    private fun initSessionColors(context: Context, session: Int) {
        SESSION_COLOR_WARMUP = ContextCompat.getColor(context, R.color.state_warmup)
        SESSION_COLOR_WORK = ContextCompat.getColor(context, R.color.state_work)
        SESSION_COLOR_REST = ContextCompat.getColor(context, R.color.state_rest)
        SESSION_COLOR_COOLDOWN = ContextCompat.getColor(context, R.color.state_cooldown)
        SESSION_COLOR_COMPLETED = ContextCompat.getColor(context, R.color.state_completed)

        colorsLoaded = true
    }


}