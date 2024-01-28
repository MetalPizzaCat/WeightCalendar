package com.sofia.weightcalendar

import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.vertical.VerticalAxis
import com.patrykandpatrick.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatrick.vico.core.context.MeasureContext

class WeightChartVerticalAxisPlacer(
    private val minWeight: Float = 50f,
    private val maxWeight: Float = 90f,
    private val stepWeight: Float = 0.5f
) : AxisItemPlacer.Vertical {
    override fun getLabelValues(
        context: ChartDrawContext,
        axisHeight: Float,
        maxLabelHeight: Float,
        position: AxisPosition.Vertical
    ): List<Float> {
        val labelList = mutableListOf<Float>()

        var currentValue = minWeight
        // kotlin doesn't have for loops with steps for floats for some reason
        while (currentValue <= maxWeight) {
            labelList.add(currentValue - minWeight)
            currentValue += stepWeight
        }
        return labelList
    }

    override fun getTopVerticalAxisInset(
        verticalLabelPosition: VerticalAxis.VerticalLabelPosition,
        maxLabelHeight: Float,
        maxLineThickness: Float
    ): Float {
        return AxisItemPlacer.Vertical.default()
            .getTopVerticalAxisInset(verticalLabelPosition, maxLabelHeight, maxLineThickness)
    }

    override fun getWidthMeasurementLabelValues(
        context: MeasureContext,
        axisHeight: Float,
        maxLabelHeight: Float,
        position: AxisPosition.Vertical
    ): List<Float> {
        return AxisItemPlacer.Vertical.default()
            .getWidthMeasurementLabelValues(context, axisHeight, maxLabelHeight, position)
    }

    override fun getBottomVerticalAxisInset(
        verticalLabelPosition: VerticalAxis.VerticalLabelPosition,
        maxLabelHeight: Float,
        maxLineThickness: Float
    ): Float {
        return AxisItemPlacer.Vertical.default()
            .getBottomVerticalAxisInset(verticalLabelPosition, maxLabelHeight, maxLineThickness)
    }

    override fun getHeightMeasurementLabelValues(
        context: MeasureContext,
        position: AxisPosition.Vertical
    ): List<Float> {
        val labelList = mutableListOf<Float>()

        var currentValue = minWeight
        // kotlin doesn't have for loops with steps for floats for some reason
        while (currentValue <= maxWeight) {
            labelList.add(currentValue - minWeight)
            currentValue += stepWeight
        }
        return labelList
    }
}