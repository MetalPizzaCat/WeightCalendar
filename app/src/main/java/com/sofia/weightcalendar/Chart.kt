package com.sofia.weightcalendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import com.github.tehras.charts.line.LineChart
import com.github.tehras.charts.line.LineChartData
import com.github.tehras.charts.line.renderer.line.SolidLineDrawer
import com.github.tehras.charts.line.renderer.point.FilledCircularPointDrawer
import com.github.tehras.charts.line.renderer.xaxis.SimpleXAxisDrawer
import com.github.tehras.charts.piechart.animation.simpleChartAnimation
import com.sofia.weightcalendar.charts.SteppedYAxisDrawer
import com.sofia.weightcalendar.charts.processValuesByDay
import com.sofia.weightcalendar.charts.processValuesByWeek
import com.sofia.weightcalendar.charts.processValuesByYear

enum class ChartDurationType { DAILY, WEEKLY, MONTHLY }

private val chartDurationTypeNames: List<Int> =
    listOf(R.string.daily, R.string.weekly, R.string.monthly)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressChart(
    appViewModel: AppViewModel,
    year: Int,
    month: Int,
    modifier: Modifier = Modifier
) {
    val entries by appViewModel.entriesForYear(year).observeAsState()
    var durationTypeSelectorExpanded by remember { mutableStateOf(false) }
    var durationType by remember { mutableStateOf(ChartDurationType.DAILY) }

    val data = when (durationType) {
        ChartDurationType.DAILY -> listOf(
            LineChartData(
                points =
                processValuesByDay(
                    entries?.filter { it.month == month },
                    true
                ),
                lineDrawer = SolidLineDrawer(),
            ),
        )


        ChartDurationType.WEEKLY -> listOf(
            LineChartData(
                points =
                processValuesByWeek(
                    month,
                    year,
                    entries?.filter { it.month == month },
                    true
                ),
                lineDrawer = SolidLineDrawer(),
            ),
        )


        ChartDurationType.MONTHLY -> listOf(
            LineChartData(
                points =
                processValuesByYear(
                    year,
                    entries?.filter { it.month == month },
                    true
                ),
                lineDrawer = SolidLineDrawer(),
            )
        )
    }

    val hasEnoughData: Boolean = data[0].points.size > 1

    Column(modifier = modifier) {
        Row {
            ExposedDropdownMenuBox(expanded = durationTypeSelectorExpanded,
                modifier = modifier,
                onExpandedChange = {
                    durationTypeSelectorExpanded = !durationTypeSelectorExpanded
                }) {
                TextField(
                    value = stringResource(chartDurationTypeNames[durationType.ordinal]),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = durationTypeSelectorExpanded) },
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(expanded = durationTypeSelectorExpanded,
                    onDismissRequest = { durationTypeSelectorExpanded = false }) {
                    ChartDurationType.entries.forEach {
                        DropdownMenuItem(text = {
                            Text(
                                text = stringResource(chartDurationTypeNames[it.ordinal])
                            )
                        },
                            onClick = {
                                durationType = it
                                durationTypeSelectorExpanded = false
                            })
                    }
                }
            }

            if (hasEnoughData) {
                LineChart(
                    linesChartData = data,
                    modifier = Modifier.fillMaxSize(),
                    animation = simpleChartAnimation(),
                    pointDrawer = FilledCircularPointDrawer(),
                    xAxisDrawer = SimpleXAxisDrawer(),
                    yAxisDrawer = SteppedYAxisDrawer(),
                    horizontalOffset = 5f,
                )
            } else {
                Text(stringResource(R.string.not_enough_data_to_build_a_graph))
            }
        }
    }
}
