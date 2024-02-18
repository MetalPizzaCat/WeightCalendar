package com.sofia.weightcalendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sofia.weightcalendar.components.GatedOutlineTextField
import java.text.DateFormatSymbols
import java.time.YearMonth


/**
 * Template component for the entry field that will handle end of input both on Done action and loss of focus
 * @param label Label text for the TextField itself
 * @param currentValue Current input value for the field
 */
@Composable
fun StepEntryField(
    label: String,
    currentValue: Int?,
    targetValue: Int,
    modifier: Modifier = Modifier,
    onValueChanged: (Int?) -> Unit
) {
    var value by remember { mutableStateOf(currentValue) }

    val bgColor = if ((value ?: 0) > targetValue) {
        colorResource(R.color.green)
    } else {
        colorResource(R.color.red)
    }

    OutlinedTextField(
        label = { Text(label) },
        value = value?.toString() ?: "",
        singleLine = true,
        textStyle = TextStyle.Default.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
        onValueChange = { value = it.toIntOrNull() },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        ),
        colors = if (currentValue == null) {
            OutlinedTextFieldDefaults.colors()
        } else {
            OutlinedTextFieldDefaults.colors(
                focusedContainerColor = bgColor,
                unfocusedContainerColor = bgColor,
                unfocusedTextColor = colorResource(R.color.white),
                focusedTextColor = colorResource(R.color.white)
            )
        },
        modifier = modifier
            .onFocusChanged {
                if (!it.isFocused) {
                    onValueChanged(value)
                }
            },
        keyboardActions = KeyboardActions(onDone = {
            onValueChanged(value)
        })
    )
}

/**
 * Component that handles input for the entire day
 * @param day Day in month which will be displayed
 * @param dayOfWeek Which day name to display under number. If null nothing is displayed
 * @param morningWeight Current morning weight
 * @param eveningWeight Current evening weight
 */
@Composable
fun DataEntryRow(
    day: Int,
    dayOfWeek: String?,
    morningWeight: Float?,
    eveningWeight: Float?,
    steps: Int?,
    targetStepCount: Int,
    onMorningWeightChanged: (Float?) -> Unit,
    onEveningWeightChanged: (Float?) -> Unit,
    onStepCountChanged: (Int?) -> Unit
) {
    Row(modifier = Modifier.padding(5.dp)) {
        if (dayOfWeek == null) {
            Text(
                text = day.toString(),
                modifier = Modifier.weight(0.2f),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        } else {
            Column() {
                Text(
                    text = day.toString(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = dayOfWeek,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
        GatedOutlineTextField(
            text = morningWeight?.toString() ?: "",
            label = { Text(stringResource(R.string.morning)) },
            modifier = Modifier.weight(0.3f)
        ) {
            onMorningWeightChanged(it.toFloatOrNull())
        }
        GatedOutlineTextField(
            text = eveningWeight?.toString() ?: "",
            label = { Text(stringResource(R.string.evening)) },
            modifier = Modifier.weight(0.3f)
        ) {
            onEveningWeightChanged(it.toFloatOrNull())
        }
        StepEntryField(
            label = stringResource(R.string.steps),
            currentValue = steps,
            targetValue = targetStepCount,
            modifier = Modifier.weight(0.3f),
            onValueChanged = onStepCountChanged
        )
    }
}

/**
 *  Component responsible for handling the day value entry for a month
 *  @param appViewModel Current view model that has access to the app data
 *  @param year Currently selected year
 *  @param month Currently selected month
 *  @param onMorningWeightChanged Will be called when one of the child entry objects changes it's value
 *  @param onMorningWeightChanged Will be called when one of the child entry objects changes it's value
 *  */
@Composable
fun CalendarEditor(
    appViewModel: AppViewModel,
    year: Int,
    month: Int,
    onMorningWeightChanged: (DayWeightData) -> Unit,
    onEveningWeightChanged: (DayWeightData) -> Unit,
    onStepsChanged: (DayStepsData) -> Unit,
    modifier: Modifier = Modifier
) {

    val minSteps by appViewModel.getTargetSteps().collectAsState(initial = 0)
    val entries by appViewModel.entriesForMonth(month, year).observeAsState()
    LazyColumn(modifier = modifier) {
        entries?.let {
            items(items = it, key = { entry -> entry.uid ?: -1 }) { day ->
                // when switching from month with more days than the one we switched to
                // we run into the problem of LazyColumn still keeping them loaded and trying to updated them
                // but running outside of the day range
                // so we just do a sanity check to make sure that they day is in a valid range
                if (day.day <= YearMonth.of(year, month + 1).lengthOfMonth()) {
                    ElevatedCard {
                        DataEntryRow(
                            day = day.day,
                            // to ensure that week days are properly named in every language
                            dayOfWeek = DateFormatSymbols().shortWeekdays[(YearMonth.of(
                                year,
                                month + 1
                            )
                                .atDay(day.day).dayOfWeek.value) % 7 + 1],
                            steps = day.steps,
                            morningWeight = day.morningWeight,
                            eveningWeight = day.eveningWeight,
                            onEveningWeightChanged = { weight ->
                                onEveningWeightChanged(
                                    DayWeightData(
                                        year,
                                        month,
                                        day.day,
                                        weight
                                    )
                                )
                            },
                            targetStepCount = minSteps,
                            onMorningWeightChanged = { weight ->
                                onMorningWeightChanged(
                                    DayWeightData(
                                        year,
                                        month,
                                        day.day,
                                        weight
                                    )
                                )
                            },
                            onStepCountChanged = { steps ->
                                onStepsChanged(
                                    DayStepsData(
                                        year,
                                        month,
                                        day.day,
                                        steps
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}