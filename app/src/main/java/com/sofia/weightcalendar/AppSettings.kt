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


/**
 * Provides a menu for editing the app settings
 */
@Composable
fun AppSettings(appViewModel: AppViewModel, modifier: Modifier = Modifier) {
    val steps by appViewModel.getTargetSteps().collectAsState(initial = 5)
    var isEditingSteps by remember { mutableStateOf(false) }
    ElevatedCard(modifier = modifier) {
        Row {
            Text("${stringResource(R.string.min_steps)}: ", modifier = Modifier.weight(0.2f))
            // this was done mostly because the TextField was refusing to update when steps got updated
            // but i kept this also because it looks kinda nice
            if (isEditingSteps) {
                GatedOutlineTextField(
                    text = steps.toString(),
                    label = { Text(stringResource(R.string.min_steps)) },
                    modifier = Modifier.weight(0.6f)
                ) {
                    appViewModel.setTargetSteps(it.toIntOrNull() ?: 0)
                    isEditingSteps = false
                }
            } else {
                Text(steps.toString(), fontSize = 24.sp)
            }
            IconButton(
                onClick = { isEditingSteps = !isEditingSteps },
                modifier = Modifier.weight(0.2f)
            ) {
                Icon(
                    Icons.Filled.Create,
                    contentDescription = stringResource(R.string.settings)
                )
            }
        }

    }
}