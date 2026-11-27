package com.zuhdiarifazizi.projectakhirpapb.ui.history

import android.speech.tts.TextToSpeech
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zuhdiarifazizi.projectakhirpapb.data.repository.DemoClothingCatalog
import com.zuhdiarifazizi.projectakhirpapb.domain.feedback.HapticFeedbackManager
import com.zuhdiarifazizi.projectakhirpapb.domain.feedback.TextToSpeechManager
import com.zuhdiarifazizi.projectakhirpapb.domain.model.OutfitHistory
import com.zuhdiarifazizi.projectakhirpapb.domain.model.RotationRecommendation
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.ClothesRepository
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.OutfitRepository
import com.zuhdiarifazizi.projectakhirpapb.domain.usecase.AnalyzeOutfitRotationUseCase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    private val clothesRepository: ClothesRepository,
    private val outfitRepository: OutfitRepository,
    private val analyzeOutfitRotationUseCase: AnalyzeOutfitRotationUseCase = AnalyzeOutfitRotationUseCase(),
    private val ttsManager: TextToSpeechManager? = null,
    private val hapticManager: HapticFeedbackManager? = null,
    private val userIdFlow: StateFlow<String> = MutableStateFlow(DemoClothingCatalog.USER_ID),
    private val nowProvider: () -> Long = { System.currentTimeMillis() }
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userIdFlow
                .flatMapLatest { rawUserId ->
                    val uid = rawUserId.ifBlank { DemoClothingCatalog.USER_ID }
                    combine(
                        clothesRepository.observeClothes(uid),
                        outfitRepository.observeHistory(uid)
                    ) { clothes, history ->
                        val analysis = analyzeOutfitRotationUseCase(
                            clothes = clothes,
                            historyEntries = history,
                            nowEpochMillis = nowProvider()
                        )
                        Triple(clothes.size, analysis.historyLast30Days, analysis.rotationCandidates)
                    }
                }
                .collect { (wardrobeCount, last30Days, rotationCandidates) ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            totalWardrobeCount = wardrobeCount,
                            historyEntries = last30Days,
                            rotationRecommendations = rotationCandidates,
                            rarelyWornItems = rotationCandidates.filter { candidate -> candidate.isRarelyWorn }
                        )
                    }
                }
        }
    }

    fun speakHistoryAndRotationSummary(queueFollowUpSpeech: Boolean = false) {
        val current = _uiState.value
        hapticManager?.performButtonClick()
        val mode = if (queueFollowUpSpeech) TextToSpeech.QUEUE_ADD else TextToSpeech.QUEUE_FLUSH

        val historySentence = if (current.historyEntries.isEmpty()) {
            "Belum ada riwayat pemakaian outfit dalam tiga puluh hari terakhir."
        } else {
            val latest = current.historyEntries.first()
            val itemNames = latest.items.joinToString(", ") { it.name }.ifBlank { "kombinasi outfit" }
            "Terdapat ${current.historyEntries.size} catatan riwayat pemakaian dalam tiga puluh hari terakhir. " +
                "Yang paling baru adalah kategori ${latest.occasion.displayName}: $itemNames."
        }

        val rotationSentence = if (current.rarelyWornItems.isEmpty()) {
            "Semua pakaian di lemari sudah terotasi dengan baik."
        } else {
            val highlights = current.rarelyWornItems.take(3).joinToString(", ") {
                "${it.item.name} dipakai ${it.usageCountLast30Days} kali"
            }
            " Ada ${current.rarelyWornItems.size} pakaian yang jarang dipakai dan direkomendasikan untuk rotasi, antara lain: $highlights."
        }

        val summary = historySentence + rotationSentence
        _uiState.update { it.copy(statusMessage = summary) }
        ttsManager?.speak(summary, queueMode = mode)
    }

    fun speakHistoryEntry(entry: OutfitHistory) {
        hapticManager?.performButtonClick()
        val dateText = formatDate(entry.wornAtEpochMillis)
        val itemNames = entry.items.joinToString(", ") { "${it.name} (${it.color.colorName})" }
            .ifBlank { "pakaian tidak tercatat" }
        val message = "Dipakai pada $dateText untuk aktivitas ${entry.occasion.displayName}, " +
            "skor kecocokan ${(entry.score * 100).toInt()} persen. Pakaian: $itemNames."
        _uiState.update { it.copy(statusMessage = message) }
        ttsManager?.speak(message)
    }

    fun speakRotationRecommendation(recommendation: RotationRecommendation) {
        hapticManager?.performButtonClick()
        val item = recommendation.item
        val usageLabel = if (recommendation.usageCountLast30Days == 0) {
            "belum pernah dipilih dalam tiga puluh hari terakhir"
        } else {
            "baru dipakai ${recommendation.usageCountLast30Days} kali dalam tiga puluh hari terakhir"
        }
        val message = "${item.name}, ${item.category.displayName}, warna ${item.color.colorName}, " +
            "untuk aktivitas ${item.occasion.displayName}. Pakaian ini $usageLabel sehingga cocok dipakai kembali untuk rotasi."
        _uiState.update { it.copy(statusMessage = message) }
        ttsManager?.speak(message)
    }

    fun deleteHistoryEntry(entry: OutfitHistory) {
        viewModelScope.launch {
            runCatching { outfitRepository.deleteHistoryEntry(entry.id) }
                .onSuccess {
                    hapticManager?.performSuccess()
                    val msg = "Catatan riwayat ${entry.occasion.displayName} dihapus."
                    _uiState.update { it.copy(statusMessage = msg) }
                    ttsManager?.speak(msg)
                }
                .onFailure {
                    hapticManager?.performError()
                    val msg = "Catatan riwayat belum dapat dihapus."
                    _uiState.update { it.copy(statusMessage = msg) }
                    ttsManager?.speak(msg)
                }
        }
    }

    fun requestClearHistory() = _uiState.update { it.copy(showClearConfirmation = true) }

    fun cancelClearHistory() = _uiState.update { it.copy(showClearConfirmation = false) }

    fun confirmClearHistory() {
        val uid = userIdFlow.value.ifBlank { DemoClothingCatalog.USER_ID }
        viewModelScope.launch {
            runCatching { outfitRepository.clearHistory(uid) }
                .onSuccess {
                    hapticManager?.performSuccess()
                    val msg = "Semua riwayat pemakaian untuk akun ini telah dibersihkan."
                    _uiState.update { it.copy(showClearConfirmation = false, statusMessage = msg) }
                    ttsManager?.speak(msg)
                }
                .onFailure {
                    hapticManager?.performError()
                    _uiState.update { it.copy(showClearConfirmation = false) }
                }
        }
    }

    private fun formatDate(epochMillis: Long): String {
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id-ID"))
        return formatter.format(Date(epochMillis))
    }
}
