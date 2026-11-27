package com.zuhdiarifazizi.projectakhirpapb.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.VisualTransformation
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraBodyTextStyle
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraHelperTextStyle
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraShapes

@Composable
fun AuraTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    helperText: String? = null,
    errorMessage: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingAction: (@Composable () -> Unit)? = null
) {
    val supportMessage = errorMessage ?: helperText
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                if (errorMessage != null) error(errorMessage)
            },
        enabled = enabled,
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        isError = errorMessage != null,
        label = { Text(label) },
        placeholder = placeholder?.let { hint -> ({ Text(hint) }) },
        trailingIcon = trailingAction,
        supportingText = supportMessage?.let { message ->
            {
                Text(
                    text = message,
                    style = AuraHelperTextStyle,
                    color = if (errorMessage != null) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        textStyle = AuraBodyTextStyle,
        shape = AuraShapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            errorBorderColor = MaterialTheme.colorScheme.error
        )
    )
}
