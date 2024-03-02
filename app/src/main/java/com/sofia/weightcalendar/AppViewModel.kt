package com.sofia.weightcalendar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
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
import java.util.Calendar

private object PreferenceKeys {
    val TARGET_STEPS = intPreferencesKey("target_steps")
    val CHART_STEP = floatPreferencesKey("chart_step")
}

enum class AppTabs {
    CALENDAR, GRAPH
}

class AppViewModel(
    private val repository: EntryRepository,
    private val dataStore: DataStore<Preferences>
) :
    ViewModel() {
    var currentTab by mutableStateOf(AppTabs.CALENDAR)
        private set

    /**
     * Current type of chart to display
     */
    var currentChartDurationType by mutableStateOf(ChartDurationType.DAILY)

    /**
     * Current year selected via settings
     */
    var selectedYear by mutableIntStateOf(Calendar.getInstance().get(Calendar.YEAR))

    /**
     * Current month selected via settings
     */
    var selectedMonth by mutableIntStateOf(Calendar.getInstance().get(Calendar.MONTH))

    /**
     * Change tab to a new one based on id
     * @param tab Tab value in id
     */
    fun setCurrentTab(tab: Int) {
        currentTab = AppTabs.entries[tab]
    }

    /**
     * Get all entries for a month
     * @param month month to read from
     * @param year year from which to read from
     */
    fun entriesForMonth(month: Int, year: Int) =
        repository.entriesForMonth(month, year).asLiveData()

    fun entriesForYear(year: Int) =
        repository.entriesForYear(year).asLiveData()

    /**
     * Get current minimal steps value from android settings
     */
    fun getTargetSteps(): Flow<Int> {
        return dataStore.data.map {
            it[PreferenceKeys.TARGET_STEPS] ?: 0
        }
    }

    /**
     * Set new minimal steps value and save it in settings
     * @param steps New steps value
     */
    fun setTargetSteps(steps: Int) {
        CoroutineScope(SupervisorJob()).launch {
            dataStore.edit { settings ->
                settings[PreferenceKeys.TARGET_STEPS] = steps
            }
        }
    }

    fun getChartStep(): Flow<Float> {
        return dataStore.data.map {
            it[PreferenceKeys.CHART_STEP] ?: 0f
        }
    }

    fun setChartStep(step: Float) {
        CoroutineScope(SupervisorJob()).launch {
            dataStore.edit { settings ->
                settings[PreferenceKeys.CHART_STEP] = step
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