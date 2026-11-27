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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zuhdiarifazizi.projectakhirpapb.R
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraCard
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraPrimaryButton
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraSecondaryButton
import com.zuhdiarifazizi.projectakhirpapb.ui.home.components.VoiceActionButton
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraHighContrastInk
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraIndigoSoft
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraNavyDeep
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraShapes
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraSpacing
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraWarmSurface

private val RFID_FLOW_STEPS = listOf(
    "1" to "Pasang atau siapkan stiker tag RFID / NFC 13,56 MHz di pakaian yang akan ditambahkan.",
    "2" to "Tempelkan tag RFID tersebut tepat ke bagian belakang ponsel hingga terasa getaran konfirmasi.",
    "3" to "Setelah kode UID terbaca, layar Detail Pakaian Baru terbuka otomatis dengan tag sudah tertaut.",
    "4" to "Lengkapi nama/warna/foto pakaian (bisa dibantu Kamera AI) lalu tekan Simpan Pakaian."
)

@Composable
fun RfidAddScreen(
    isNfcSupported: Boolean,
    isNfcEnabled: Boolean,
    statusMessage: String?,
    isVoiceListening: Boolean = false,
    partialTranscript: String = "",
    onVoiceClick: () -> Unit = {},
    onSpeakSteps: () -> Unit = {},
    onBack: () -> Unit,
    onContinueManualForm: () -> Unit,
    onSimulateNewRfidTag: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AuraWarmSurface)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AuraSpacing.md, vertical = AuraSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(AuraSpacing.md)
    ) {
        AuraSecondaryButton(text = "Kembali ke Pilihan Tambah Baju", onClick = onBack)

        Text(
            text = "Tambah Baju lewat Scan RFID",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = AuraHighContrastInk
            ),
            modifier = Modifier.semantics { heading() }
        )

        VoiceActionButton(
            isListening = isVoiceListening,
            onClick = onVoiceClick,
            partialTranscript = partialTranscript
        )

        Surface(
            color = AuraIndigoSoft,
            shape = AuraShapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {}
        ) {
            Column(
                modifier = Modifier.padding(AuraSpacing.md),
                verticalArrangement = Arrangement.spacedBy(AuraSpacing.xs)
            ) {
                val nfcHeadline = when {
                    !isNfcSupported -> "Perangkat tanpa NFC — mode simulasi siap dicoba"
                    isNfcEnabled -> "Sensor RFID/NFC 13,56 MHz Siap Menerima Tag Baru"
                    else -> "NFC belum aktif di Pengaturan ponsel"
                }
                Text(nfcHeadline, fontWeight = FontWeight.Bold, color = AuraNavyDeep)
                Text(
                    text = statusMessage
                        ?: "Tempelkan tag RFID baru ke bagian belakang ponsel untuk langsung menautkan pakaian.",
                    color = AuraNavyDeep,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                )
            }
        }

        Surface(
            onClick = onSpeakSteps,
            color = Color.White,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, AuraNavyDeep),
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp)
                .semantics {
                    contentDescription = "Dengarkan langkah tambah pakaian lewat scan RFID"
                    role = Role.Button
                }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_volume_up),
                    contentDescription = null,
                    tint = AuraNavyDeep,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = "Dengarkan Langkah Scan RFID",
                    fontWeight = FontWeight.Bold,
                    color = AuraNavyDeep
                )
            }
        }

        AuraCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Langkah-langkah Scan Tag RFID",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.semantics { heading() }
                )
                RFID_FLOW_STEPS.forEach { (num, text) ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.semantics(mergeDescendants = true) {
                            contentDescription = "Langkah $num: $text"
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(AuraNavyDeep, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(num, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Text(text, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        AuraPrimaryButton(
            text = "Simulasi Tap Tag RFID Baru (Demo)",
            onClick = onSimulateNewRfidTag,
            modifier = Modifier.fillMaxWidth()
        )

        AuraSecondaryButton(
            text = "Lanjut Isi Form Tanpa Tag RFID",
            onClick = onContinueManualForm,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(AuraSpacing.md))
    }
}
