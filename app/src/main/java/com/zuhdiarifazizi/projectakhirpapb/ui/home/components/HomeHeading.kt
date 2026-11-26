package com.zuhdiarifazizi.projectakhirpapb.ui.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraNavy
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraTextSecondary

@Composable
fun HomeHeading(
    title: String = "Mau pergi ke mana?",
    subtitle: String = "Pilih tujuan untuk hari ini.",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                heading()
            }
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(color = AuraNavy)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium.copy(color = AuraTextSecondary)
        )
    }
}
