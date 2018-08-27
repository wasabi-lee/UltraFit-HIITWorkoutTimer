package io.incepted.ultrafittimer.timer

import io.incepted.ultrafittimer.R

object SoundResSwitcher {

    fun beepSwitcher(beepTheme: Int): Int {
        return when (beepTheme) {
            1 -> R.raw.beep_basic_beep
            2 -> R.raw.beep_whistle
            3 -> R.raw.beep_machine_gun
            4 -> R.raw.beep_rifle_shot
            5 -> R.raw.beep_chime_bell
            6 -> R.raw.beep_click
            7 -> R.raw.beep_glass_click
            8 -> R.raw.beep_home_run
            9 -> R.raw.beep_xylophone_beep
            else -> R.raw.beep_basic_beep
        }
    }

    fun cueSwitcher(tickTheme: Int): Int {
        return when (tickTheme) {
            1 -> R.raw.tick_basic_tick
            2 -> R.raw.tick_camera_click
            3 -> R.raw.tick_hint
            4 -> R.raw.tick_pistol
            5 -> R.raw.tick_shotgun_pump
            6 -> R.raw.tick_temple_block_hit
            7 -> R.raw.tick_xylophone_tick
            else -> R.raw.tick_basic_tick
        }
    }

}
