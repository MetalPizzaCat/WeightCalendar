package com.sofia.weightcalendar

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import java.text.DateFormatSymbols
import java.util.Calendar

enum class AppScreenState {
    CALENDAR, CHART, SETTINGS
}

@Composable
fun AppScaffold(
    appViewModel: AppViewModel,
    onTimeChanged: (year: Int, month: Int) -> Unit,
    onMorningWeightChanged: (DayWeightData) -> Unit,
    onEveningWeightChanged: (DayWeightData) -> Unit,
    onStepsChanged: (DayStepsData) -> Unit,
) {

    var selectedYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var state by remember { mutableStateOf(AppScreenState.CALENDAR) }

    Scaffold(topBar = {
        Row {
            MonthSelector(selectedMonth, modifier = Modifier.weight(0.7f, true)) {
                selectedMonth = it
                onTimeChanged(selectedYear, selectedMonth)
            }
            YearSelector(selectedYear, modifier = Modifier.weight(0.3f, true)) {
                selectedYear = it
                onTimeChanged(selectedYear, selectedMonth)
            }
        }
    }, bottomBar = {
        BottomAppBar(actions = {
            IconButton(onClick = { state = AppScreenState.CALENDAR }) {
                Icon(
                    Icons.Filled.DateRange,
                    contentDescription = stringResource(R.string.weight_calendar_icon_desc)
                )
            }
            IconButton(onClick = { state = AppScreenState.CHART }) {
                Icon(
                    Icons.Filled.Info,
                    contentDescription = stringResource(R.string.charts_icon_desc)
                )
            }
            IconButton(onClick = { state = AppScreenState.SETTINGS }) {
                Icon(
                    Icons.Filled.Build,
                    contentDescription = stringResource(R.string.settings)
                )
            }
        })
    }) { innerPadding ->
        when (state) {
            AppScreenState.CALENDAR -> CalendarEditor(
                appViewModel = appViewModel,
                year = selectedYear,
                month = selectedMonth,
                onMorningWeightChanged = onMorningWeightChanged,
                onEveningWeightChanged = onEveningWeightChanged,
                onStepsChanged = onStepsChanged,
                modifier = Modifier.padding(innerPadding)
            )

            AppScreenState.CHART -> ChartDisplay(
                appViewModel = appViewModel,
                modifier = Modifier.padding(innerPadding)
            )

            AppScreenState.SETTINGS -> AppSettings(
                appViewModel = appViewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

/**
 * Simple drop box for the months with month names
 * @param currentMonth Current month
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthSelector(
    currentMonth: Int,
    modifier: Modifier = Modifier,
    monthSelected: (Int) -> Unit
) {
    var monthMenuExpanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = monthMenuExpanded,
        modifier = modifier,
        onExpandedChange = { monthMenuExpanded = !monthMenuExpanded }) {
        TextField(
            value = DateFormatSymbols().months[currentMonth],
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthMenuExpanded) },
            modifier = Modifier.menuAnchor()
        )
        ExposedDropdownMenu(expanded = monthMenuExpanded,
            onDismissRequest = { monthMenuExpanded = false }) {
            for (i in 0..11) {
                DropdownMenuItem(text = {
                    Text(
                        text = DateFormatSymbols().months[i]
                    )
                },
                    onClick = {
                        monthSelected(i)
                        monthMenuExpanded = false
                    })
            }
        }
    }
}

/**
 * Simple drop box for the year that starts with this year - 10 and goes until this year + 100
 * @param currentYear Current year
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearSelector(
    currentYear: Int,
    modifier: Modifier = Modifier,
    yearSelected: (Int) -> Unit
) {
    var monthMenuExpanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = monthMenuExpanded,
        modifier = modifier,
        onExpandedChange = { monthMenuExpanded = !monthMenuExpanded }) {
        TextField(
            value = currentYear.toString(),
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthMenuExpanded) },
            modifier = Modifier.menuAnchor()
        )
        ExposedDropdownMenu(expanded = monthMenuExpanded,
            onDismissRequest = { monthMenuExpanded = false }) {
            for (i in -10..100) {
                val year = Calendar.getInstance().get(Calendar.YEAR) + i
                DropdownMenuItem(text = {
                    Text(
                        text = (year).toString()
                    )
                }, onClick = {
                    yearSelected(year)
                    monthMenuExpanded = false
                })
            }
        }
    }
}