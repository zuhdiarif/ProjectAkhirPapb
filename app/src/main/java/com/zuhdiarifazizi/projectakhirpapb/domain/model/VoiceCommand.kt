package com.zuhdiarifazizi.projectakhirpapb.domain.model

enum class VoicePage {
    GLOBAL,
    ONBOARDING,
    HOME,
    PICK_CATEGORY,
    PICK_RECOMMENDATIONS,
    PICK_RESULT,
    HISTORY,
    WARDROBE_LIST,
    ADD_GUIDE,
    RFID_ADD,
    CAMERA_LIVE,
    CAMERA_PREVIEW,
    CLOTHING_FORM,
    CLOTHING_DETAIL,
    SCAN_FIND
}

data class VoiceScreenContext(
    val page: VoicePage = VoicePage.GLOBAL,
    val hasConfirmationDialog: Boolean = false
)

enum class ItemVoiceAction {
    SPEAK,
    DETAIL,
    EDIT,
    DELETE
}

sealed interface VoiceRecognitionError {
    data object NoMatch : VoiceRecognitionError
    data object NetworkError : VoiceRecognitionError
    data object AudioError : VoiceRecognitionError
    data object ServerDisconnected : VoiceRecognitionError
    data object SpeechTimeout : VoiceRecognitionError
    data object PermissionDenied : VoiceRecognitionError
    data object ServiceUnavailable : VoiceRecognitionError
    data class Unknown(val errorCode: Int, val message: String) : VoiceRecognitionError

    fun toUserMessage(): String {
        return when (this) {
            is NoMatch -> "Suara tidak terdengar jelas. Silakan ulangi ucapan Anda."
            is NetworkError -> "Gangguan koneksi internet untuk pengenalan suara."
            is AudioError -> "Terjadi masalah pada perekaman audio mikrofon."
            is ServerDisconnected -> "Koneksi server suara terputus. Silakan coba lagi."
            is SpeechTimeout -> "Waktu bicara habis. Silakan tekan tombol mikrofon untuk mencoba lagi."
            is PermissionDenied -> "Izin mikrofon belum diberikan. Silakan izinkan akses mikrofon."
            is ServiceUnavailable -> "Layanan pengenalan suara tidak tersedia pada perangkat."
            is Unknown -> "Terjadi kendala pengenalan suara. Silakan coba lagi."
        }
    }
}

enum class VoiceIntentType {
    SELECT_WORK,
    SELECT_HANGOUT,
    SELECT_SPORT,
    SELECT_STREETWEAR,
    SELECT_OTHER,
    OPEN_HOME,
    OPEN_HISTORY,
    OPEN_WARDROBE,
    OPEN_ONBOARDING,
    NAVIGATE_BACK,
    REPEAT_RESPONSE,
    CANCEL_ACTION,
    CONFIRM_POSITIVE,
    READ_PAGE_GUIDANCE,
    ONBOARDING_NEXT,
    ONBOARDING_PREVIOUS,
    ONBOARDING_FINISH,
    TOGGLE_OUTFIT_CATEGORY,
    SELECT_ALL_OUTFIT_CATEGORIES,
    CONTINUE_RECOMMENDATION,
    NEXT_RECOMMENDATION,
    PREVIOUS_RECOMMENDATION,
    LISTEN_RECOMMENDATIONS,
    VIEW_RECOMMENDATION_DETAIL,
    SELECT_FIRST_RECOMMENDATION,
    SELECT_CURRENT_RECOMMENDATION,
    SELECT_NUMBERED_RECOMMENDATION,
    CONFIRM_WEAR_OUTFIT,
    SPEAK_HISTORY_ITEM,
    SPEAK_ROTATION_ITEM,
    DELETE_HISTORY_ITEM,
    REQUEST_CLEAR_HISTORY,
    FILTER_WARDROBE_CATEGORY,
    SEARCH_WARDROBE,
    CLEAR_WARDROBE_SEARCH,
    WARDROBE_ITEM_ACTION,
    SEED_STARTER_WARDROBE,
    ADD_CLOTHING,
    START_ADD_VIDEO_AI,
    START_ADD_RFID,
    START_ADD_MANUAL,
    PICK_FROM_GALLERY,
    OPEN_CLOTHING_CAMERA,
    CAPTURE_PHOTO_NOW,
    USE_CAPTURED_PHOTO,
    RETAKE_PHOTO,
    TOGGLE_TORCH,
    FLIP_CAMERA,
    TOGGLE_SCANNER_VOICE,
    SCAN_RFID,
    BIND_RFID_TAG,
    CLEAR_RFID_TAG,
    SIMULATE_RFID_DEMO,
    RECOGNIZE_VIA_CAMERA_AI,
    SET_FORM_NAME,
    SET_FORM_MATERIAL,
    SET_FORM_CATEGORY,
    SET_FORM_COLOR,
    SET_FORM_PATTERN,
    SET_FORM_OCCASION,
    REMOVE_FORM_PHOTO,
    SAVE_CLOTHING_FORM,
    MARK_WORN_TODAY,
    EDIT_CURRENT_CLOTHING,
    DELETE_CURRENT_CLOTHING,
    UNKNOWN
}

sealed class VoiceCommand(val type: VoiceIntentType) {
    data object SelectWork : VoiceCommand(VoiceIntentType.SELECT_WORK)
    data object SelectHangout : VoiceCommand(VoiceIntentType.SELECT_HANGOUT)
    data object SelectSport : VoiceCommand(VoiceIntentType.SELECT_SPORT)
    data object SelectStreetwear : VoiceCommand(VoiceIntentType.SELECT_STREETWEAR)
    data object SelectOther : VoiceCommand(VoiceIntentType.SELECT_OTHER)
    data object OpenHome : VoiceCommand(VoiceIntentType.OPEN_HOME)
    data object OpenHistory : VoiceCommand(VoiceIntentType.OPEN_HISTORY)
    data object OpenWardrobe : VoiceCommand(VoiceIntentType.OPEN_WARDROBE)
    data object OpenOnboarding : VoiceCommand(VoiceIntentType.OPEN_ONBOARDING)
    data object NavigateBack : VoiceCommand(VoiceIntentType.NAVIGATE_BACK)
    data object RepeatResponse : VoiceCommand(VoiceIntentType.REPEAT_RESPONSE)
    data object CancelAction : VoiceCommand(VoiceIntentType.CANCEL_ACTION)
    data object ConfirmPositive : VoiceCommand(VoiceIntentType.CONFIRM_POSITIVE)
    data object ReadPageGuidance : VoiceCommand(VoiceIntentType.READ_PAGE_GUIDANCE)

    data object OnboardingNext : VoiceCommand(VoiceIntentType.ONBOARDING_NEXT)
    data object OnboardingPrevious : VoiceCommand(VoiceIntentType.ONBOARDING_PREVIOUS)
    data object OnboardingFinish : VoiceCommand(VoiceIntentType.ONBOARDING_FINISH)

    data class ToggleOutfitCategory(
        val category: ClothingCategory,
        val selected: Boolean?
    ) : VoiceCommand(VoiceIntentType.TOGGLE_OUTFIT_CATEGORY)

    data object SelectAllOutfitCategories : VoiceCommand(VoiceIntentType.SELECT_ALL_OUTFIT_CATEGORIES)
    data object ContinueRecommendation : VoiceCommand(VoiceIntentType.CONTINUE_RECOMMENDATION)
    data object NextRecommendation : VoiceCommand(VoiceIntentType.NEXT_RECOMMENDATION)
    data object PreviousRecommendation : VoiceCommand(VoiceIntentType.PREVIOUS_RECOMMENDATION)
    data class ListenRecommendations(val index: Int? = null) : VoiceCommand(VoiceIntentType.LISTEN_RECOMMENDATIONS)
    data class ViewRecommendationDetail(val index: Int? = null) : VoiceCommand(VoiceIntentType.VIEW_RECOMMENDATION_DETAIL)
    data object SelectFirstRecommendation : VoiceCommand(VoiceIntentType.SELECT_FIRST_RECOMMENDATION)
    data object SelectCurrentRecommendation : VoiceCommand(VoiceIntentType.SELECT_CURRENT_RECOMMENDATION)
    data class SelectNumberedRecommendation(val index: Int) : VoiceCommand(VoiceIntentType.SELECT_NUMBERED_RECOMMENDATION)
    data object ConfirmWearOutfit : VoiceCommand(VoiceIntentType.CONFIRM_WEAR_OUTFIT)

    data class SpeakHistoryItem(val oneBasedIndex: Int) : VoiceCommand(VoiceIntentType.SPEAK_HISTORY_ITEM)
    data class SpeakRotationItem(val oneBasedIndex: Int) : VoiceCommand(VoiceIntentType.SPEAK_ROTATION_ITEM)
    data class DeleteHistoryItem(val oneBasedIndex: Int) : VoiceCommand(VoiceIntentType.DELETE_HISTORY_ITEM)
    data object RequestClearHistory : VoiceCommand(VoiceIntentType.REQUEST_CLEAR_HISTORY)

    data class FilterWardrobeCategory(val category: ClothingCategory?) : VoiceCommand(VoiceIntentType.FILTER_WARDROBE_CATEGORY)
    data class SearchWardrobe(val query: String) : VoiceCommand(VoiceIntentType.SEARCH_WARDROBE)
    data object ClearWardrobeSearch : VoiceCommand(VoiceIntentType.CLEAR_WARDROBE_SEARCH)
    data class WardrobeItemAction(
        val action: ItemVoiceAction,
        val oneBasedNumber: Int?,
        val nameQuery: String?
    ) : VoiceCommand(VoiceIntentType.WARDROBE_ITEM_ACTION)

    data object SeedStarterWardrobe : VoiceCommand(VoiceIntentType.SEED_STARTER_WARDROBE)

    data object AddClothing : VoiceCommand(VoiceIntentType.ADD_CLOTHING)
    data object StartAddVideoAi : VoiceCommand(VoiceIntentType.START_ADD_VIDEO_AI)
    data object StartAddRfid : VoiceCommand(VoiceIntentType.START_ADD_RFID)
    data object StartAddManual : VoiceCommand(VoiceIntentType.START_ADD_MANUAL)
    data object PickFromGallery : VoiceCommand(VoiceIntentType.PICK_FROM_GALLERY)

    data object OpenClothingCamera : VoiceCommand(VoiceIntentType.OPEN_CLOTHING_CAMERA)
    data object CapturePhotoNow : VoiceCommand(VoiceIntentType.CAPTURE_PHOTO_NOW)
    data object UseCapturedPhoto : VoiceCommand(VoiceIntentType.USE_CAPTURED_PHOTO)
    data object RetakePhoto : VoiceCommand(VoiceIntentType.RETAKE_PHOTO)
    data class ToggleTorch(val enable: Boolean?) : VoiceCommand(VoiceIntentType.TOGGLE_TORCH)
    data object FlipCamera : VoiceCommand(VoiceIntentType.FLIP_CAMERA)
    data class ToggleScannerVoice(val enable: Boolean?) : VoiceCommand(VoiceIntentType.TOGGLE_SCANNER_VOICE)

    data object ScanRfid : VoiceCommand(VoiceIntentType.SCAN_RFID)
    data object BindRfidTag : VoiceCommand(VoiceIntentType.BIND_RFID_TAG)
    data object ClearRfidTag : VoiceCommand(VoiceIntentType.CLEAR_RFID_TAG)
    data object SimulateRfidDemo : VoiceCommand(VoiceIntentType.SIMULATE_RFID_DEMO)
    data object RecognizeViaCameraAi : VoiceCommand(VoiceIntentType.RECOGNIZE_VIA_CAMERA_AI)

    data class SetFormName(val name: String) : VoiceCommand(VoiceIntentType.SET_FORM_NAME)
    data class SetFormMaterial(val material: String) : VoiceCommand(VoiceIntentType.SET_FORM_MATERIAL)
    data class SetFormCategory(val category: ClothingCategory) : VoiceCommand(VoiceIntentType.SET_FORM_CATEGORY)
    data class SetFormColor(val colorName: String) : VoiceCommand(VoiceIntentType.SET_FORM_COLOR)
    data class SetFormPattern(val pattern: ClothingPattern) : VoiceCommand(VoiceIntentType.SET_FORM_PATTERN)
    data class SetFormOccasion(val occasion: Occasion) : VoiceCommand(VoiceIntentType.SET_FORM_OCCASION)
    data object RemoveFormPhoto : VoiceCommand(VoiceIntentType.REMOVE_FORM_PHOTO)
    data object SaveClothingForm : VoiceCommand(VoiceIntentType.SAVE_CLOTHING_FORM)

    data object MarkWornToday : VoiceCommand(VoiceIntentType.MARK_WORN_TODAY)
    data object EditCurrentClothing : VoiceCommand(VoiceIntentType.EDIT_CURRENT_CLOTHING)
    data object DeleteCurrentClothing : VoiceCommand(VoiceIntentType.DELETE_CURRENT_CLOTHING)

    data class Unknown(val rawText: String) : VoiceCommand(VoiceIntentType.UNKNOWN)

    fun toLegacyVoiceIntent(): VoiceIntent = when (this) {
        is SelectWork -> VoiceIntent.SelectOccasion(Occasion.WORK)
        is SelectHangout -> VoiceIntent.SelectOccasion(Occasion.HANGOUT)
        is SelectSport -> VoiceIntent.SelectOccasion(Occasion.EXERCISE)
        is SelectStreetwear -> VoiceIntent.SelectOccasion(Occasion.STREETWEAR)
        is SelectOther -> VoiceIntent.SelectOccasion(Occasion.OTHER)
        is OpenHome, is OnboardingFinish -> VoiceIntent.Navigate(VoiceDestination.HOME)
        is OpenHistory -> VoiceIntent.Navigate(VoiceDestination.HISTORY)
        is OpenWardrobe -> VoiceIntent.Navigate(VoiceDestination.MANAGE_CLOTHES)
        is NavigateBack, is CancelAction -> VoiceIntent.GoBack
        is RepeatResponse, is ReadPageGuidance -> VoiceIntent.ReadScreen
        is ContinueRecommendation,
        is NextRecommendation,
        is PreviousRecommendation,
        is ListenRecommendations,
        is ViewRecommendationDetail,
        is SelectFirstRecommendation,
        is SelectCurrentRecommendation,
        is SelectNumberedRecommendation,
        is ConfirmWearOutfit,
        is ToggleOutfitCategory,
        is SelectAllOutfitCategories -> VoiceIntent.Navigate(VoiceDestination.PICK_OUTFIT)
        is AddClothing,
        is StartAddVideoAi,
        is StartAddRfid,
        is StartAddManual -> VoiceIntent.AddNewClothing
        is OpenClothingCamera,
        is CapturePhotoNow,
        is UseCapturedPhoto,
        is RetakePhoto,
        is ToggleTorch,
        is FlipCamera,
        is ToggleScannerVoice,
        is ScanRfid,
        is BindRfidTag,
        is ClearRfidTag,
        is SimulateRfidDemo,
        is RecognizeViaCameraAi,
        is PickFromGallery,
        is FilterWardrobeCategory,
        is SearchWardrobe,
        is ClearWardrobeSearch,
        is WardrobeItemAction,
        is SeedStarterWardrobe,
        is SetFormName,
        is SetFormMaterial,
        is SetFormCategory,
        is SetFormColor,
        is SetFormPattern,
        is SetFormOccasion,
        is RemoveFormPhoto,
        is SaveClothingForm,
        is MarkWornToday,
        is EditCurrentClothing,
        is DeleteCurrentClothing -> VoiceIntent.Navigate(VoiceDestination.MANAGE_CLOTHES)
        is SpeakHistoryItem,
        is SpeakRotationItem,
        is DeleteHistoryItem,
        is RequestClearHistory -> VoiceIntent.Navigate(VoiceDestination.HISTORY)
        is OpenOnboarding,
        is OnboardingNext,
        is OnboardingPrevious,
        is ConfirmPositive -> VoiceIntent.Navigate(VoiceDestination.HOME)
        is Unknown -> VoiceIntent.Unknown(rawText)
    }
}
