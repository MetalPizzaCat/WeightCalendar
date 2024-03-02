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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sofia.weightcalendar.components.GatedOutlineTextField
import com.sofia.weightcalendar.data.Entry
import com.sofia.weightcalendar.data.EntryDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
 */
@Composable
fun DataEntryRow(
    day: Entry,
    dayOfWeek: String?,
    targetStepCount: Int,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val entryDao = EntryDatabase.getInstance(context).entryDao()
    Row(modifier = Modifier.padding(5.dp)) {
        if (dayOfWeek == null) {
            Text(
                text = day.day.toString(),
                modifier = Modifier.weight(0.2f),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        } else {
            Column() {
                Text(
                    text = day.day.toString(),
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
            text = day.morningWeight?.toString() ?: "",
            label = { Text(stringResource(R.string.morning)) },
            modifier = Modifier.weight(0.3f)
        ) {
            scope.launch(Dispatchers.IO) {
                if (entryDao.exists(day.year, day.month, day.day)) {
                    entryDao.updateMorningWeight(
                        day.year,
                        day.month,
                        day.day,
                        it.toFloatOrNull()
                    )
                } else {
                    entryDao.insert(
                        Entry(
                            null,
                            day.day,
                            day.month,
                            day.year,
                            it.toFloatOrNull(),
                            null,
                            day.steps
                        )
                    )
                }
            }
        }
        GatedOutlineTextField(
            text = day.eveningWeight?.toString() ?: "",
            label = { Text(stringResource(R.string.evening)) },
            modifier = Modifier.weight(0.3f)
        ) {
            scope.launch(Dispatchers.IO) {
                if (entryDao.exists(day.year, day.month, day.day)) {
                    entryDao.updateEveningWeight(
                        day.year,
                        day.month,
                        day.day,
                        it.toFloatOrNull()
                    )
                } else {
                    entryDao.insert(
                        Entry(
                            null,
                            day.day,
                            day.month,
                            day.year,
                            null,
                            it.toFloatOrNull(),
                            day.steps
                        )
                    )
                }
            }
        }
        StepEntryField(
            label = stringResource(R.string.steps),
            currentValue = day.steps,
            targetValue = targetStepCount,
            modifier = Modifier.weight(0.3f),
            onValueChanged = {
                scope.launch(Dispatchers.IO) {
                    if (entryDao.exists(day.year, day.month, day.day)) {
                        entryDao.updateSteps(
                            day.year,
                            day.month,
                            day.day,
                            it
                        )
                    } else {
                        entryDao.insert(
                            Entry(
                                null,
                                day.day,
                                day.month,
                                day.year,
                                null,
                                null,
                                it
                            )
                        )
                    }
                }
            }
        )
    }
}

/**
 *  Component responsible for handling the day value entry for a month
 *  @param appViewModel Current view model that has access to the app data
 *  @param year Currently selected year
 *  @param month Currently selected month
 *  */
@Composable
fun CalendarEditor(
    appViewModel: AppViewModel,
    year: Int,
    month: Int,
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
                            day = day,
                            // to ensure that week days are properly named in every language
                            dayOfWeek = DateFormatSymbols().shortWeekdays[(YearMonth.of(
                                year,
                                month + 1
                            )
                                .atDay(day.day).dayOfWeek.value) % 7 + 1],
                            targetStepCount = minSteps,
                        )
                    }
                }
            }
        }
    }
}
