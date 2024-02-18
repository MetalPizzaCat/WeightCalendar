package com.sofia.weightcalendar.charts

import com.github.tehras.charts.line.LineChartData
import com.sofia.weightcalendar.data.Entry
import java.time.YearMonth


/**
 * Converts entry list into chart entry list
 * Conversion happens by taking an average for each week, empty weeks being skipped
 * @param year Which year should this work with. Should be the year from the entries
 * @param month Which month this operates on, used for filtering out the values
 * @param morning Should it calculate morning or evening values
 */
fun processValuesByWeek(
    month: Int,
    year: Int,
    entries: List<Entry>?,
    morning: Boolean
): List<LineChartData.Point> {
    if (entries.isNullOrEmpty()) {
        return emptyList()
    }
    val targetEntries = entries.filter { it.month == month }
    val monthLen = YearMonth.of(year, month + 1).lengthOfMonth()
    val result: ArrayList<LineChartData.Point> = ArrayList()
    for (week in 1..monthLen step 7) {
        var currentWeight = 0f
        var currentValidEntryCount = 0
        val remainingDays = Integer.min(monthLen - week, 7)
        for (day in 1..remainingDays) {
            val currentId = week + day - 1
            val weight: Float? = if (morning) {
                targetEntries[currentId].morningWeight
            } else {
                targetEntries[currentId].eveningWeight
            }
            if (weight != null) {
                currentValidEntryCount++
                currentWeight += (weight)
            }
        }
        if (currentValidEntryCount > 0) {
            result.add(
                LineChartData.Point(
                    (currentWeight / currentValidEntryCount),
                    (week / 7 + 1).toString(),
                )
            )
        }
    }
    return result
}

/**
 * Converts entry list into chart entry list
 * Conversion happens by taking an average for each month, with empty months being skipped entirely
 * @param year Which year should this work with. Should be the year from the entries
 * @param morning Should it calculate morning or evening values
 */
fun processValuesByYear(
    year: Int,
    entries: List<Entry>?,
    morning: Boolean
): List<LineChartData.Point> {
    if (entries.isNullOrEmpty()) {
        return emptyList()
    }
    val result: ArrayList<LineChartData.Point> = ArrayList()
    for (month in 0..11) {
        val validEntries =
            if (morning) {
                entries.filter { it.year == year && it.month == month && it.morningWeight != null }
                    .map { it.morningWeight!! }
            } else {
                entries.filter { it.year == year && it.month == month && it.eveningWeight != null }
                    .map { it.eveningWeight!! }
            }
        val weight = validEntries.sum()
        val count = validEntries.count()
        if (count > 0) {
            result.add(LineChartData.Point(weight / count, (month + 1).toString()))
        }
    }
    return result
}

/**
 * Converts the entry list into chart entry list with day as the key and morning weight as value
 */
fun processValuesByDay(
    entries: List<Entry>?,
    morning: Boolean
): List<LineChartData.Point> =
    if (morning) {
        entries?.filter { it.morningWeight != null }
            ?.map { i -> LineChartData.Point(i.morningWeight!!, i.day.toString()) }
            ?: emptyList()
    } else {
        entries?.filter { it.eveningWeight != null }
            ?.map { i -> LineChartData.Point(i.eveningWeight!!, i.day.toString()) }
            ?: emptyList()
    }
