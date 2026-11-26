package com.zuhdiarifazizi.projectakhirpapb.ui.manageclothes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingItem
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraCard
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraPrimaryButton
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraSecondaryButton
import com.zuhdiarifazizi.projectakhirpapb.ui.components.ClothingImage
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraIndigoSoft
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraNavyDeep
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraShapes
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraSpacing

@Composable
fun RfidScanScreen(
    isNfcSupported: Boolean,
    isNfcEnabled: Boolean,
    isAnalyzingPhoto: Boolean,
    scannedUid: String?,
    matchedItem: ClothingItem?,
    matchedScore: Double?,
    statusMessage: String?,
    onBack: () -> Unit,
    onRecognizeWithCamera: () -> Unit,
    onRecognizeWithGallery: () -> Unit,
    onSpeakMatchedItem: (ClothingItem) -> Unit,
    onOpenMatchedDetails: (ClothingItem) -> Unit,
    onSimulateDemoTap: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AuraSpacing.md, vertical = AuraSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(AuraSpacing.md)
    ) {
        AuraSecondaryButton(text = "Kembali ke Kelola Baju", onClick = onBack)

        Text(
            text = "Pindai RFID / Kenali Baju",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.semantics { heading() }
        )

        Surface(
            color = AuraIndigoSoft,
            shape = AuraShapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {}
        ) {
            Column(modifier = Modifier.padding(AuraSpacing.md), verticalArrangement = Arrangement.spacedBy(AuraSpacing.xs)) {
                val nfcHeadline = when {
                    !isNfcSupported -> "Perangkat tanpa NFC — gunakan Kamera AI atau simulasi demo"
                    isNfcEnabled -> "Reader RFID/NFC 13,56 MHz Aktif"
                    else -> "NFC belum aktif di Pengaturan ponsel"
                }
                Text(nfcHeadline, fontWeight = FontWeight.Bold, color = AuraNavyDeep)
                Text(
                    text = statusMessage ?: "Tempelkan kartu atau stiker RFID pakaian ke bagian belakang ponsel.",
                    color = AuraNavyDeep,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                )
                scannedUid?.let { uid ->
                    Text("UID Terdeteksi: $uid", style = MaterialTheme.typography.bodySmall, color = AuraNavyDeep)
                }
            }
        }

        if (isAnalyzingPhoto) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        matchedItem?.let { item ->
            AuraCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(AuraSpacing.sm)) {
                    Text(
                        text = "Pakaian Dikenali",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.semantics { heading() }
                    )
                    ClothingImage(item = item, imageHeight = 180.dp)
                    Text(item.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("${item.category.displayName} • Warna ${item.color.colorName} • ${item.occasion.displayName}")
                    if (item.material.isNotBlank()) Text("Bahan: ${item.material}")
                    matchedScore?.let { score ->
                        Text("Tingkat kecocokan: ${(score * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
                    }
                    AuraPrimaryButton(
                        text = "Dengarkan Deskripsi",
                        onClick = { onSpeakMatchedItem(item) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "Dengarkan deskripsi ${item.name}" }
                    )
                    AuraSecondaryButton(
                        text = "Buka Detail Pakaian",
                        onClick = { onOpenMatchedDetails(item) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "Buka detail pakaian ${item.name}" }
                    )
                }
            }
        }

        AuraPrimaryButton(
            text = "Kenali Pakaian lewat Kamera AI",
            onClick = onRecognizeWithCamera,
            modifier = Modifier.fillMaxWidth()
        )
        AuraSecondaryButton(
            text = "Kenali dari Galeri Foto",
            onClick = onRecognizeWithGallery,
            modifier = Modifier.fillMaxWidth()
        )

        if (!isNfcSupported) {
            AuraCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(AuraSpacing.xs)) {
                    Text(
                        text = "Uji Demo RFID (Emulator)",
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.semantics { heading() }
                    )
                    Text(
                        text = "Untuk mencoba di emulator tanpa hardware NFC, gunakan simulasi tap UID demo:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    AuraSecondaryButton(
                        text = "Simulasi Tap UID 04:A1:B2:C3:D4",
                        onClick = { onSimulateDemoTap("04:A1:B2:C3:D4") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(Modifier.height(AuraSpacing.md))
    }
}
