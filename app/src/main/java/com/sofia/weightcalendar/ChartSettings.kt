package com.sofia.weightcalendar

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

private val chartDurationTypeNames: List<Int> =
    listOf(R.string.daily, R.string.weekly, R.string.monthly)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartDurationTypeSelector(viewModel: AppViewModel, modifier: Modifier = Modifier) {
    var durationTypeSelectorExpanded by remember { mutableStateOf(false) }
    Row {
        ExposedDropdownMenuBox(expanded = durationTypeSelectorExpanded,
            modifier = modifier,
            onExpandedChange = {
                durationTypeSelectorExpanded = !durationTypeSelectorExpanded
            }) {
            TextField(
                value = stringResource(chartDurationTypeNames[viewModel.currentChartDurationType.ordinal]),
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
                            viewModel.currentChartDurationType = it
                            durationTypeSelectorExpanded = false
                        })
                }
            }
        }
    }
}