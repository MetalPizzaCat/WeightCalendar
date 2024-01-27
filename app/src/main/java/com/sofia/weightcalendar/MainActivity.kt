package com.sofia.weightcalendar

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.sofia.weightcalendar.data.Entry
import com.sofia.weightcalendar.data.EntryDatabase
import com.sofia.weightcalendar.data.EntryRepository
import com.sofia.weightcalendar.ui.theme.WeightCalendarTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.util.Calendar

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : ComponentActivity() {
    private val database by lazy { EntryDatabase.getInstance(this) }
    private val repository by lazy { EntryRepository(database.entryDao()) }
    private val appViewModel by lazy {
        ViewModelProvider(this, AppViewModelFactory(repository, dataStore)).get(
            AppViewModel::class.java
        )
    }
    private val applicationScope = CoroutineScope(SupervisorJob())

    private suspend fun updateMorningWeight(data: DayWeightData) {
        val entryDao = EntryDatabase.getInstance(this).entryDao()
        if (entryDao.exists(data.year, data.month, data.day)) {
            entryDao.updateMorningWeight(data.year, data.month, data.day, data.weight)
        } else {
            entryDao.insert(
                Entry(
                    null,
                    data.day,
                    data.month,
                    data.year,
                    data.weight,
                    null,
                    steps = null
                )
            )
        }
    }

    private suspend fun updateEveningWeight(data: DayWeightData) {
        val entryDao = EntryDatabase.getInstance(this).entryDao()
        if (entryDao.exists(data.year, data.month, data.day)) {
            entryDao.updateEveningWeight(data.year, data.month, data.day, data.weight)
        } else {
            entryDao.insert(
                Entry(
                    null,
                    data.day,
                    data.month,
                    data.year,
                    null,
                    data.weight,
                    steps = null
                )
            )
        }
    }

    private suspend fun updateSteps(data: DayStepsData) {
        val entryDao = EntryDatabase.getInstance(this).entryDao()
        if (entryDao.exists(data.year, data.month, data.day)) {
            entryDao.updateSteps(data.year, data.month, data.day, data.steps)
        } else {
            entryDao.insert(Entry(null, data.day, data.month, data.year, null, null, data.steps))
        }
    }

    /**
     * Generates a new month data if month is not present. Exists to make sure that all days are filled
     * But also to avoid dealing with having objects being created by not quite in the columns
     * Shouldn't be an issue since it's just 31 entries added each time
     */
    private fun generateMonth(year: Int, month: Int) {
        val entryDao = EntryDatabase.getInstance(this).entryDao()
        if (entryDao.monthExists(year, month)) {
            return
        }
        // YearMonth expects values in range on 1-12 while Calendar returns values in 0-11
        // that is stupid :3
        for (day in 1..YearMonth.of(year, month + 1).lengthOfMonth()) {
            entryDao.insert(Entry(null, day, month, year, null, null, null))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applicationScope.launch {
            generateMonth(
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH)
            )
        }
        setContent {
            WeightCalendarTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    AppScaffold(
                        appViewModel,
                        onTimeChanged = { year, month ->
                            applicationScope.launch {
                                generateMonth(year, month)
                            }
                        },
                        // all of the functions call back to the top of the app because of the threading issue
                        // for whatever reason android threw an error about accessing database on ui thread
                        // no matter what scope was used in child components
                        onEveningWeightChanged = {
                            applicationScope.launch {
                                updateEveningWeight(it)
                            }
                        }, onMorningWeightChanged = {
                            applicationScope.launch {
                                updateMorningWeight(it)
                            }
                        },
                        onStepsChanged = {
                            applicationScope.launch {
                                updateSteps(it)
                            }
                        })
                }
            }
        }
    }
}