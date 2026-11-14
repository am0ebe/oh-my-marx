package de.strix.marxversion.ui.ds

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle

/**
 * ✏️ text field (design_system.md § 7 inputs): paperHi · 1dp `outline` · r12 · label above (label-m) · focus = 2dp blue
 * error = redDeep border (caller adds ⚠ + words) · L3 @1114 (circle found / join / composer) — generic, reuse i oth lanes
 */
@Composable
fun DsField(
    value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier, label: String? = null, placeholder: String? = null,
    singleLine: Boolean = true, maxLines: Int = if (singleLine) 1 else 4, isError: Boolean = false, enabled: Boolean = true,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge, keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) = Column(modifier) {
    label?.let { Text(it, color = Ds.ink, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = Space.xs)) }
    OutlinedTextField(
        value, onChange, singleLine = singleLine, maxLines = maxLines, isError = isError, enabled = enabled,
        textStyle = textStyle.copy(color = Ds.ink), keyboardOptions = keyboardOptions,
        shape = RoundedCornerShape(Radius.m),
        placeholder = placeholder?.let { p -> { Text(p, style = textStyle, color = Ds.inkMuted) } },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Ds.paperHi, unfocusedContainerColor = Ds.paperHi, disabledContainerColor = Ds.paperHi,
            errorContainerColor = Ds.paperHi,
            focusedBorderColor = Ds.blue, unfocusedBorderColor = Ds.outline, errorBorderColor = Ds.redDeep,
            cursorColor = Ds.blue, focusedTextColor = Ds.ink, unfocusedTextColor = Ds.ink,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}
