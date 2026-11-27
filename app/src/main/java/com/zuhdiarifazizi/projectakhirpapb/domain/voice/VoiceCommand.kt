package com.zuhdiarifazizi.projectakhirpapb.domain.voice

import com.zuhdiarifazizi.projectakhirpapb.domain.model.Occasion
import com.zuhdiarifazizi.projectakhirpapb.domain.model.VoiceDestination
import com.zuhdiarifazizi.projectakhirpapb.domain.model.VoiceIntent

enum class VoiceIntentType {
    SELECT_WORK,
    SELECT_HANGOUT,
    SELECT_SPORT,
    SELECT_STREETWEAR,
    SELECT_OTHER,
    QUICK_LOGIN,
    LOGOUT_ACCOUNT,
    OPEN_HOME,
    OPEN_HISTORY,
    OPEN_WARDROBE,
    NAVIGATE_BACK,
    REPEAT_RESPONSE,
    CANCEL_ACTION,
    CONTINUE_RECOMMENDATION,
    NEXT_RECOMMENDATION,
    LISTEN_RECOMMENDATIONS,
    SELECT_FIRST_RECOMMENDATION,
    SELECT_CURRENT_RECOMMENDATION,
    ADD_CLOTHING,
    OPEN_CLOTHING_CAMERA,
    SCAN_RFID,
    UNKNOWN
}

sealed class VoiceCommand(val type: VoiceIntentType) {
    data object SelectWork : VoiceCommand(VoiceIntentType.SELECT_WORK)
    data object SelectHangout : VoiceCommand(VoiceIntentType.SELECT_HANGOUT)
    data object SelectSport : VoiceCommand(VoiceIntentType.SELECT_SPORT)
    data object SelectStreetwear : VoiceCommand(VoiceIntentType.SELECT_STREETWEAR)
    data object SelectOther : VoiceCommand(VoiceIntentType.SELECT_OTHER)
    data object QuickLogin : VoiceCommand(VoiceIntentType.QUICK_LOGIN)
    data object LogoutAccount : VoiceCommand(VoiceIntentType.LOGOUT_ACCOUNT)
    data object OpenHome : VoiceCommand(VoiceIntentType.OPEN_HOME)
    data object OpenHistory : VoiceCommand(VoiceIntentType.OPEN_HISTORY)
    data object OpenWardrobe : VoiceCommand(VoiceIntentType.OPEN_WARDROBE)
    data object NavigateBack : VoiceCommand(VoiceIntentType.NAVIGATE_BACK)
    data object RepeatResponse : VoiceCommand(VoiceIntentType.REPEAT_RESPONSE)
    data object CancelAction : VoiceCommand(VoiceIntentType.CANCEL_ACTION)
    data object ContinueRecommendation : VoiceCommand(VoiceIntentType.CONTINUE_RECOMMENDATION)
    data object NextRecommendation : VoiceCommand(VoiceIntentType.NEXT_RECOMMENDATION)
    data object ListenRecommendations : VoiceCommand(VoiceIntentType.LISTEN_RECOMMENDATIONS)
    data object SelectFirstRecommendation : VoiceCommand(VoiceIntentType.SELECT_FIRST_RECOMMENDATION)
    data object SelectCurrentRecommendation : VoiceCommand(VoiceIntentType.SELECT_CURRENT_RECOMMENDATION)
    data object AddClothing : VoiceCommand(VoiceIntentType.ADD_CLOTHING)
    data object OpenClothingCamera : VoiceCommand(VoiceIntentType.OPEN_CLOTHING_CAMERA)
    data object ScanRfid : VoiceCommand(VoiceIntentType.SCAN_RFID)
    data class Unknown(val rawText: String) : VoiceCommand(VoiceIntentType.UNKNOWN)

    fun toLegacyVoiceIntent(): VoiceIntent {
        return when (this) {
            is SelectWork -> VoiceIntent.SelectOccasion(Occasion.WORK)
            is SelectHangout -> VoiceIntent.SelectOccasion(Occasion.HANGOUT)
            is SelectSport -> VoiceIntent.SelectOccasion(Occasion.EXERCISE)
            is SelectStreetwear -> VoiceIntent.SelectOccasion(Occasion.STREETWEAR)
            is SelectOther -> VoiceIntent.SelectOccasion(Occasion.OTHER)
            is QuickLogin -> VoiceIntent.Navigate(VoiceDestination.HOME)
            is LogoutAccount -> VoiceIntent.GoBack
            is OpenHome -> VoiceIntent.Navigate(VoiceDestination.HOME)
            is OpenHistory -> VoiceIntent.Navigate(VoiceDestination.HISTORY)
            is OpenWardrobe -> VoiceIntent.Navigate(VoiceDestination.MANAGE_CLOTHES)
            is NavigateBack -> VoiceIntent.GoBack
            is RepeatResponse -> VoiceIntent.ReadScreen
            is CancelAction -> VoiceIntent.GoBack
            is ContinueRecommendation -> VoiceIntent.Navigate(VoiceDestination.PICK_OUTFIT)
            is NextRecommendation -> VoiceIntent.Navigate(VoiceDestination.PICK_OUTFIT)
            is ListenRecommendations -> VoiceIntent.Navigate(VoiceDestination.PICK_OUTFIT)
            is SelectFirstRecommendation -> VoiceIntent.Navigate(VoiceDestination.PICK_OUTFIT)
            is SelectCurrentRecommendation -> VoiceIntent.Navigate(VoiceDestination.PICK_OUTFIT)
            is AddClothing -> VoiceIntent.AddNewClothing
            is OpenClothingCamera -> VoiceIntent.Navigate(VoiceDestination.MANAGE_CLOTHES)
            is ScanRfid -> VoiceIntent.Navigate(VoiceDestination.MANAGE_CLOTHES)
            is Unknown -> VoiceIntent.Unknown(rawText)
        }
    }
}
