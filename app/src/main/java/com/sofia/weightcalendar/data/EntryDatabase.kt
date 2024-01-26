package com.sofia.weightcalendar.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Entry::class], version = 1)
abstract class EntryDatabase : RoomDatabase() {
    abstract fun entryDao(): EntryDao

    companion object {

        @Volatile
        private var INSTANCE: EntryDatabase? = null

        fun getInstance(
            context: Context
        ): EntryDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                EntryDatabase::class.java, "reminders.db"
            )
                .fallbackToDestructiveMigration().build()
    }
}