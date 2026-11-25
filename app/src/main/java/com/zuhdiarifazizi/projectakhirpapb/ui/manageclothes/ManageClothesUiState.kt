package com.zuhdiarifazizi.projectakhirpapb.ui.manageclothes

import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingCategory
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingItem
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingPattern
import com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion

enum class ManageClothesPage { LIST, FORM, DETAIL, CAMERA }

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

data class ClothingFormState(
    val name: String = "",
    val category: ClothingCategory = ClothingCategory.TOP_WEAR,
    val pattern: ClothingPattern = ClothingPattern.SOLID,
    val occasion: Occasion = Occasion.OTHER,
    val colorName: String? = null,
    val photoUri: String? = null,
    val nameError: String? = null,
    val colorError: String? = null
)

data class ManageClothesUiState(
    val items: List<ClothingItem> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val page: ManageClothesPage = ManageClothesPage.LIST,
    val selectedItem: ClothingItem? = null,
    val editingItemId: String? = null,
    val form: ClothingFormState = ClothingFormState(),
    val originalPhotoUri: String? = null,
    val stagedPhotoUri: String? = null,
    val capturedCameraPhotoUri: String? = null,
    val captureRequestId: Int = 0,
    val showDeleteConfirmation: Boolean = false,
    val searchQuery: String = "",
    val categoryFilter: ClothingCategory? = null,
    val errorMessage: String? = null,
    val statusMessage: String? = null
) {
    val visibleItems: List<ClothingItem>
        get() = items.filter { item ->
            (categoryFilter == null || item.category == categoryFilter) &&
                (searchQuery.isBlank() || item.name.contains(searchQuery.trim(), ignoreCase = true))
        }
}
