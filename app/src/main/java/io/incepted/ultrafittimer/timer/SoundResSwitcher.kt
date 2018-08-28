package io.incepted.ultrafittimer.timer

import io.incepted.ultrafittimer.R

object SoundResSwitcher {


    fun beepNameSwitcher(beepVal: Int): String {
        return when (beepVal) {
            1 -> "Basic Beep"
            2 -> "Whistle"
            3 -> "Machine Gun"
            4 -> "Rifle Shot"
            5 -> "Chime Bell"
            6 -> "Click"
            7 -> "Glass Click"
            8 -> "Home Run"
            9 -> "Xylophone Beep"
            else -> "Basic Beep"
        }
    }


    fun cueNameSwitcher(cueVal: Int): String {
        return when (cueVal) {
            1 -> "Basic Cue"
            2 -> "Camera Click"
            3 -> "Hint"
            4 -> "Pistol"
            5 -> "Shotgun Pump"
            6 -> "Temple Block"
            7 -> "Xylophone Tick"
            else -> "Basic Cue"
        }
    }


    fun beepResSwitcher(beepVal: Int): Int {
        return when (beepVal) {
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

    fun cueResSwitcher(cueVal: Int): Int {
        return when (cueVal) {
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
