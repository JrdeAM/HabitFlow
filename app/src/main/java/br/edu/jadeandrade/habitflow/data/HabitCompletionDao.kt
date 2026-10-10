package br.edu.jadeandrade.habitflow.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface HabitCompletionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(completion: HabitCompletionEntity): Long

    @Query(
        """
        SELECT COUNT(*) 
        FROM habit_completions
        WHERE habitId = :habitId
        AND completedDate BETWEEN :startDate AND :endDate
        """
    )
    fun countCompletionsInPeriod(
        habitId: Int,
        startDate: String,
        endDate: String
    ): Int

    @Query(
        """
        SELECT EXISTS(
            SELECT 1
            FROM habit_completions
            WHERE habitId = :habitId
            AND completedDate = :date
        )
        """
    )
    fun wasCompletedOnDate(habitId: Int, date: String): Boolean
}