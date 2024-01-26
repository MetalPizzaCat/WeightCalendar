package com.sofia.weightcalendar.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {
    @Query("SELECT * FROM entry")
    fun getAll(): Flow<List<Entry>>

    @Query("SELECT * FROM entry WHERE year = :year and month = :month")
    fun getAll(year: Int, month: Int): Flow<List<Entry>>

    @Query("UPDATE entry SET morning_weight = :weight WHERE year = :year and month = :month and day = :day")
    suspend fun updateMorningWeight(year: Int, month: Int, day: Int, weight: Float)

    @Query("UPDATE entry SET evening_weight = :weight WHERE year = :year and month = :month and day = :day")
    suspend fun updateEveningWeight(year: Int, month: Int, day: Int, weight: Float)

    @Query("SELECT EXISTS(SELECT * FROM entry WHERE  year = :year and month = :month and day = :day)")
    fun exists(year: Int, month: Int, day: Int): Boolean

    @Query("SELECT EXISTS(SELECT * FROM entry WHERE  year = :year and month = :month)")
    fun monthExists(year: Int, month: Int): Boolean

    @Insert
    fun insert(entry: Entry)

    @Delete
    fun delete(entry: Entry)
}