package io.incepted.ultrafittimer.timer

object TimerCommunication {

    const val TIMER_NOTIFICATION_ID = 4343122
    const val TIMER_COMPLETE_NOTIFICATION_ID = 4675457

    // Intent bundle content keys
    const val BUNDLE_KEY_IS_PRESET = "bundle_key_warmup_time"
    const val BUNDLE_KEY_TARGET_ID = "bundle_key_work_name"

    // Broadcast receiver extra keys for Service-Activity communication
    const val BR_ACTION_TIMER_TICK_RESULT = "io.incepted.ultrafittimer.timer.TIMER_TICK"
    const val BR_ACTION_TIMER_COMPLETED_RESULT = "io.incepted.ultrafittimer.timer.TIMER_COMPLETED"
    const val BR_ACTION_TIMER_SESSION_SWITCH = "io.incepted.ultrafittimer.timer.TIMER_SESSION_SWITCH"
    const val BR_ACTION_TIMER_RESUME_PAUSE_STATE = "io.incepted.ultrafittimer.timer.TIMER_RESUME_PAUSE_STATE"
    const val BR_ACTION_TIMER_TERMINATED = "io.incepted.ultrafittimer.timer.TIMER_TERMINATED"
    const val BR_ACTION_TIMER_ERROR = "io.incepted.ultrafittimer.timer.TIMER_ERROR"

    // Tick info extras
    const val BR_EXTRA_KEY_TICK_SESSION_NAME = "io.incepted.ultrafittimer.timer.EXTRA_KEY_SESSION_NAME"
    const val BR_EXTRA_KEY_TICK_SESSION_REMAINING_SECS = "io.incepted.ultrafittimer.timer.EXTRA_KEY_SESSION_REMAINING_SEC"
    const val BR_EXTRA_KEY_TICK_SESSION_ROUND_TOTAL_SECS = "io.incepted.ultrafittimer.timer.EXTRA_KEY_SESSION_ROUND_TOTAL_SECS"
    const val BR_EXTRA_KEY_TICK_SESSION_SESSION = "io.incepted.ultrafittimer.timer.EXTRA_KEY_SESSION_SESSION"
    const val BR_EXTRA_KEY_TICK_SESSION_ROUND_COUNT = "io.incepted.ultrafittimer.timer.EXTRA_KEY_SESSION_COUNT"
    const val BR_EXTRA_KEY_TICK_SESSION_TOTAL_ROUND = "io.incepted.ultrafittimer.timer.EXTRA_KEY_TOTAL_ROUND"
    const val BR_EXTRA_KEY_TICK_SESSION_FIRST_TICK = "io.incepted.ultrafittimer.timer.EXTRA_KEY_TOTAL_ROUND"

    // Resume pause state extra
    const val BR_EXTRA_KEY_RESUME_PAUSE_STATE = "io.incepted.ultrafittimer.timer.EXTRA_KEY_RESUME_PAUSE_STATE"

}