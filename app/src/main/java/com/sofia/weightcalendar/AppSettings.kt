package com.sofia.weightcalendar

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.sofia.weightcalendar.components.GatedOutlineTextField
import kotlin.math.max


@Composable
fun NumberSettingEditor(initial: String, label: String, onChanged: (value: String) -> Unit) {
    var isEditing by remember { mutableStateOf(false) }
    Row {
        Text(label, modifier = Modifier.weight(0.2f))
        // this was done mostly because the TextField was refusing to update when steps got updated
        // but i kept this also because it looks kinda nice
        if (isEditing) {
            GatedOutlineTextField(
                text = initial,
                label = { Text(stringResource(R.string.min_steps)) },
                emitChangeOnFocusLoss = false,
                modifier = Modifier.weight(0.6f)
            ) {
                onChanged(it)
                isEditing = false
            }
        } else {
            Text(initial, fontSize = 24.sp)
        }
        IconButton(
            onClick = { isEditing = !isEditing },
            modifier = Modifier.weight(0.2f)
        ) {
            Icon(
                Icons.Filled.Create,
                contentDescription = stringResource(R.string.settings)
            )
        }
    }
}

/**
 * Provides a menu for editing the app settings
 */
@Composable
fun AppSettings(appViewModel: AppViewModel, modifier: Modifier = Modifier) {
    val steps by appViewModel.getTargetSteps().collectAsState(initial = 5)
    val chartStep by appViewModel.getChartStep().collectAsState(initial = 0.5f)

    ElevatedCard(modifier = modifier) {

        NumberSettingEditor(
            label = "${stringResource(R.string.min_steps)}: ",
            initial = steps.toString()
        ) {
            appViewModel.setTargetSteps(max(it.toIntOrNull() ?: 0, 0))
        }
        NumberSettingEditor(
            label = "${stringResource(R.string.chart_step_label)}: ",
            initial = chartStep.toString()
        ) {
            appViewModel.setChartStep(max(it.toFloatOrNull() ?: 0f, 0.1f))
        }
    }
}