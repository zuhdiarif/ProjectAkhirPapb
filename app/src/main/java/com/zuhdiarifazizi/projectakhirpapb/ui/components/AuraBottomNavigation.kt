package com.zuhdiarifazizi.projectakhirpapb.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zuhdiarifazizi.projectakhirpapb.R
import com.zuhdiarifazizi.projectakhirpapb.ui.navigation.AuraDestination
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraCardBorder
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraNavy
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraSurface
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraTextMuted

data class AuraNavItem(
    val destination: AuraDestination,
    val label: String,
    val iconResId: Int
)

@Composable
fun AuraBottomNavigation(
    currentDestination: AuraDestination,
    onNavigate: (AuraDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        AuraNavItem(
            destination = AuraDestination.HOME,
            label = "Pilih Pakaian",
            iconResId = R.drawable.ic_clothes
        ),
        AuraNavItem(
            destination = AuraDestination.HISTORY,
            label = "Riwayat",
            iconResId = R.drawable.ic_history
        ),
        AuraNavItem(
            destination = AuraDestination.MANAGE_CLOTHES,
            label = "Kelola Baju",
            iconResId = R.drawable.ic_wardrobe
        )
    )

    Surface(
        color = AuraSurface,
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Column {
            HorizontalDivider(
                color = AuraCardBorder.copy(alpha = 0.6f),
                thickness = 1.dp
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 3.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = currentDestination == item.destination ||
                            (item.destination == AuraDestination.HOME && currentDestination == AuraDestination.PICK_OUTFIT)

                    val accessibilityText = "${item.label}. Tab ${index + 1} dari ${items.size}. " +
                            if (isSelected) "Dipilih." else "Tidak dipilih. Ketuk dua kali untuk berpindah."

                    val tintColor = if (isSelected) AuraNavy else AuraTextMuted
                    val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 56.dp)
                            .semantics(mergeDescendants = true) {
                                contentDescription = accessibilityText
                                role = Role.Tab
                                selected = isSelected
                            }
                            .clickable(
                                onClickLabel = "Pindah ke tab ${item.label}",
                                role = Role.Tab,
                                onClick = { onNavigate(item.destination) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = item.iconResId),
                                contentDescription = null,
                                tint = tintColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontSize = 12.sp,
                                    fontWeight = fontWeight,
                                    color = tintColor
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
