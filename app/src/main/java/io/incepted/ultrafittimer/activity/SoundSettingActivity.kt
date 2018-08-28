package io.incepted.ultrafittimer.activity

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import io.incepted.ultrafittimer.R
import io.incepted.ultrafittimer.adapter.SoundSettingAdapter
import io.incepted.ultrafittimer.db.tempmodel.SoundItem
import io.incepted.ultrafittimer.timer.SoundResSwitcher
import kotlinx.android.synthetic.main.activity_sound_setting.*

import javax.inject.Inject

class SoundSettingActivity : AppCompatActivity(),
        SoundSettingAdapter.SoundSelectCallback, MediaPlayer.OnCompletionListener {

    companion object {
        const val EXTRA_KEY_IS_CUE_PREF = "is_cue_pref"
        const val EXTRA_KEY_PREF_VALUE = "pref_val"
    }

    @Inject
    lateinit var sharedPref: SharedPreferences

    private var isCue = false
    private var prefValue = 1

    private var soundAdapter: SoundSettingAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sound_setting)

        unpackExtra()
        initToolbar()
        initListView()

    }

    private fun unpackExtra() {
        isCue = intent.getBooleanExtra(EXTRA_KEY_IS_CUE_PREF, false)
        prefValue = intent.getIntExtra(EXTRA_KEY_PREF_VALUE, 1)
    }


    private fun initToolbar() {
        setSupportActionBar(sound_setting_toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val toolbarText = resources.getString(
                if (isCue) R.string.sound_setting_toolbar_header_cue
                else R.string.sound_setting_toolbar_header_beep)

        sound_setting_toolbar_text.text = toolbarText
    }


    private fun initListView() {
        soundAdapter = SoundSettingAdapter(this, getSoundList(), this)
        sound_setting_list.adapter = soundAdapter
    }


    private fun getSoundList(): ArrayList<SoundItem> {
        val nameArray = resources.getStringArray(
                if (isCue) R.array.pref_list_cue_sound else R.array.pref_list_beep_sound)
        val valArrayStr = resources.getStringArray(
                if (isCue) R.array.pref_list_cue_sound_value else R.array.pref_list_beep_sound_value)

        val soundList = arrayListOf<SoundItem>()
        for (i in 0 until nameArray.size) {
            soundList.add(
                    SoundItem(soundName = nameArray[i],
                            soundValue = valArrayStr[i].toInt(),
                            selected = valArrayStr[i].toInt() == prefValue))
        }
        return soundList
    }


    override fun onSoundSelected(selectedVal: Int) {
        val res =
                if (isCue) SoundResSwitcher.cueResSwitcher(selectedVal)
                else SoundResSwitcher.beepResSwitcher(selectedVal)
        playSound(res)
    }


    private fun playSound(res: Int) {
        val mp = MediaPlayer.create(this, res)
        mp.setOnCompletionListener(this)
        mp.start()
    }


    override fun onCompletion(mp: MediaPlayer?) {
        mp?.release()
    }


    @SuppressLint("ApplySharedPref")
    override fun onBackPressed() {

        val prefKey = resources.getString(
                if (isCue) R.string.pref_key_cue_sound
                else R.string.pref_key_beep_sound
        )

        val editor = sharedPref.edit()
        editor.putString(prefKey, soundAdapter?.getSelectedSound()?.soundValue.toString())
        editor.commit()

        super.onBackPressed()

    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
