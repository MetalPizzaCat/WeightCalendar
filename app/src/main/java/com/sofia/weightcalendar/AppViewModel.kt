package com.sofia.weightcalendar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.sofia.weightcalendar.data.EntryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private object PreferenceKeys {
    val TARGET_STEPS = intPreferencesKey("target_steps")
}

enum class AppTabs {
    CALENDAR, GRAPH, SETTINGS
}

class AppViewModel(
    private val repository: EntryRepository,
    private val dataStore: DataStore<Preferences>
) :
    ViewModel() {
    var currentTab by mutableStateOf(AppTabs.CALENDAR)
        private set

    fun setCurrentTab(tab : Int){
        currentTab = AppTabs.entries[tab]
    }
    fun entriesForMonth(month: Int, year: Int) =
        repository.entriesForMonth(month, year).asLiveData()

    fun entriesForYear(year: Int) =
        repository.entriesForYear(year).asLiveData()

    fun getTargetSteps(): Flow<Int> {
        return dataStore.data.map {
            it[PreferenceKeys.TARGET_STEPS] ?: 0
        }
    }

    fun setTargetSteps(steps: Int) {
        CoroutineScope(SupervisorJob()).launch {
            dataStore.edit { settings ->
                settings[PreferenceKeys.TARGET_STEPS] = steps
            }
        }
    }
}

class AppViewModelFactory(
    private val repository: EntryRepository,
    private val dataStore: DataStore<Preferences>
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(repository, dataStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}