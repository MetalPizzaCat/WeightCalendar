package com.sofia.weightcalendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.sofia.weightcalendar.data.Entry
import com.sofia.weightcalendar.data.EntryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppViewModel(val repository: EntryRepository) : ViewModel() {


    fun entriesForMonth(month: Int, year: Int) =
        repository.entriesForMonth(month, year).asLiveData()

    fun insert(note: Entry) = viewModelScope.launch {
        repository.insert(note)
    }

    suspend fun updateMorningWeight(data: DayWeightData) = viewModelScope.launch {
        repository.updateMorningWeight(data)
    }

    suspend fun updateEveningWeight(data: DayWeightData) = viewModelScope.launch {
        repository.updateEveningWeight(data)
    }
}

class AppViewModelFactory(private val repository: EntryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}