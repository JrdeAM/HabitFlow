package br.edu.jadeandrade.habitflow.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface HabitDao {

    @Insert
    fun insert(habit: HabitEntity): Long

    @Update
    fun update(habit: HabitEntity)

    @Delete
    fun delete(habit: HabitEntity)

    @Query("SELECT * FROM habits WHERE userId = :userId ORDER BY id DESC")
    fun getByUserId(userId: Int): List<HabitEntity>
}