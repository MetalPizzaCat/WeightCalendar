package com.sofia.weightcalendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.composed.ComposedChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import com.sofia.weightcalendar.data.Entry
import java.lang.Integer.min
import java.time.YearMonth


fun processValuesByWeek(entries: List<Entry>, morning: Boolean): List<FloatEntry> {
    if (entries.isEmpty()) {
        return emptyList()
    }
    val monthLen = YearMonth.of(entries.first().year, entries.first().month + 1).lengthOfMonth()
    val result: ArrayList<FloatEntry> = ArrayList()
    for (week in 1..monthLen step 7) {
        var currentWeight = 0f
        var currentValidEntryCount = 0
        val remainingDays = min(monthLen - week, 7)
        for (day in 1..remainingDays) {
            val currentId = week + day - 1
            val weight: Float? = if (morning) {
                entries[currentId].morningWeight
            } else {
                entries[currentId].eveningWeight
            }
            if (weight != null) {
                currentValidEntryCount++
                currentWeight += weight
            }
        }
        if (currentValidEntryCount > 0) {
            result.add(entryOf(week / 7 + 1, currentWeight))
        }
    }
    return result
}

fun processValueByDay(entries: List<Entry>): List<FloatEntry> =
    entries.map { i -> entryOf(i.day, i.morningWeight ?: 0f) }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartDisplay(appViewModel: AppViewModel, modifier: Modifier = Modifier) {

    val entries by appViewModel.entriesForMonth(1, 2024).observeAsState()
    var morningSelected by remember { mutableStateOf(true) }
    var eveningSelected by remember { mutableStateOf(false) }

    val data = ComposedChartEntryModelProducer.build {
        if (morningSelected) {
            add(processValuesByWeek(entries ?: emptyList(), true))
        }
        if (eveningSelected) {
            add(processValuesByWeek(entries ?: emptyList(), false))
        }
    }
    Column(modifier = modifier) {
        Row {
            FilterChip(selected = morningSelected,
                leadingIcon = if (morningSelected) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = "Done icon",
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else {
                    null
                },
                onClick = { morningSelected = !morningSelected },
                label = {
                    Text(
                        stringResource(R.string.morning)
                    )
                })
            FilterChip(selected = eveningSelected,
                leadingIcon = if (eveningSelected) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = "Done icon",
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else {
                    null
                },
                onClick = { eveningSelected = !eveningSelected },
                label = {
                    Text(
                        stringResource(R.string.evening)
                    )
                })
        }
        Chart(
            chart = lineChart(),
            chartModelProducer = data,
            startAxis = rememberStartAxis(),
            bottomAxis = rememberBottomAxis(),
            modifier = modifier
        )
    }

}