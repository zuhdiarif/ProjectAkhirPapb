package com.zuhdiarifazizi.projectakhirpapb.ui.manageclothes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zuhdiarifazizi.projectakhirpapb.R
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraSecondaryButton
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraHighContrastInk
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraHintBannerBg
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraNavy
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraNavyDeep
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraSpacing
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraTextSecondary
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraWarmSurface

private data class GuideStep(val number: Int, val instruction: String)

private val ADD_CLOTHING_STEPS = listOf(
    GuideStep(1, "Letakkan pakaian mendatar di atas meja atau tempat tidur."),
    GuideStep(2, "Ratakan semua kerutan dan lipatan kain."),
    GuideStep(3, "Pastikan seluruh bagian pakaian terlihat jelas di layar."),
    GuideStep(4, "Pegang ponsel sejajar tepat di atas pakaian.")
)

@Composable
fun AddClothingGuideScreen(
    onBack: () -> Unit,
    onProfileClick: () -> Unit,
    onStartCamera: () -> Unit,
    onPickFromGallery: () -> Unit,
    onManualForm: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AuraWarmSurface)
    ) {
        Surface(
            color = AuraWarmSurface.copy(alpha = 0.95f),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = AuraSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .semantics {
                                contentDescription = "Kembali ke layar sebelumnya"
                                role = Role.Button
                            }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = null,
                            tint = AuraHighContrastInk,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = "Tambah Baju Baru",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = AuraHighContrastInk
                        ),
                        modifier = Modifier.semantics { heading() }
                    )
                }

                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier
                        .padding(end = AuraSpacing.sm)
                        .size(36.dp)
                        .background(AuraNavyDeep, RoundedCornerShape(10.dp))
                        .semantics {
                            contentDescription = "Profil pengguna"
                            role = Role.Button
                        }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_profile),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = AuraSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AuraSpacing.md)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(AuraSpacing.xs)) {
                Text(
                    text = "Tambah baju baru",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp,
                        color = AuraHighContrastInk
                    ),
                    modifier = Modifier.semantics { heading() }
                )
                Text(
                    text = "Ikuti empat langkah mudah untuk memfoto pakaian Anda:",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 18.sp,
                        color = AuraTextSecondary
                    )
                )
            }

            ADD_CLOTHING_STEPS.forEach { step ->
                HighContrastStepCard(step = step)
            }

            Surface(
                color = AuraHintBannerBg,
                border = BorderStroke(2.dp, AuraHighContrastInk),
                shape = RoundedCornerShape(2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics(mergeDescendants = true) {
                        contentDescription = "Panduan suara akan berbunyi saat pakaian sudah terdeteksi."
                    }
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_volume_up),
                        contentDescription = null,
                        tint = AuraNavy,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Panduan suara akan berbunyi saat pakaian sudah terdeteksi.",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 17.sp,
                            color = AuraHighContrastInk
                        )
                    )
                }
            }

            Spacer(Modifier.height(AuraSpacing.xs))

            Button(
                onClick = onStartCamera,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 64.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AuraNavy,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_clothes),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.size(12.dp))
                Text(
                    text = "Ambil Foto",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AuraSpacing.sm)
            ) {
                AuraSecondaryButton(
                    text = "Galeri",
                    onClick = onPickFromGallery,
                    modifier = Modifier.weight(1f)
                )
                AuraSecondaryButton(
                    text = "Isi Manual",
                    onClick = onManualForm,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(AuraSpacing.md))
        }
    }
}

@Composable
private fun HighContrastStepCard(step: GuideStep) {
    Surface(
        color = Color.White,
        border = BorderStroke(2.dp, AuraHighContrastInk),
        shape = RoundedCornerShape(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = "Langkah ${step.number}: ${step.instruction}"
            }
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(AuraHighContrastInk),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = step.number.toString(),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = AuraWarmSurface
                    )
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "LANGKAH ${step.number}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        letterSpacing = 0.8.sp,
                        color = AuraHighContrastInk
                    )
                )
                Text(
                    text = step.instruction,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 17.sp,
                        lineHeight = 25.sp,
                        color = AuraHighContrastInk
                    )
                )
            }
        }
    }
}
