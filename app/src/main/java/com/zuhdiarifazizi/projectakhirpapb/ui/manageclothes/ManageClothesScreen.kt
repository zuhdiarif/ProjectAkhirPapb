package com.zuhdiarifazizi.projectakhirpapb.ui.manageclothes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zuhdiarifazizi.projectakhirpapb.R
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingCategory
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingItem
import com.zuhdiarifazizi.projectakhirpapb.domain.scanner.ScannerFrameGuidance
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraCard
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraDestructiveButton
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraPrimaryButton
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraSecondaryButton
import com.zuhdiarifazizi.projectakhirpapb.ui.components.ClothingImage
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraHighContrastInk
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraIndigoSoft
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraIndigoWash
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraNavy
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraNavyDeep
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraShapes
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraSpacing
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraSurfaceTint
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraTextSecondary
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraTipCardBg
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraTipIconBg
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraTipText
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraWarmSurface
import java.io.File

@Composable
fun ManageClothesScreen(
    uiState: ManageClothesUiState,
    isVoiceListening: Boolean,
    onVoiceClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAddGuide: () -> Unit,
    onAddManual: () -> Unit,
    onScanFind: () -> Unit,
    onSpeakOverview: () -> Unit,
    onSpeakItem: (ClothingItem) -> Unit,
    onItemDetails: (ClothingItem) -> Unit,
    onEdit: (ClothingItem) -> Unit,
    onSearchChange: (String) -> Unit,
    onCategoryFilter: (ClothingCategory?) -> Unit,
    onRequestDelete: (ClothingItem) -> Unit,
    onCancelDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
    onFormBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onMaterialChange: (String) -> Unit,
    onCategoryChange: (ClothingCategory) -> Unit,
    onPatternChange: (com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingPattern) -> Unit,
    onOccasionChange: (com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion) -> Unit,
    onColorChange: (String) -> Unit,
    onCameraClick: () -> Unit,
    onFindCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onRemovePhoto: () -> Unit,
    onBindRfidClick: () -> Unit,
    onCancelRfidBind: () -> Unit,
    onClearRfid: () -> Unit,
    onSimulateRfidTap: (String) -> Unit,
    onSave: () -> Unit,
    onBackToList: () -> Unit,
    onEditSelected: () -> Unit,
    onToggleTorch: () -> Unit,
    onToggleCameraFacing: () -> Unit,
    onToggleVoiceGuidance: () -> Unit,
    onSpeakCameraInstruction: () -> Unit,
    onScannerFrameEvaluated: (ScannerFrameGuidance) -> Unit,
    onCameraReady: () -> Unit,
    onCameraUnavailable: () -> Unit,
    onCameraCaptureFailed: () -> Unit,
    onCameraPhotoCaptured: (String) -> Unit,
    onUseCapturedPhoto: () -> Unit,
    onRetakePhoto: () -> Unit,
    onCancelCamera: () -> Unit,
    createCameraFile: () -> File,
    modifier: Modifier = Modifier
) {
    when (uiState.page) {
        ManageClothesPage.LIST -> ClothesList(
            uiState = uiState,
            onProfileClick = onProfileClick,
            onAddGuide = onAddGuide,
            onScanFind = onScanFind,
            onSpeakOverview = onSpeakOverview,
            onSpeakItem = onSpeakItem,
            onItemDetails = onItemDetails,
            onEdit = onEdit,
            onDelete = onRequestDelete,
            onSearchChange = onSearchChange,
            onCategoryFilter = onCategoryFilter,
            modifier = modifier
        )

        ManageClothesPage.ADD_GUIDE -> AddClothingGuideScreen(
            onBack = onBackToList,
            onProfileClick = onProfileClick,
            onStartCamera = onCameraClick,
            onPickFromGallery = onGalleryClick,
            onManualForm = onAddManual,
            modifier = modifier
        )

        ManageClothesPage.CAMERA -> ClothingCameraScreen(
            hud = uiState.scannerHud,
            captureRequestId = uiState.captureRequestId,
            capturedPhotoUri = uiState.capturedCameraPhotoUri,
            onBack = onCancelCamera,
            onOpenGallery = onGalleryClick,
            onToggleTorch = onToggleTorch,
            onToggleCameraFacing = onToggleCameraFacing,
            onToggleVoiceGuidance = onToggleVoiceGuidance,
            onSpeakInstruction = onSpeakCameraInstruction,
            onFrameEvaluated = onScannerFrameEvaluated,
            onCameraReady = onCameraReady,
            onCameraUnavailable = onCameraUnavailable,
            onCaptureFailed = onCameraCaptureFailed,
            onPhotoCaptured = onCameraPhotoCaptured,
            onUsePhoto = onUseCapturedPhoto,
            onRetake = onRetakePhoto,
            createCameraFile = createCameraFile,
            modifier = modifier
        )

        ManageClothesPage.FORM -> ClothingFormScreen(
            form = uiState.form,
            isEditing = uiState.editingItemId != null,
            isSaving = uiState.isSaving,
            isAnalyzingPhoto = uiState.isAnalyzingPhoto,
            isNfcSupported = uiState.isNfcSupported,
            isBindingRfid = uiState.isBindingRfidInForm,
            statusMessage = uiState.statusMessage,
            errorMessage = uiState.errorMessage,
            isVoiceListening = isVoiceListening,
            onBack = onFormBack,
            onVoiceClick = onVoiceClick,
            onNameChange = onNameChange,
            onMaterialChange = onMaterialChange,
            onCategoryChange = onCategoryChange,
            onPatternChange = onPatternChange,
            onOccasionChange = onOccasionChange,
            onColorChange = onColorChange,
            onCameraClick = onCameraClick,
            onGalleryClick = onGalleryClick,
            onRemovePhoto = onRemovePhoto,
            onBindRfidClick = onBindRfidClick,
            onCancelRfidBind = onCancelRfidBind,
            onClearRfid = onClearRfid,
            onSimulateRfidTap = onSimulateRfidTap,
            onSave = onSave,
            modifier = modifier
        )

        ManageClothesPage.DETAIL -> uiState.selectedItem?.let { item ->
            ClothingDetails(
                item = item,
                statusMessage = uiState.statusMessage,
                onBack = onBackToList,
                onSpeak = { onSpeakItem(item) },
                onEdit = onEditSelected,
                onDelete = { onRequestDelete(item) },
                modifier = modifier
            )
        }

        ManageClothesPage.SCAN_FIND -> RfidScanScreen(
            isNfcSupported = uiState.isNfcSupported,
            isNfcEnabled = uiState.isNfcEnabled,
            isAnalyzingPhoto = uiState.isAnalyzingPhoto,
            scannedUid = uiState.scannedRfidUid,
            matchedItem = uiState.matchedScanItem,
            matchedScore = uiState.matchedVisualScore,
            statusMessage = uiState.scanStatusMessage,
            onBack = onBackToList,
            onRecognizeWithCamera = onFindCameraClick,
            onRecognizeWithGallery = onGalleryClick,
            onSpeakMatchedItem = onSpeakItem,
            onOpenMatchedDetails = onItemDetails,
            onSimulateDemoTap = onSimulateRfidTap,
            modifier = modifier
        )
    }

    if (uiState.showDeleteConfirmation && uiState.selectedItem != null) {
        AlertDialog(
            onDismissRequest = onCancelDelete,
            title = { Text("Hapus ${uiState.selectedItem.name}?") },
            text = { Text("Pakaian ini tidak akan muncul lagi di koleksi maupun rekomendasi outfit.") },
            confirmButton = {
                TextButton(onClick = onConfirmDelete) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = onCancelDelete) { Text("Batal") }
            }
        )
    }
}

@Composable
private fun ClothesList(
    uiState: ManageClothesUiState,
    onProfileClick: () -> Unit,
    onAddGuide: () -> Unit,
    onScanFind: () -> Unit,
    onSpeakOverview: () -> Unit,
    onSpeakItem: (ClothingItem) -> Unit,
    onItemDetails: (ClothingItem) -> Unit,
    onEdit: (ClothingItem) -> Unit,
    onDelete: (ClothingItem) -> Unit,
    onSearchChange: (String) -> Unit,
    onCategoryFilter: (ClothingCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AuraSurfaceTint)
    ) {
        Surface(color = AuraWarmSurface.copy(alpha = 0.95f)) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.semantics { heading() }
                    ) {
                        Text(
                            text = "Aura",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = AuraNavyDeep,
                                fontSize = 26.sp
                            )
                        )
                        Text(
                            text = "  •  ",
                            style = MaterialTheme.typography.titleLarge.copy(color = AuraTextSecondary)
                        )
                        Text(
                            text = "Kelola Baju",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = AuraHighContrastInk,
                                fontSize = 20.sp
                            )
                        )
                    }

                    IconButton(
                        onClick = onProfileClick,
                        modifier = Modifier
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
                HorizontalDivider(color = Color(0xFFC5C5D3).copy(alpha = 0.4f), thickness = 1.dp)
            }
        }

        val visible = uiState.visibleItems
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Surface(
                    color = AuraIndigoSoft,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(AuraNavyDeep),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_mic),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "ASISTEN SUARA\nLEMARI",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AuraNavyDeep,
                                    letterSpacing = 0.4.sp
                                )
                            )
                        }

                        Surface(
                            onClick = onSpeakOverview,
                            shape = RoundedCornerShape(8.dp),
                            color = AuraSurfaceTint,
                            modifier = Modifier.defaultMinSize(minHeight = 40.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_volume_up),
                                    contentDescription = null,
                                    tint = AuraNavyDeep,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Nyalakan",
                                    fontWeight = FontWeight.Bold,
                                    color = AuraNavyDeep
                                )
                            }
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Lemari Pakaian Saya",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF131B2E)
                        ),
                        modifier = Modifier.semantics { heading() }
                    )
                    Text(
                        text = "Total ${uiState.items.size} pakaian tersimpan & siap dipadukan",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = AuraTextSecondary
                        )
                    )
                }
            }

            item {
                Button(
                    onClick = onAddGuide,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 64.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AuraNavy, contentColor = Color.White)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Tambah baju baru",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        )
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = onSearchChange,
                        singleLine = true,
                        placeholder = { Text("Cari kemeja, celana chino, jaket...") },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_search),
                                contentDescription = null,
                                tint = AuraTextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = AuraIndigoWash,
                            unfocusedContainerColor = AuraIndigoWash,
                            focusedBorderColor = AuraNavy,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        onClick = onScanFind,
                        color = AuraIndigoWash,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .size(56.dp)
                            .semantics {
                                contentDescription = "Pindai RFID atau kenali pakaian lewat kamera"
                                role = Role.Button
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(R.drawable.ic_wardrobe),
                                contentDescription = null,
                                tint = AuraNavyDeep,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            item {
                val allCount = uiState.items.size
                val topCount = uiState.items.count { it.category == ClothingCategory.TOP_WEAR }
                val bottomCount = uiState.items.count { it.category == ClothingCategory.BOTTOM_WEAR }
                val outerCount = uiState.items.count { it.category == ClothingCategory.OUTER_WEAR }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        CategoryFilterPill(
                            label = "Semua ($allCount)",
                            selected = uiState.categoryFilter == null,
                            onClick = { onCategoryFilter(null) }
                        )
                    }
                    item {
                        CategoryFilterPill(
                            label = "Atasan ($topCount)",
                            selected = uiState.categoryFilter == ClothingCategory.TOP_WEAR,
                            onClick = { onCategoryFilter(ClothingCategory.TOP_WEAR) }
                        )
                    }
                    item {
                        CategoryFilterPill(
                            label = "Bawahan ($bottomCount)",
                            selected = uiState.categoryFilter == ClothingCategory.BOTTOM_WEAR,
                            onClick = { onCategoryFilter(ClothingCategory.BOTTOM_WEAR) }
                        )
                    }
                    item {
                        CategoryFilterPill(
                            label = "Luaran ($outerCount)",
                            selected = uiState.categoryFilter == ClothingCategory.OUTER_WEAR,
                            onClick = { onCategoryFilter(ClothingCategory.OUTER_WEAR) }
                        )
                    }
                }
            }

            uiState.statusMessage?.let { message ->
                item {
                    Text(
                        text = message,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                        style = MaterialTheme.typography.bodyMedium,
                        color = AuraNavyDeep
                    )
                }
            }

            if (uiState.isLoading) {
                item { Text("Memuat koleksi pakaian…") }
            } else if (visible.isEmpty()) {
                item {
                    AuraCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(AuraSpacing.xs)) {
                            Text("Belum ada pakaian yang cocok.", fontWeight = FontWeight.SemiBold)
                            Text("Tekan + Tambah baju baru untuk mulai mendigitalkan lemari Anda.")
                        }
                    }
                }
            } else {
                itemsIndexed(visible, key = { _, item -> item.id }) { index, item ->
                    FigmaClothingItemCard(
                        indexNumber = index + 1,
                        item = item,
                        onSpeak = { onSpeakItem(item) },
                        onOpenDetails = { onItemDetails(item) },
                        onEdit = { onEdit(item) },
                        onDelete = { onDelete(item) }
                    )
                }
            }

            item {
                Surface(
                    color = AuraTipCardBg,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(AuraTipIconBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_info),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Tips Menata Lemari",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF001848)
                                )
                            )
                            Text(
                                text = "Kelompokkan baju berdasarkan urutan warna terang ke gelap untuk memudahkan pemindaian kamera Anda berikutnya.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = AuraTipText
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryFilterPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (selected) AuraNavy else Color(0xFFF2F3FF),
        modifier = Modifier.defaultMinSize(minHeight = 44.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (selected) Color.White else Color(0xFF131B2E)
                )
            )
        }
    }
}

@Composable
private fun FigmaClothingItemCard(
    indexNumber: Int,
    item: ClothingItem,
    onSpeak: () -> Unit,
    onOpenDetails: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "${item.name}, ${item.category.displayName}, warna ${item.color.colorName}"
            }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 80.dp, height = 96.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF2F3FF))
                ) {
                    ClothingImage(item = item, imageHeight = 96.dp)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(AuraNavyDeep)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "#${indexNumber.toString().padStart(2, '0')}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF131B2E)
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Box {
                            IconButton(
                                onClick = { menuExpanded = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .semantics { contentDescription = "Menu opsi ${item.name}" }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_more_vert),
                                    contentDescription = null,
                                    tint = AuraTextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                                DropdownMenuItem(
                                    text = { Text("Lihat Detail") },
                                    onClick = { menuExpanded = false; onOpenDetails() }
                                )
                                DropdownMenuItem(
                                    text = { Text("Edit & RFID") },
                                    onClick = { menuExpanded = false; onEdit() }
                                )
                                DropdownMenuItem(
                                    text = { Text("Hapus") },
                                    onClick = { menuExpanded = false; onDelete() }
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        BadgeChip(text = item.category.displayName, bgColor = AuraIndigoSoft, textColor = AuraNavyDeep)
                        if (item.material.isNotBlank()) {
                            BadgeChip(text = item.material, bgColor = AuraIndigoWash, textColor = AuraTextSecondary)
                        }
                        if (!item.rfidTagUid.isNullOrBlank()) {
                            BadgeChip(text = "RFID", bgColor = AuraTipCardBg, textColor = AuraNavyDeep)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_history),
                            contentDescription = null,
                            tint = AuraTextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = formatLastWornLabel(item.lastWornEpochMillis),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = AuraTextSecondary
                            )
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onSpeak,
                    shape = RoundedCornerShape(8.dp),
                    color = AuraIndigoWash,
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_volume_up),
                            contentDescription = null,
                            tint = AuraNavyDeep,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Dengarkan Deskripsi",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = AuraNavyDeep
                            )
                        )
                    }
                }

                Surface(
                    onClick = onOpenDetails,
                    shape = RoundedCornerShape(8.dp),
                    color = AuraIndigoSoft,
                    modifier = Modifier
                        .size(48.dp)
                        .semantics {
                            contentDescription = "Buka detail ${item.name}"
                            role = Role.Button
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_forward),
                            contentDescription = null,
                            tint = AuraNavyDeep,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeChip(text: String, bgColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontSize = 13.sp
            )
        )
    }
}

@Composable
private fun ClothingDetails(
    item: ClothingItem,
    statusMessage: String?,
    onBack: () -> Unit,
    onSpeak: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AuraSpacing.md, vertical = AuraSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(AuraSpacing.md)
    ) {
        AuraSecondaryButton(text = "Kembali ke daftar", onClick = onBack)
        Text(item.name, modifier = Modifier.semantics { heading() }, style = MaterialTheme.typography.headlineSmall)
        ClothingImage(item = item, imageHeight = 220.dp)
        AuraCard {
            Column(verticalArrangement = Arrangement.spacedBy(AuraSpacing.xs)) {
                Text("Kategori: ${item.category.displayName}")
                Text("Warna: ${item.color.colorName} (H:${item.color.hue.toInt()}° S:${(item.color.saturation * 100).toInt()}% V:${(item.color.value * 100).toInt()}%)")
                if (item.material.isNotBlank()) Text("Bahan: ${item.material}")
                Text("Motif: ${item.pattern.displayName}")
                Text("Aktivitas: ${item.occasion.displayName}")
                Text("Tag RFID: ${item.rfidTagUid ?: "Belum ditautkan"}")
                Text(formatLastWornLabel(item.lastWornEpochMillis))
            }
        }
        statusMessage?.let {
            Text(it, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
        }
        AuraPrimaryButton("Dengarkan Deskripsi", onSpeak, modifier = Modifier.fillMaxWidth())
        AuraSecondaryButton("Edit Pakaian & Tag RFID", onEdit, modifier = Modifier.fillMaxWidth())
        AuraDestructiveButton("Hapus Pakaian", onDelete, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(AuraSpacing.md))
    }
}

private fun formatLastWornLabel(lastWornMillis: Long?): String {
    if (lastWornMillis == null) return "Belum pernah dipakai"
    val diffDays = ((System.currentTimeMillis() - lastWornMillis) / (24L * 60L * 60L * 1000L)).coerceAtLeast(0L)
    return when {
        diffDays == 0L -> "Dipakai hari ini"
        diffDays < 7L -> "Terakhir dipakai $diffDays hari lalu"
        else -> "Terakhir dipakai ${diffDays / 7L} minggu lalu"
    }
}
