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
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraCard
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraPrimaryButton
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraSecondaryButton
import com.zuhdiarifazizi.projectakhirpapb.ui.home.components.VoiceActionButton
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraHighContrastInk
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraHintBannerBg
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraIndigoSoft
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraNavy
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraNavyDeep
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraSpacing
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraTextSecondary
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraWarmSurface

private data class GuideStep(val number: Int, val instruction: String)

private val VIDEO_GUIDE_STEPS = listOf(
    GuideStep(1, "Bentangkan pakaian secukupnya; lipatan kecil atau jarak agak dekat tetap aman."),
    GuideStep(2, "Arahkan kamera ke bagian pakaian; panduan suara tenang akan memandu perlahan."),
    GuideStep(3, "Tahan sebentar saat terdeteksi atau tekan Ambil Foto Sekarang kapan saja.")
)

private val RFID_ADD_STEPS = listOf(
    GuideStep(1, "Tempelkan stiker atau gantungan tag RFID 13,56 MHz pada bagian dalam kerah/pinggang pakaian."),
    GuideStep(2, "Tekan Tambah dengan Scan RFID di bawah, lalu tempelkan tag ke belakang ponsel hingga bergetar."),
    GuideStep(3, "Setelah UID tag tertaut otomatis, lengkapi warna/foto bila perlu lalu tekan Simpan Pakaian.")
)

@Composable
fun AddClothingGuideScreen(
    isVoiceListening: Boolean = false,
    partialTranscript: String = "",
    onVoiceClick: () -> Unit = {},
    onSpeakGuide: () -> Unit = {},
    onBack: () -> Unit,
    onStartCamera: () -> Unit,
    onStartRfidAdd: () -> Unit = {},
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .semantics {
                            contentDescription = "Kembali ke daftar pakaian"
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
                    text = "Pilih Cara Tambah Baju",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 21.sp,
                        color = AuraHighContrastInk
                    ),
                    modifier = Modifier.semantics { heading() }
                )
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
                    text = "3 Opsi Tambah Baju",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        color = AuraHighContrastInk
                    ),
                    modifier = Modifier.semantics { heading() }
                )
                Text(
                    text = "Pilih Video Kamera AI, Scan Tag RFID, atau Isi Manual. Semua langkah dapat dioperasikan lewat suara.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        color = AuraTextSecondary
                    )
                )
            }

            Surface(
                onClick = onSpeakGuide,
                color = AuraIndigoSoft,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .semantics {
                        contentDescription = "Dengarkan panduan 3 opsi tambah pakaian"
                        role = Role.Button
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_volume_up),
                        contentDescription = null,
                        tint = AuraNavyDeep,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Dengarkan Panduan 3 Opsi Tambah Baju",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = AuraNavyDeep
                        )
                    )
                }
            }

            VoiceActionButton(
                isListening = isVoiceListening,
                onClick = onVoiceClick,
                partialTranscript = partialTranscript
            )

            // Opsi 1: Video Kamera AI (Utama, Tenang & Fleksibel)
            AuraCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(AuraSpacing.sm)) {
                    Text(
                        text = "Opsi 1 • Pindai dengan Video Kamera AI",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AuraNavyDeep
                        ),
                        modifier = Modifier.semantics { heading() }
                    )
                    VIDEO_GUIDE_STEPS.forEach { step ->
                        HighContrastStepCard(step = step)
                    }
                    Surface(
                        color = AuraHintBannerBg,
                        border = BorderStroke(1.dp, AuraHighContrastInk),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Panduan kamera berbicara pelan dengan jeda nyaman dan tidak sensitif terhadap lipatan kain.",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = AuraHighContrastInk
                        )
                    }
                    Button(
                        onClick = onStartCamera,
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 58.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AuraNavy, contentColor = Color.White)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_clothes),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.size(10.dp))
                        Text(
                            text = "1. Mulai Pindai Video AI",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    AuraSecondaryButton(
                        text = "Pilih Foto dari Galeri",
                        onClick = onPickFromGallery,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Opsi 2: Scan Tag RFID (Ide Original + Langkah-langkah)
            AuraCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(AuraSpacing.sm)) {
                    Text(
                        text = "Opsi 2 • Tambah dengan Scan Tag RFID",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AuraNavyDeep
                        ),
                        modifier = Modifier.semantics { heading() }
                    )
                    Text(
                        text = "Langkah mendaftarkan pakaian dengan tag RFID/NFC:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AuraTextSecondary
                    )
                    RFID_ADD_STEPS.forEach { step ->
                        HighContrastStepCard(step = step)
                    }
                    AuraPrimaryButton(
                        text = "2. Tambah dengan Scan RFID",
                        onClick = onStartRfidAdd,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Opsi 3: Isi Detail Manual
            AuraCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(AuraSpacing.sm)) {
                    Text(
                        text = "Opsi 3 • Isi Formulir Secara Manual",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AuraNavyDeep
                        ),
                        modifier = Modifier.semantics { heading() }
                    )
                    Text(
                        text = "Masukkan nama pakaian, bahan, kategori, nama warna jelas (ramah buta warna), motif, dan aktivitas langsung tanpa memindai.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AuraTextSecondary
                    )
                    AuraSecondaryButton(
                        text = "3. Isi Detail Manual",
                        onClick = onManualForm,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(AuraSpacing.md))
        }
    }
}

@Composable
private fun HighContrastStepCard(step: GuideStep) {
    Surface(
        color = Color.White,
        border = BorderStroke(1.5.dp, AuraHighContrastInk),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = "Langkah ${step.number}: ${step.instruction}"
            }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(AuraHighContrastInk, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = step.number.toString(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AuraWarmSurface
                    )
                )
            }
            Text(
                text = step.instruction,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = AuraHighContrastInk,
                    lineHeight = 20.sp
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}
