package com.zuhdiarifazizi.projectakhirpapb.ui.manageclothes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingCategory
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingPattern
import com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraCard
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraPrimaryButton
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraSecondaryButton
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraTextField
import com.zuhdiarifazizi.projectakhirpapb.ui.components.LocalClothingPhoto
import com.zuhdiarifazizi.projectakhirpapb.ui.home.components.VoiceActionButton
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraSpacing

@Composable
fun ClothingFormScreen(
    form: ClothingFormState,
    isEditing: Boolean,
    isSaving: Boolean,
    statusMessage: String?,
    errorMessage: String?,
    isVoiceListening: Boolean,
    onBack: () -> Unit,
    onVoiceClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onCategoryChange: (ClothingCategory) -> Unit,
    onPatternChange: (ClothingPattern) -> Unit,
    onOccasionChange: (Occasion) -> Unit,
    onColorChange: (String) -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onRemovePhoto: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AuraSpacing.md, vertical = AuraSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(AuraSpacing.md)
    ) {
        AuraSecondaryButton(text = "Kembali", onClick = onBack)
        Text(
            text = if (isEditing) "Edit Pakaian" else "Tambah Pakaian",
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineSmall
        )
        Text("Lengkapi informasi dengan suara pembaca layar atau sentuhan. Kategori, warna, motif, dan aktivitas membantu menyusun rekomendasi.", style = MaterialTheme.typography.bodyMedium)
        VoiceActionButton(isListening = isVoiceListening, onClick = onVoiceClick)

        AuraCard {
            Column(verticalArrangement = Arrangement.spacedBy(AuraSpacing.sm)) {
                Text("Foto pakaian (opsional)", fontWeight = FontWeight.SemiBold)
                if (form.photoUri != null) {
                    LocalClothingPhoto(
                        imageUri = form.photoUri,
                        contentDescription = "Foto pakaian yang dipilih untuk ${form.name.ifBlank { "pakaian baru" }}",
                        imageHeight = 210.dp
                    )
                    AuraSecondaryButton("Hapus Foto", onRemovePhoto, modifier = Modifier.fillMaxWidth())
                } else {
                    Text("Belum ada foto. Anda tetap dapat menyimpan pakaian tanpa foto.", style = MaterialTheme.typography.bodyMedium)
                }
                AuraPrimaryButton("Ambil Foto", onCameraClick, modifier = Modifier.fillMaxWidth())
                AuraSecondaryButton("Pilih Foto dari Galeri", onGalleryClick, modifier = Modifier.fillMaxWidth())
            }
        }

        AuraTextField(
            value = form.name,
            onValueChange = onNameChange,
            label = "Nama pakaian",
            placeholder = "Contoh: Kemeja putih",
            errorMessage = form.nameError
        )

        ChoiceGroup(
            title = "Kategori pakaian",
            selectedLabel = form.category.displayName,
            choices = ClothingCategory.entries.map { it to it.displayName },
            onSelect = onCategoryChange
        )
        ChoiceGroup(
            title = "Warna utama",
            selectedLabel = form.colorName,
            choices = ClothingColorOptions.map { it.name to it.name },
            onSelect = onColorChange,
            errorMessage = form.colorError
        )
        ChoiceGroup(
            title = "Motif pakaian",
            selectedLabel = form.pattern.displayName,
            choices = ClothingPattern.entries.map { it to it.displayName },
            onSelect = onPatternChange
        )
        ChoiceGroup(
            title = "Aktivitas yang sesuai",
            selectedLabel = form.occasion.displayName,
            choices = Occasion.entries.map { it to it.displayName },
            onSelect = onOccasionChange
        )

        statusMessage?.let {
            Text(it, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }, style = MaterialTheme.typography.bodyMedium)
        }
        errorMessage?.let {
            Text(it, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive }, color = MaterialTheme.colorScheme.error)
        }
        AuraPrimaryButton(
            text = if (isSaving) "Menyimpan…" else "Simpan Pakaian",
            onClick = onSave,
            enabled = !isSaving,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(AuraSpacing.md))
    }
}

@Composable
private fun <T> ChoiceGroup(
    title: String,
    selectedLabel: String?,
    choices: List<Pair<T, String>>,
    onSelect: (T) -> Unit,
    errorMessage: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(AuraSpacing.xs)) {
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
        FlowRow(horizontalArrangement = Arrangement.spacedBy(AuraSpacing.xs), verticalArrangement = Arrangement.spacedBy(AuraSpacing.xs)) {
            choices.forEach { (value, label) ->
                FilterChip(
                    selected = selectedLabel == label,
                    onClick = { onSelect(value) },
                    label = { Text(label) }
                )
            }
        }
        errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        Text("Terpilih: ${selectedLabel ?: "belum dipilih"}", style = MaterialTheme.typography.bodySmall)
    }
}
