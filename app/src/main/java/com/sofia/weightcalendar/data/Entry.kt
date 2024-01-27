package com.sofia.weightcalendar.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Entry(
    @PrimaryKey(autoGenerate = true) val uid: Int? = null,
    @ColumnInfo(name = "day") val day : Int,
    @ColumnInfo(name = "month") val month : Int,
    @ColumnInfo(name = "year") val year : Int,
    @ColumnInfo(name = "morning_weight") val morningWeight : Float? = null,
    @ColumnInfo(name = "evening_weight") val eveningWeight : Float? = null,
    @ColumnInfo(name = "steps") val steps : Int? = null
)
