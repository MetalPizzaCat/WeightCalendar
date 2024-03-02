package com.sofia.weightcalendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.github.tehras.charts.line.LineChart
import com.github.tehras.charts.line.LineChartData
import com.github.tehras.charts.line.renderer.line.SolidLineDrawer
import com.github.tehras.charts.line.renderer.point.FilledCircularPointDrawer
import com.github.tehras.charts.line.renderer.xaxis.SimpleXAxisDrawer
import com.github.tehras.charts.piechart.animation.simpleChartAnimation
import com.sofia.weightcalendar.charts.NamedMonthsXAxisDrawer
import com.sofia.weightcalendar.charts.SteppedYAxisDrawer
import com.sofia.weightcalendar.charts.processValuesByDay
import com.sofia.weightcalendar.charts.processValuesByWeek
import com.sofia.weightcalendar.charts.processValuesByYear
import kotlin.math.max

enum class ChartDurationType { DAILY, WEEKLY, MONTHLY }

@Composable
fun ProgressChart(
    appViewModel: AppViewModel,
    year: Int,
    month: Int,
    modifier: Modifier = Modifier
) {
    val chartStep by appViewModel.getChartStep().collectAsState(initial = 0.5f)
    val entries by appViewModel.entriesForYear(year).observeAsState()


    val data = when (appViewModel.currentChartDurationType) {
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
                    entries,
                    true
                ),
                lineDrawer = SolidLineDrawer(),
            )
        )
    }

    val hasEnoughData: Boolean = data[0].points.size > 1

    Column(modifier = modifier) {


        if (hasEnoughData) {
            LineChart(
                linesChartData = data,
                modifier = Modifier.fillMaxSize(),
                animation = simpleChartAnimation(),
                pointDrawer = FilledCircularPointDrawer(),
                xAxisDrawer = if (appViewModel.currentChartDurationType == ChartDurationType.MONTHLY) {
                    NamedMonthsXAxisDrawer()
                } else {
                    SimpleXAxisDrawer()
                },
                yAxisDrawer = SteppedYAxisDrawer(
                    max(0.1f, chartStep),
                    labelTextColor = MaterialTheme.colorScheme.inversePrimary
                ),
                horizontalOffset = 5f,
            )
        } else {
            Text(stringResource(R.string.not_enough_data_to_build_a_graph))
        }
    }
}
