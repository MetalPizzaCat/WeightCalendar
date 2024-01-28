package com.sofia.weightcalendar

import android.graphics.Typeface
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.shapeComponent
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.compose.dimensions.dimensionsOf
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.axis.formatter.DecimalFormatAxisValueFormatter
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.composed.ComposedChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import com.patrykandpatrick.vico.core.extension.floor
import com.patrykandpatrick.vico.core.extension.sumByFloat
import com.sofia.weightcalendar.data.Entry
import java.lang.Float.max
import java.lang.Integer.min
import java.text.DateFormatSymbols
import java.time.YearMonth
import kotlin.math.absoluteValue


/***
 * List of colors that should be used for chart generation
 */
private val chartColors: List<Color> = listOf(
    Color(0x3fb8a2ff),
    Color(0x663fb8ff),
    Color(0xb83f55ff),
    Color(0x92b83fff)
)

private val bottomAxisValueFormatter =
    AxisValueFormatter<AxisPosition.Horizontal.Bottom> { x, _ -> DateFormatSymbols().months[x.toInt() - 1] }

private const val minDaysForWeekCalculation: Int = 12

/**
 * Converts entry list into chart entry list
 * Conversion happens by taking an average for each week, empty weeks being skipped
 * @param year Which year should this work with. Should be the year from the entries
 * @param month Which month this operates on, used for filtering out the values
 * @param morning Should it calculate morning or evening values
 * @param offset A value that will be subtracted from the weight to keep it closer to the start of the chart
 */
internal fun processValuesByWeek(
    month: Int,
    year: Int,
    offset: Float = 50f,
    entries: List<Entry>?,
    morning: Boolean
): List<FloatEntry> {
    if (entries.isNullOrEmpty()) {
        return emptyList()
    }
    val targetEntries = entries.filter { it.month == month }
    val monthLen = YearMonth.of(year, month + 1).lengthOfMonth()
    val result: ArrayList<FloatEntry> = ArrayList()
    for (week in 1..monthLen step 7) {
        var currentWeight = 0f
        var currentValidEntryCount = 0
        val remainingDays = min(monthLen - week, 7)
        for (day in 1..remainingDays) {
            val currentId = week + day - 1
            val weight: Float? = if (morning) {
                targetEntries[currentId].morningWeight
            } else {
                targetEntries[currentId].eveningWeight
            }
            if (weight != null) {
                currentValidEntryCount++
                currentWeight += (weight - offset)
            }
        }
        if (currentValidEntryCount > 0) {
            result.add(entryOf(week / 7 + 1, (currentWeight / currentValidEntryCount)))
        }
    }
    return result
}

/**
 * Converts entry list into chart entry list
 * Conversion happens by taking an average for each month, with empty months being skipped entirely
 * @param year Which year should this work with. Should be the year from the entries
 * @param morning Should it calculate morning or evening values
 * @param offset A value that will be subtracted from the weight to keep it closer to the start of the chart
 */
internal fun processValuesByYear(
    year: Int,
    offset: Float,
    entries: List<Entry>?,
    morning: Boolean
): List<FloatEntry> {
    if (entries.isNullOrEmpty()) {
        return emptyList()
    }
    val result: ArrayList<FloatEntry> = ArrayList()
    for (month in 0..11) {
        val validEntries =
            if (morning) {
                entries.filter { it.year == year && it.month == month && it.morningWeight != null }
                    .map { it.morningWeight!! }
            } else {
                entries.filter { it.year == year && it.month == month && it.eveningWeight != null }
                    .map { it.eveningWeight!! }
            }
        val weight = validEntries.sumByFloat { it - offset }
        val count = validEntries.count()
        if (count > 0) {
            result.add(entryOf(month + 1, weight / count))
        }
    }
    return result
}

/**
 * Converts the entry list into chart entry list with day as the key and morning weight as value
 * @param offset A value that will be subtracted from the weight to keep it closer to the start of the chart
 */
internal fun processValuesByDay(
    offset: Float,
    entries: List<Entry>?,
    morning: Boolean
): List<FloatEntry> =
    if (morning) {
        entries?.filter { it.morningWeight != null }
            ?.map { i -> entryOf(i.day, i.morningWeight!! - offset) } ?: emptyList()
    } else {
        entries?.filter { it.eveningWeight != null }
            ?.map { i -> entryOf(i.day, i.eveningWeight!! - offset) } ?: emptyList()
    }


/**
 * Calculates the max possible value for the chart based on the lowest value in the entry
 */
internal fun getChartUpperLimit(default: Float = 200f, entries: List<Float?>?): Float {
    if (entries.isNullOrEmpty()) {
        return default
    }
    if (entries.filterNotNull().isEmpty()) {
        return default
    }
    return max(default, entries.filterNotNull().max())
}


/**
 * Calculates the min possible value for the chart based on the lowest value in the entry
 */
internal fun getChartLowerLimit(default: Float = 50f, entries: List<Float?>?): Float {
    if (entries.isNullOrEmpty()) {
        return default
    }
    if (entries.filterNotNull().isEmpty()) {
        return default
    }
    return (kotlin.math.max(
        default,
        entries.filterNotNull().min().floor
    ).absoluteValue / 5).floor * 5
}

/**
 * Element that displays chart based on the values from the database
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartDisplay(
    appViewModel: AppViewModel,
    year: Int,
    month: Int,
    modifier: Modifier = Modifier
) {

    // Note: i absolutely hate how the vico library works and as such most of the code was written when i was quite annoyed
    // there are probably a lot of inefficiencies but i do not care

    val entries by appViewModel.entriesForYear(year).observeAsState()
    var morningSelected by remember { mutableStateOf(true) }
    var calcByMonth by remember { mutableStateOf(true) }
    var eveningSelected by remember { mutableStateOf(false) }

    val minWeight = getChartLowerLimit(entries = entries?.map { it.morningWeight })
    val maxWeight = getChartUpperLimit(entries = entries?.map { it.morningWeight })
    val currentMorningEntriesForMonth =
        entries?.count { it.month == month && it.morningWeight != null } ?: 0
    val currentEveningEntriesForMonth =
        entries?.count { it.month == month && it.morningWeight != null } ?: 0
    val data = ComposedChartEntryModelProducer.build {
        if (calcByMonth) {
            // because there is a period where user has not put enough for the graph to show
            // we make a temp graph that only shows values for this day
            if (
                (currentMorningEntriesForMonth < minDaysForWeekCalculation && morningSelected) ||
                (currentEveningEntriesForMonth < minDaysForWeekCalculation && eveningSelected)
            ) {
                if (morningSelected) {
                    add(processValuesByDay(minWeight, entries?.filter { it.month == month }, true))
                }
                if (eveningSelected) {
                    add(processValuesByDay(minWeight, entries?.filter { it.month == month }, false))
                }
            } else {
                if (morningSelected) {
                    add(processValuesByWeek(month, year, minWeight, entries, true))
                }
                if (eveningSelected) {
                    add(processValuesByWeek(month, year, minWeight, entries, false))
                }
            }
        } else {
            if (morningSelected) {
                add(processValuesByYear(year, minWeight, entries, true))
            }
            if (eveningSelected) {
                add(processValuesByYear(year, minWeight, entries, false))
            }
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
            FilterChip(selected = calcByMonth,
                leadingIcon = if (calcByMonth) {
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
                onClick = { calcByMonth = !calcByMonth },
                label = {
                    Text(
                        stringResource(R.string.per_week)
                    )
                })
        }
        ProvideChartStyle(rememberChartStyle(chartColors)) {
            Chart(
                chart = lineChart(),
                chartModelProducer = data,
                startAxis = rememberStartAxis(
                    valueFormatter = { x, _ -> (x + minWeight).toString() },
                    itemPlacer = WeightChartVerticalAxisPlacer(
                        minWeight,
                        maxWeight,
                        if (((maxWeight - minWeight) * 2f) >= 40) {
                            1f
                        } else {
                            0.5f
                        },
                    ),
                ),

                bottomAxis = rememberBottomAxis(
                    valueFormatter = if (calcByMonth) {
                        DecimalFormatAxisValueFormatter()
                    } else {
                        bottomAxisValueFormatter
                    },
                    title =
                    if (calcByMonth) {
                        if (currentMorningEntriesForMonth < minDaysForWeekCalculation) {
                            stringResource(R.string.day)
                        } else {
                            stringResource(R.string.week)
                        }
                    } else {
                        stringResource(R.string.month_axis_label)
                    },
                    titleComponent = textComponent(
                        color = Color.Black,
                        background = shapeComponent(Shapes.pillShape, Color(0xffffbb00)),
                        typeface = Typeface.MONOSPACE,
                        padding = dimensionsOf(5.dp),
                        margins = dimensionsOf(5.dp),
                    ),
                ),
                isZoomEnabled = true,
                modifier = modifier
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .background(Color.LightGray),
            )
        }
    }
}