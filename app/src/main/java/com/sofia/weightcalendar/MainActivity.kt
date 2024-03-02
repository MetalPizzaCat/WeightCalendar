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
import com.sofia.weightcalendar.data.EntryDatabase
import com.sofia.weightcalendar.data.EntryRepository
import com.sofia.weightcalendar.ui.theme.WeightCalendarTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Calendar

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : ComponentActivity() {
    private val database by lazy { EntryDatabase.getInstance(this) }
    private val repository by lazy { EntryRepository(database.entryDao()) }
    private val appViewModel by lazy {
        ViewModelProvider(this, AppViewModelFactory(repository, dataStore))[AppViewModel::class.java]
    }
    private val applicationScope = CoroutineScope(SupervisorJob())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = this
        applicationScope.launch {
            generateMonth(
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                context
            )
        }
        setContent {
            WeightCalendarTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    AppScaffold(appViewModel)
                }
            }
        }
    }
}