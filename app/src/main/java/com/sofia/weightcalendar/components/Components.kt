package com.sofia.weightcalendar.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp

/**
 * A composable object that wraps around the OutlinedTextField that only calls value change on Done or loss of focus
 * Text field is always one line
 * @param text Initial text
 * @param label Label component of the text field
 * @param emitChangeOnFocusLoss If true the change event will also emit when component looses focus
 */
@Composable
fun GatedOutlineTextField(
    text: String,
    label: @Composable (() -> Unit),
    modifier: Modifier = Modifier,
    emitChangeOnFocusLoss: Boolean = true,
    onValueChanged: (String) -> Unit
) {
    var value by remember { mutableStateOf(text) }
    OutlinedTextField(
        label = label,
        value = value,
        singleLine = true,
        textStyle = TextStyle.Default.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
        onValueChange = { value = it },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        ),
        modifier = modifier,
        keyboardActions = KeyboardActions(onDone = {
            onValueChanged(value)
        })
    )
}