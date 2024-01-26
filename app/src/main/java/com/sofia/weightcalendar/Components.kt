package com.sofia.weightcalendar

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sofia.weightcalendar.data.Entry
import java.text.DateFormatSymbols
import java.time.YearMonth
import java.util.Calendar


@Composable
fun DataEntryRow(
    entry: Entry,
    onMorningWeightChanged: (DayWeightData) -> Unit,
    onEveningWeightChanged: (DayWeightData) -> Unit
) {
    var morningWeight by remember { mutableStateOf((entry.morningWeight ?: 0).toString()) }
    var eveningWeight by remember { mutableStateOf((entry.eveningWeight ?: 0).toString()) }

    Row(modifier = Modifier.padding(5.dp)) {
        Text(
            text = entry.day.toString(),
            modifier = Modifier.weight(0.1f),
            fontSize = 24.sp
        )
        Text(
            text = stringResource(R.string.morning),
            modifier = Modifier.weight(0.2f)
        )
        TextField(
            value = morningWeight,
            singleLine = true,
            onValueChange = { morningWeight = it },
            modifier = Modifier
                .weight(0.2f),
            keyboardActions = KeyboardActions(onDone = {
                onMorningWeightChanged(
                    DayWeightData(
                        entry.year,
                        entry.month,
                        entry.day,
                        morningWeight.toFloatOrNull() ?: 0.0f
                    )
                )
            })
        )
        Text(
            text = stringResource(R.string.evening),
            modifier = Modifier.weight(0.2f)
        )
        TextField(
            value = eveningWeight,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            onValueChange = { eveningWeight = it },
            modifier = Modifier
                .weight(0.2f),
            keyboardActions = KeyboardActions(onDone = {
                onEveningWeightChanged(
                    DayWeightData(
                        entry.year,
                        entry.month,
                        entry.day,
                        eveningWeight.toFloatOrNull() ?: 0.0f
                    )
                )
            })
        )
    }
}

@Composable
fun AppScaffold(
    appViewModel: AppViewModel,
    onTimeChanged: (year: Int, month: Int) -> Unit,
    onMorningWeightChanged: (DayWeightData) -> Unit,
    onEveningWeightChanged: (DayWeightData) -> Unit
) {

    var selectedYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    val entries by appViewModel.entriesForMonth(selectedMonth, selectedYear).observeAsState()

    Scaffold(topBar = {
        Row {
            MonthSelector(selectedMonth, selectedYear, modifier = Modifier.weight(0.7f)) {
                selectedMonth = it
                onTimeChanged(selectedYear, selectedMonth)
            }
            YearSelector(selectedYear, modifier = Modifier.weight(0.3f)) {
                selectedYear = it
                onTimeChanged(selectedYear, selectedMonth)
            }
        }
    }, bottomBar = {
        BottomAppBar(actions = {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    Icons.Filled.DateRange,
                    contentDescription = stringResource(R.string.weight_calendar_icon_desc)
                )
            }
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    Icons.Filled.Info,
                    contentDescription = stringResource(R.string.charts_icon_desc)
                )
            }
        })
    }) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            entries?.let {
                items(it) { day ->
                    ElevatedCard {
                        DataEntryRow(
                            entry = day, onMorningWeightChanged, onEveningWeightChanged
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthSelector(
    currentMonth: Int,
    selectedYear: Int,
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
                DropdownMenuItem(text = { Text(text = YearMonth.of(selectedYear, i + 1).month.name) },
                    onClick = {
                        monthSelected(i)
                        monthMenuExpanded = false
                    })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearSelector(currentYear: Int, modifier: Modifier = Modifier, yearSelected: (Int) -> Unit) {
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