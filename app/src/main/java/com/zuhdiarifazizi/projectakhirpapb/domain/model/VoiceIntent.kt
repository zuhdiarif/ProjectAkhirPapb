package com.zuhdiarifazizi.projectakhirpapb.domain.model

enum class VoiceDestination {
    HOME,
    MANAGE_CLOTHES,
    PICK_OUTFIT,
    HISTORY
}

sealed interface VoiceIntent {
    data class Navigate(val destination: VoiceDestination) : VoiceIntent
    data class SelectOccasion(val occasion: Occasion) : VoiceIntent
    data object AddNewClothing : VoiceIntent
    data object ReadScreen : VoiceIntent
    data object GoBack : VoiceIntent
    data class Unknown(val rawText: String) : VoiceIntent
}
