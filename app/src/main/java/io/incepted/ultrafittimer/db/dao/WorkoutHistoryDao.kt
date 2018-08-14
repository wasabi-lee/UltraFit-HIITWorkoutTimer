package io.incepted.ultrafittimer.db.dao

import androidx.room.*
import io.incepted.ultrafittimer.db.model.WorkoutHistory
import io.reactivex.Flowable
import io.reactivex.Maybe

@Dao 
interface WorkoutHistoryDao {

    @Query("SELECT * FROM workout_history")
    fun getAllWorkoutHistory(): Flowable<List<WorkoutHistory>>

    @Query("SELECT * FROM workout_history WHERE _id = (:workoutHistoryId)")
    fun getWorkoutHistoryById(workoutHistoryId: Int) : Maybe<WorkoutHistory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWorkoutHistory(newWorkoutHistory: WorkoutHistory)

    @Update
    fun updateWorkoutHistory(updatedWorkoutHistory: WorkoutHistory)

    @Query("DELETE FROM workout_history WHERE _id = (:workoutHistoryId)")
    fun deleteWorkoutHistory(workoutHistoryId: Int)

    @Query("DELETE FROM workout_history")
    fun deleteAllWorkoutHistorys()

}