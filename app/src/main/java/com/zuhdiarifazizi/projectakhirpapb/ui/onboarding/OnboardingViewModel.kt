package com.zuhdiarifazizi.projectakhirpapb.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.HapticFeedbackManager
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.OnboardingRepository
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.TextToSpeechManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val onboardingRepository: OnboardingRepository,
    private val ttsManager: TextToSpeechManager? = null,
    private val hapticManager: HapticFeedbackManager? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val completed = runCatching { onboardingRepository.isOnboardingCompleted() }
                .getOrDefault(false)
            _uiState.update { it.copy(isCheckingStatus = false, isCompleted = completed) }
            if (!completed) {
                speakSlide(0)
            }
        }
    }

    fun nextPage(onFinished: () -> Unit = {}) {
        val current = _uiState.value
        if (current.isLastPage) {
            completeOnboarding(onFinished)
            return
        }
        val nextIndex = (current.currentPage + 1).coerceAtMost(current.slides.lastIndex)
        hapticManager?.performButtonClick()
        _uiState.update { it.copy(currentPage = nextIndex) }
        speakSlide(nextIndex)
    }

    fun previousPage(): Boolean {
        val current = _uiState.value
        if (current.currentPage <= 0) return false
        val prevIndex = (current.currentPage - 1).coerceAtLeast(0)
        hapticManager?.performButtonClick()
        _uiState.update { it.copy(currentPage = prevIndex) }
        speakSlide(prevIndex)
        return true
    }

    fun skipOnboarding(onFinished: () -> Unit = {}) {
        completeOnboarding(onFinished)
    }

    fun reopenOnboarding() {
        hapticManager?.performButtonClick()
        _uiState.update { it.copy(isCheckingStatus = false, currentPage = 0) }
        speakSlide(0)
    }

    fun speakCurrentSlide() {
        hapticManager?.performButtonClick()
        speakSlide(_uiState.value.currentPage)
    }

    private fun completeOnboarding(onFinished: () -> Unit) {
        hapticManager?.performSuccess()
        _uiState.update { it.copy(isCompleted = true) }
        viewModelScope.launch {
            runCatching { onboardingRepository.setOnboardingCompleted(true) }
            ttsManager?.speak("Pengenalan selesai. Selamat menggunakan Aura.")
            onFinished()
        }
    }

    private fun speakSlide(index: Int) {
        val slide = _uiState.value.slides.getOrNull(index) ?: return
        ttsManager?.speak("${slide.badge}. ${slide.title}. ${slide.description} ${slide.highlight}")
    }
}
