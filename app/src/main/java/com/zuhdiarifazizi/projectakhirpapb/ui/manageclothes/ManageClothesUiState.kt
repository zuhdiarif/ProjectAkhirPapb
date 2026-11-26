package com.zuhdiarifazizi.projectakhirpapb.ui.manageclothes

import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingCategory
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingItem
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingPattern
import com.zuhdiarifazizi.projectakhirpapb.domain.model.HsvColor
import com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion
import com.zuhdiarifazizi.projectakhirpapb.domain.scanner.ScannerReadiness

enum class ManageClothesPage { LIST, ADD_GUIDE, CAMERA, FORM, DETAIL, SCAN_FIND }

data class ClothingColorOption(
    val name: String,
    val hue: Float,
    val saturation: Float,
    val value: Float
)

val ClothingColorOptions = listOf(
    ClothingColorOption("Putih", 0f, 0.02f, 0.98f),
    ClothingColorOption("Hitam", 0f, 0.02f, 0.08f),
    ClothingColorOption("Abu-abu", 0f, 0.02f, 0.52f),
    ClothingColorOption("Biru dongker", 220f, 0.82f, 0.25f),
    ClothingColorOption("Biru denim", 211f, 0.54f, 0.56f),
    ClothingColorOption("Biru", 211f, 0.54f, 0.56f),
    ClothingColorOption("Cream", 38f, 0.22f, 0.91f),
    ClothingColorOption("Olive", 80f, 0.55f, 0.38f),
    ClothingColorOption("Merah", 0f, 0.78f, 0.72f),
    ClothingColorOption("Cokelat", 25f, 0.58f, 0.40f)
)

data class ScannerHudState(
    val lightPercent: Int = 88,
    val stepLabel: String = "Langkah 1 / 4",
    val detectedTitle: String = "Mencari pakaian...",
    val guidanceText: String = "Pegang ponsel sejajar di atas pakaian.",
    val readiness: ScannerReadiness = ScannerReadiness.NO_GARMENT,
    val isTorchOn: Boolean = false,
    val isVoiceGuidanceOn: Boolean = true,
    val useFrontCamera: Boolean = false,
    val isLockedForAutoCapture: Boolean = false,
    val isFindMode: Boolean = false
)

data class ClothingFormState(
    val name: String = "",
    val category: ClothingCategory = ClothingCategory.TOP_WEAR,
    val pattern: ClothingPattern = ClothingPattern.SOLID,
    val occasion: Occasion = Occasion.OTHER,
    val colorName: String? = null,
    val extractedHsv: HsvColor? = null,
    val material: String = "Katun Halus",
    val rfidTagUid: String? = null,
    val photoUri: String? = null,
    val nameError: String? = null,
    val colorError: String? = null
)

data class ManageClothesUiState(
    val items: List<ClothingItem> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isAnalyzingPhoto: Boolean = false,
    val page: ManageClothesPage = ManageClothesPage.LIST,
    val selectedItem: ClothingItem? = null,
    val editingItemId: String? = null,
    val form: ClothingFormState = ClothingFormState(),
    val scannerHud: ScannerHudState = ScannerHudState(),
    val originalPhotoUri: String? = null,
    val stagedPhotoUri: String? = null,
    val capturedCameraPhotoUri: String? = null,
    val captureRequestId: Int = 0,
    val showDeleteConfirmation: Boolean = false,
    val searchQuery: String = "",
    val categoryFilter: ClothingCategory? = null,
    val isNfcSupported: Boolean = false,
    val isNfcEnabled: Boolean = false,
    val isBindingRfidInForm: Boolean = false,
    val scannedRfidUid: String? = null,
    val matchedScanItem: ClothingItem? = null,
    val matchedVisualScore: Double? = null,
    val scanStatusMessage: String? = null,
    val errorMessage: String? = null,
    val statusMessage: String? = null
) {
    val visibleItems: List<ClothingItem>
        get() = items.filter { item ->
            (categoryFilter == null || item.category == categoryFilter) &&
                (searchQuery.isBlank() || item.name.contains(searchQuery.trim(), ignoreCase = true))
        }
}
