package com.zuhdiarifazizi.projectakhirpapb.ui.components

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraButtonTextStyle
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraErrorRed
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraShapes
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraTextPrimary

@Composable
fun AuraPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) = AuraFilledButton(text, onClick, modifier, enabled, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary)

@Composable
fun AuraDestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) = AuraFilledButton(text, onClick, modifier, enabled, AuraErrorRed, Color.White)

@Composable
fun AuraSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.defaultMinSize(minHeight = 56.dp),
        shape = AuraShapes.medium,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = AuraTextPrimary)
    ) {
        Text(text = text, style = AuraButtonTextStyle)
    }
}

@Composable
private fun AuraFilledButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    containerColor: Color,
    contentColor: Color
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.defaultMinSize(minHeight = 56.dp),
        shape = AuraShapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Text(text = text, style = AuraButtonTextStyle)
    }
}
