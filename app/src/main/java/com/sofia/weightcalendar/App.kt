package com.sofia.weightcalendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import java.text.DateFormatSymbols
import java.util.Calendar

data class TabInfo(val text: String, val icon: ImageVector) {}

@Composable
fun TopAppTabBar(
    currentTab: Int,
    tabs: List<TabInfo>,
    modifier: Modifier = Modifier,
    onSelected: (id: Int) -> Unit
) {
    TabRow(selectedTabIndex = currentTab, modifier = modifier) {
        tabs.forEachIndexed { id, info ->
            Tab(
                text = { Text(info.text) },
                selected = currentTab == id,
                onClick = { onSelected(id) },
                icon = { Icon(imageVector = info.icon, contentDescription = "Tab icon") }
            )
        }
    }
}

@Composable
fun AppScaffold(
    appViewModel: AppViewModel,
    onTimeChanged: (year: Int, month: Int) -> Unit,
    onMorningWeightChanged: (DayWeightData) -> Unit,
    onEveningWeightChanged: (DayWeightData) -> Unit,
    onStepsChanged: (DayStepsData) -> Unit,
) {

    var selectedYear by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.MONTH)) }

    Column {
        TopAppTabBar(
            currentTab = appViewModel.currentTab.ordinal, tabs = listOf(
                TabInfo(stringResource(R.string.calendar), Icons.Default.DateRange),
                TabInfo(stringResource(R.string.chart), Icons.Filled.Check),
                TabInfo(stringResource(R.string.settings), Icons.Filled.Build)
            ), onSelected = {
                appViewModel.setCurrentTab(it)
            }
        )
        if (appViewModel.currentTab != AppTabs.SETTINGS) {
            Row(modifier = Modifier) {
                MonthSelector(selectedMonth, modifier = Modifier.weight(0.5f, true)) {
                    selectedMonth = it
                    onTimeChanged(selectedYear, selectedMonth)
                }
                YearSelector(selectedYear, modifier = Modifier.weight(0.5f, true)) {
                    selectedYear = it
                    onTimeChanged(selectedYear, selectedMonth)
                }
            }
        }
        when (appViewModel.currentTab) {
            AppTabs.CALENDAR -> CalendarEditor(
                appViewModel = appViewModel,
                year = selectedYear,
                month = selectedMonth,
                onMorningWeightChanged = onMorningWeightChanged,
                onEveningWeightChanged = onEveningWeightChanged,
                onStepsChanged = onStepsChanged,
            )

            AppTabs.GRAPH -> ProgressChart(
                appViewModel = appViewModel,
                year = selectedYear,
                month = selectedMonth
            )

            AppTabs.SETTINGS -> AppSettings(
                appViewModel = appViewModel,
                modifier = Modifier
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