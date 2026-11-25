package com.zuhdiarifazizi.projectakhirpapb.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraShapes
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraSpacing

@Composable
fun AuraCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = AuraShapes.medium
    val border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    if (onClick == null) {
        Surface(
            modifier = modifier,
            shape = shape,
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            border = border
        ) {
            Column(modifier = Modifier.padding(AuraSpacing.md), content = content)
        }
    } else {
        Surface(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            border = border
        ) {
            Column(modifier = Modifier.padding(AuraSpacing.md), content = content)
        }
    }
}
