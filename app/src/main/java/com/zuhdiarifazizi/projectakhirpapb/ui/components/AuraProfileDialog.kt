package com.zuhdiarifazizi.projectakhirpapb.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zuhdiarifazizi.projectakhirpapb.domain.model.User

@Composable
fun AuraProfileDialog(
    user: User?,
    wardrobeCount: Int,
    historyCount: Int,
    onSpeakSummary: () -> Unit,
    onSeedStarterWardrobe: () -> Unit,
    onLogout: () -> Unit,
    onDismiss: () -> Unit
) {
    val displayName = user?.name ?: "Pengguna Lokal Aura"
    val displayEmail = user?.email ?: "lokal@aura.id"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Profil Akun Lokal",
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Nama: $displayName",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Email: $displayEmail",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Koleksi lemari: $wardrobeCount pakaian\nRiwayat 30 hari: $historyCount outfit",
                    style = MaterialTheme.typography.bodyMedium
                )
                AuraSecondaryButton(
                    text = "Dengarkan Info Akun",
                    onClick = onSpeakSummary,
                    modifier = Modifier.fillMaxWidth()
                )
                AuraSecondaryButton(
                    text = "Muat Ulang Koleksi Contoh",
                    onClick = onSeedStarterWardrobe,
                    modifier = Modifier.fillMaxWidth()
                )
                AuraDestructiveButton(
                    text = "Keluar Akun",
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}
