package io.incepted.ultrafittimer.fragment


import android.os.Bundle
import android.preference.PreferenceManager
import androidx.preference.PreferenceFragmentCompat
import io.incepted.ultrafittimer.R
import android.content.Intent
import android.content.SharedPreferences
import io.incepted.ultrafittimer.activity.SoundSettingActivity
import io.incepted.ultrafittimer.timer.SoundResSwitcher


class SettingsFragment : PreferenceFragmentCompat(), androidx.preference.PreferenceManager.OnPreferenceTreeClickListener {

    companion object {
        const val RC_BEEP = 5445
        const val RC_CUE = 5434
    }

    lateinit var sharedPref: SharedPreferences


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.app_preferences)
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context)

    }


    override fun onPreferenceTreeClick(preference: androidx.preference.Preference?): Boolean {

        when (preference?.key) {
            getString(R.string.pref_key_beep_sound) -> {

                val beepValue =
                        (sharedPref.getString(resources.getString(R.string.pref_key_beep_sound), "0") ?: "0").toInt()

                val intent = Intent(activity, SoundSettingActivity::class.java)
                intent.putExtra(SoundSettingActivity.EXTRA_KEY_IS_CUE_PREF, false)
                intent.putExtra(SoundSettingActivity.EXTRA_KEY_PREF_VALUE, beepValue)
                startActivityForResult(intent, RC_BEEP)

            }

            getString(R.string.pref_key_cue_sound) -> {

                val cueValue =
                        (sharedPref.getString(resources.getString(R.string.pref_key_cue_sound), "0") ?: "0").toInt()


                val intent = Intent(activity, SoundSettingActivity::class.java)
                intent.putExtra(SoundSettingActivity.EXTRA_KEY_IS_CUE_PREF, true)
                intent.putExtra(SoundSettingActivity.EXTRA_KEY_PREF_VALUE, cueValue)
                startActivityForResult(intent, RC_CUE)

            }
        }

        return true
    }


    override fun onResume() {
        super.onResume()

        val beepKey = resources.getString(R.string.pref_key_beep_sound)
        val cueKey = resources.getString(R.string.pref_key_cue_sound)

        val beepVal = (sharedPref.getString(beepKey, "0") ?: "0").toInt()
        val cueVal = (sharedPref.getString(cueKey, "0") ?: "0").toInt()

        findPreference(beepKey).summary = SoundResSwitcher.beepNameSwitcher(beepVal)
        findPreference(cueKey).summary = SoundResSwitcher.cueNameSwitcher(cueVal)

    }


}
