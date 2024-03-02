package com.sofia.weightcalendar

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sofia.weightcalendar.data.Entry
import com.sofia.weightcalendar.data.EntryDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DateFormatSymbols
import java.time.YearMonth
import java.util.Calendar

data class TabInfo(val text: String, val icon: ImageVector)

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

/**
 * Generates a new month data if month is not present. Exists to make sure that all days are filled
 * But also to avoid dealing with having objects being created by not quite in the columns
 * Shouldn't be an issue since it's just 31 entries added each time
 */
fun generateMonth(year: Int, month: Int, context: Context) {
    val entryDao = EntryDatabase.getInstance(context).entryDao()
    if (entryDao.monthExists(year, month)) {
        return
    }
    // YearMonth expects values in range on 1-12 while Calendar returns values in 0-11
    // that is stupid :3
    for (day in 1..YearMonth.of(year, month + 1).lengthOfMonth()) {
        entryDao.insert(Entry(null, day, month, year, null, null, null))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    appViewModel: AppViewModel,
) {

    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(floatingActionButton = {
        ExtendedFloatingActionButton(
            text = { Text(stringResource(R.string.settings)) },
            icon = { Icon(Icons.Filled.Settings, contentDescription = "Change date settings") },
            onClick = { showBottomSheet = true })
    }) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TopAppTabBar(
                currentTab = appViewModel.currentTab.ordinal, tabs = listOf(
                    TabInfo(stringResource(R.string.calendar), Icons.Default.DateRange),
                    TabInfo(stringResource(R.string.chart), Icons.Filled.Check),
                ), onSelected = {
                    appViewModel.setCurrentTab(it)
                }
            )

            when (appViewModel.currentTab) {
                AppTabs.CALENDAR -> CalendarEditor(
                    appViewModel = appViewModel,
                    year = appViewModel.selectedYear,
                    month = appViewModel.selectedMonth,
                )

                AppTabs.GRAPH -> ProgressChart(
                    appViewModel = appViewModel,
                    year = appViewModel.selectedYear,
                    month = appViewModel.selectedMonth,
                    modifier = Modifier.padding(5.dp)
                )
            }
        }
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
            ) {
                Column(
                    modifier = Modifier.padding(30.dp)
                ) {
                    Text(stringResource(R.string.current_date_range))
                    Row(modifier = Modifier) {
                        MonthSelector(
                            appViewModel.selectedMonth,
                            modifier = Modifier.weight(0.5f, true)
                        ) {
                            appViewModel.selectedMonth = it
                            scope.launch(Dispatchers.IO) {
                                generateMonth(
                                    appViewModel.selectedYear,
                                    appViewModel.selectedMonth,
                                    context
                                )
                            }
                        }
                        YearSelector(
                            appViewModel.selectedYear,
                            modifier = Modifier.weight(0.5f, true)
                        ) {
                            appViewModel.selectedYear = it
                            scope.launch(Dispatchers.IO) {
                                generateMonth(
                                    appViewModel.selectedYear,
                                    appViewModel.selectedMonth,
                                    context
                                )
                            }
                        }
                    }
                    Text(stringResource(R.string.chart_settings))
                    ChartDurationTypeSelector(viewModel = appViewModel)
                    Text(stringResource(R.string.general_settings))
                    AppSettings(
                        appViewModel = appViewModel,
                        modifier = Modifier
                    )
                }
            }

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