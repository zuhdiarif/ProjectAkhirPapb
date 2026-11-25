package com.zuhdiarifazizi.projectakhirpapb.ui.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
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
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ActivityCategory
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraCardBorder
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraCardBorderSelected
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraCardSelectedBg
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraIconContainerBg
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraNavy
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraSurface
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraTextPrimary
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraTextSecondary

@Composable
fun ActivityCategoryCard(
    category: ActivityCategory,
    isSelected: Boolean,
    onSelect: (ActivityCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val accessibilityDescription = buildString {
        append(category.title)
        append(". ")
        append(category.description)
        append(".")
        if (isSelected) {
            append(" Sedang dipilih.")
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 68.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = accessibilityDescription
                role = Role.Button
                selected = isSelected
            }
            .clickable(
                onClickLabel = "memilih kategori ${category.title}",
                role = Role.Button,
                onClick = { onSelect(category) }
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) AuraCardSelectedBg else AuraSurface
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.5.dp,
            color = if (isSelected) AuraCardBorderSelected else AuraCardBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Container
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AuraIconContainerBg),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = category.iconResId),
                    contentDescription = null, // decorative within merged card
                    colorFilter = ColorFilter.tint(AuraNavy),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title and Description
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = category.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AuraTextPrimary,
                        fontSize = 16.sp
                    )
                )
                Text(
                    text = category.description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = AuraTextSecondary,
                        fontSize = 14.sp
                    )
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Trailing action
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.actionText,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = AuraNavy,
                        fontSize = 14.sp
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_forward),
                    contentDescription = null,
                    tint = AuraNavy,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
