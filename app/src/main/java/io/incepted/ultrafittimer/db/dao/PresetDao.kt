package io.incepted.ultrafittimer.db.dao

import android.arch.persistence.room.*
import io.incepted.ultrafittimer.db.model.Preset
import io.reactivex.Flowable
import io.reactivex.Maybe

@Dao
interface PresetDao {

    @Query("SELECT * FROM preset")
    fun getAllPresets(): Flowable<List<Preset>>

    @Query("SELECT * FROM preset WHERE _id = (:presetId)")
    fun getPresetById(presetId: Int) : Maybe<Preset>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPreset(newPreset: Preset)

    @Update
    fun updatePreset(updatedPreset: Preset)

    @Query("DELETE FROM preset WHERE _id = (:presetId)")
    fun deletePreset(presetId: Int)

    @Query("DELETE FROM preset")
    fun deleteAllPresets()

}