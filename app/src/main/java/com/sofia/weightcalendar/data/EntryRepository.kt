package com.sofia.weightcalendar.data

import androidx.annotation.WorkerThread
import com.sofia.weightcalendar.DayWeightData
import kotlinx.coroutines.flow.Flow

class EntryRepository(private val entryDao: EntryDao) {
    val allEntries: Flow<List<Entry>> = entryDao.getAll()

    fun entriesForMonth(month: Int, year: Int) = entryDao.getAll(year, month)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(entry: Entry) {
        entryDao.insert(entry)
    }

    suspend fun updateMorningWeight(data: DayWeightData) {
        if (entryDao.exists(data.year, data.month, data.day)) {
            entryDao.updateMorningWeight(data.year, data.month, data.day, data.weight)
        } else {
            entryDao.insert(Entry(null, data.day, data.month, data.year, data.weight, null))
        }
    }

    suspend fun updateEveningWeight(data: DayWeightData) {
        if (entryDao.exists(data.year, data.month, data.day)) {
            entryDao.updateEveningWeight(data.year, data.month, data.day, data.weight)
        } else {
            entryDao.insert(Entry(null, data.day, data.month, data.year, null, data.weight))
        }
    }
}