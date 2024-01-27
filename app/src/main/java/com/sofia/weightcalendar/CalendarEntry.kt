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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DateFormatSymbols
import java.time.YearMonth

@Composable
fun DataEntryRow(
    day: Int,
    dayOfWeek: String?,
    morningWeight: Float?,
    eveningWeight: Float?,
    onMorningWeightChanged: (Float) -> Unit,
    onEveningWeightChanged: (Float) -> Unit
) {
    var currentMorningWeight by remember { mutableStateOf((morningWeight ?: 0).toString()) }
    var currentEveningWeight by remember { mutableStateOf((eveningWeight ?: 0).toString()) }

    Row(modifier = Modifier.padding(5.dp)) {
        if (dayOfWeek == null) {
            Text(
                text = day.toString(),
                modifier = Modifier.weight(0.2f),
                fontSize = 24.sp
            )
        } else {
            Column {
                Text(
                    text = day.toString(),
                    fontSize = 24.sp
                )
                Text(
                    text = dayOfWeek,
                    fontSize = 15.sp
                )
            }
        }
        OutlinedTextField(
            label = { Text(stringResource(R.string.morning)) },
            value = currentMorningWeight,
            singleLine = true,
            textStyle = TextStyle.Default.copy(fontSize = 24.sp),
            onValueChange = { currentMorningWeight = it },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            modifier = Modifier
                .weight(0.3f)
                .onFocusChanged {
                    if (!it.isFocused) {
                        onMorningWeightChanged(currentMorningWeight.toFloatOrNull() ?: 0.0f)
                    }
                },
            keyboardActions = KeyboardActions(onDone = {
                onMorningWeightChanged(currentMorningWeight.toFloatOrNull() ?: 0.0f)
            })
        )
        OutlinedTextField(
            label = { Text(stringResource(R.string.evening)) },
            value = currentEveningWeight,
            singleLine = true,
            textStyle = TextStyle.Default.copy(fontSize = 24.sp),
            onValueChange = { currentEveningWeight = it },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            modifier = Modifier
                .weight(0.3f)
                .onFocusChanged {
                    if (!it.isFocused) {
                        onEveningWeightChanged(currentEveningWeight.toFloatOrNull() ?: 0.0f)
                    }

                },
            keyboardActions = KeyboardActions(onDone = {
                onEveningWeightChanged(currentEveningWeight.toFloatOrNull() ?: 0.0f)
            })
        )
    }
}

@Composable
fun CalendarEditor(
    appViewModel: AppViewModel,
    year: Int,
    month: Int,
    onMorningWeightChanged: (DayWeightData) -> Unit,
    onEveningWeightChanged: (DayWeightData) -> Unit,
    modifier: Modifier = Modifier
) {

    val entries by appViewModel.entriesForMonth(month, year).observeAsState()
    LazyColumn(modifier = modifier) {
        entries?.let {
            items(items = it, key = { entry -> entry.uid ?: -1 }) { day ->
                ElevatedCard {
                    DataEntryRow(
                        day = day.day,
                        dayOfWeek = DateFormatSymbols().shortWeekdays[YearMonth.of(year, month + 1)
                            .atDay(day.day).dayOfWeek.value],
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
                    )
                }
            }
        }
    }
}