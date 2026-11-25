package com.zuhdiarifazizi.projectakhirpapb.ui.manageclothes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingCategory
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingItem
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraCard
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraDestructiveButton
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraPrimaryButton
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraSecondaryButton
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraTextField
import com.zuhdiarifazizi.projectakhirpapb.ui.components.ClothingImage
import com.zuhdiarifazizi.projectakhirpapb.ui.home.components.VoiceActionButton
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraSpacing

@Composable
fun ManageClothesScreen(
    uiState: ManageClothesUiState,
    isVoiceListening: Boolean,
    onVoiceClick: () -> Unit,
    onAdd: () -> Unit,
    onItemDetails: (ClothingItem) -> Unit,
    onEdit: (ClothingItem) -> Unit,
    onSearchChange: (String) -> Unit,
    onCategoryFilter: (ClothingCategory?) -> Unit,
    onRequestDelete: () -> Unit,
    onCancelDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
    onFormBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onCategoryChange: (ClothingCategory) -> Unit,
    onPatternChange: (com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingPattern) -> Unit,
    onOccasionChange: (com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion) -> Unit,
    onColorChange: (String) -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onRemovePhoto: () -> Unit,
    onSave: () -> Unit,
    onBackToList: () -> Unit,
    onEditSelected: () -> Unit,
    onCameraReady: () -> Unit,
    onCameraUnavailable: () -> Unit,
    onCameraCaptureFailed: () -> Unit,
    onCameraPhotoCaptured: (String) -> Unit,
    onUseCapturedPhoto: () -> Unit,
    onRetakePhoto: () -> Unit,
    onCancelCamera: () -> Unit,
    createCameraFile: () -> java.io.File,
    modifier: Modifier = Modifier
) {
    when (uiState.page) {
        ManageClothesPage.LIST -> ClothesList(
            uiState, isVoiceListening, onVoiceClick, onAdd, onItemDetails,
            onEdit, onSearchChange, onCategoryFilter, modifier
        )
        ManageClothesPage.FORM -> ClothingFormScreen(
            form = uiState.form,
            isEditing = uiState.editingItemId != null,
            isSaving = uiState.isSaving,
            statusMessage = uiState.statusMessage,
            errorMessage = uiState.errorMessage,
            isVoiceListening = isVoiceListening,
            onBack = onFormBack,
            onVoiceClick = onVoiceClick,
            onNameChange = onNameChange,
            onCategoryChange = onCategoryChange,
            onPatternChange = onPatternChange,
            onOccasionChange = onOccasionChange,
            onColorChange = onColorChange,
            onCameraClick = onCameraClick,
            onGalleryClick = onGalleryClick,
            onRemovePhoto = onRemovePhoto,
            onSave = onSave,
            modifier = modifier
        )
        ManageClothesPage.DETAIL -> uiState.selectedItem?.let { item ->
            ClothingDetails(
                item = item,
                statusMessage = uiState.statusMessage,
                onBack = onBackToList,
                onEdit = onEditSelected,
                onDelete = onRequestDelete,
                modifier = modifier
            )
        }
        ManageClothesPage.CAMERA -> ClothingCameraScreen(
            captureRequestId = uiState.captureRequestId,
            capturedPhotoUri = uiState.capturedCameraPhotoUri,
            onBack = onCancelCamera,
            onCameraReady = onCameraReady,
            onCameraUnavailable = onCameraUnavailable,
            onCaptureFailed = onCameraCaptureFailed,
            onPhotoCaptured = onCameraPhotoCaptured,
            onUsePhoto = onUseCapturedPhoto,
            onRetake = onRetakePhoto,
            createCameraFile = createCameraFile,
            modifier = modifier
        )
    }

    if (uiState.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = onCancelDelete,
            title = { Text("Hapus pakaian?") },
            text = {
                Text("${uiState.selectedItem?.name ?: "Pakaian ini"} akan dihapus dari koleksi. Rekomendasi berikutnya tidak lagi menggunakannya.")
            },
            confirmButton = {
                TextButton(onClick = onConfirmDelete, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = onCancelDelete, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun ClothesList(
    uiState: ManageClothesUiState,
    isVoiceListening: Boolean,
    onVoiceClick: () -> Unit,
    onAdd: () -> Unit,
    onItemDetails: (ClothingItem) -> Unit,
    onEdit: (ClothingItem) -> Unit,
    onSearchChange: (String) -> Unit,
    onCategoryFilter: (ClothingCategory?) -> Unit,
    modifier: Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = AuraSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AuraSpacing.md)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(AuraSpacing.sm)) {
                Text("Kelola Pakaian", modifier = Modifier.semantics { heading() }, style = MaterialTheme.typography.headlineSmall)
                Text("${uiState.items.size} pakaian di koleksi. Data pakaian lokal tersedia untuk rekomendasi outfit.", style = MaterialTheme.typography.bodyMedium)
                VoiceActionButton(isListening = isVoiceListening, onClick = onVoiceClick)
                AuraPrimaryButton("Tambah Pakaian Baru", onAdd, modifier = Modifier.fillMaxWidth())
                AuraTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchChange,
                    label = "Cari pakaian",
                    placeholder = "Masukkan nama pakaian"
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(AuraSpacing.xs), verticalArrangement = Arrangement.spacedBy(AuraSpacing.xs)) {
                    FilterChip(selected = uiState.categoryFilter == null, onClick = { onCategoryFilter(null) }, label = { Text("Semua") })
                    ClothingCategory.entries.forEach { category ->
                        FilterChip(
                            selected = uiState.categoryFilter == category,
                            onClick = { onCategoryFilter(category) },
                            label = { Text(category.displayName) }
                        )
                    }
                }
                uiState.statusMessage?.let {
                    Text(it, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }, style = MaterialTheme.typography.bodyMedium)
                }
                uiState.errorMessage?.let {
                    Text(it, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive }, color = MaterialTheme.colorScheme.error)
                }
            }
        }
        if (uiState.isLoading) {
            item { Text("Memuat koleksi pakaian…", modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }) }
        } else if (uiState.visibleItems.isEmpty()) {
            item {
                AuraCard {
                    Text(
                        if (uiState.searchQuery.isBlank()) "Koleksi pakaian masih kosong. Tambahkan pakaian pertama Anda."
                        else "Tidak ada pakaian yang cocok dengan pencarian.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            items(uiState.visibleItems, key = ClothingItem::id) { item ->
                ClothingListCard(item, onItemDetails, onEdit)
            }
        }
        item { Spacer(Modifier.height(AuraSpacing.md)) }
    }
}

@Composable
private fun ClothingListCard(
    item: ClothingItem,
    onDetails: (ClothingItem) -> Unit,
    onEdit: (ClothingItem) -> Unit
) {
    AuraCard {
        Column(verticalArrangement = Arrangement.spacedBy(AuraSpacing.sm)) {
            ClothingImage(
                item = item,
                contentDescription = "Foto pakaian ${item.name}, kategori ${item.category.displayName}, warna ${item.color.colorName}"
            )
            Text(item.name, style = MaterialTheme.typography.titleMedium)
            Text(
                "Kategori ${item.category.displayName}. Warna ${item.color.colorName}. " +
                    "Motif ${item.pattern.displayName}. Aktivitas ${item.occasion.displayName}.",
                style = MaterialTheme.typography.bodyMedium
            )
            Text("Ditambahkan ${formatDate(item.createdAtEpochMillis)}", style = MaterialTheme.typography.bodySmall)
            AuraSecondaryButton("Lihat Detail ${item.name}", { onDetails(item) }, modifier = Modifier.fillMaxWidth())
            AuraPrimaryButton("Edit ${item.name}", { onEdit(item) }, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun ClothingDetails(
    item: ClothingItem,
    statusMessage: String?,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(AuraSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AuraSpacing.md)
    ) {
        AuraSecondaryButton("Kembali ke koleksi", onBack)
        Text("Detail Pakaian", modifier = Modifier.semantics { heading() }, style = MaterialTheme.typography.headlineSmall)
        AuraCard {
            Column(verticalArrangement = Arrangement.spacedBy(AuraSpacing.sm)) {
                ClothingImage(
                    item = item,
                    imageHeight = 220.dp,
                    contentDescription = "Foto pakaian ${item.name}, kategori ${item.category.displayName}, warna ${item.color.colorName}"
                )
                Text(item.name, style = MaterialTheme.typography.titleLarge)
                Text("Kategori: ${item.category.displayName}", style = MaterialTheme.typography.bodyLarge)
                Text("Warna: ${item.color.colorName}", style = MaterialTheme.typography.bodyLarge)
                Text("Motif: ${item.pattern.displayName}", style = MaterialTheme.typography.bodyLarge)
                Text("Aktivitas yang sesuai: ${item.occasion.displayName}", style = MaterialTheme.typography.bodyLarge)
                Text("Ditambahkan: ${formatDate(item.createdAtEpochMillis)}", style = MaterialTheme.typography.bodyMedium)
            }
        }
        statusMessage?.let { Text(it, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }) }
        AuraPrimaryButton("Edit Pakaian", onEdit, modifier = Modifier.fillMaxWidth())
        AuraDestructiveButton("Hapus Pakaian", onDelete, modifier = Modifier.fillMaxWidth())
    }
}

private fun formatDate(epochMillis: Long): String {
    if (epochMillis <= 0L) return "data demonstrasi"
    return java.text.SimpleDateFormat("d MMM yyyy", java.util.Locale("id", "ID")).format(java.util.Date(epochMillis))
}
