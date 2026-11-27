package com.zuhdiarifazizi.projectakhirpapb.domain.model

data class LumaChromaGrid(
    val width: Int,
    val height: Int,
    val yLuma: IntArray,
    val centerRgbPixels: IntArray,
    val centerWidth: Int,
    val centerHeight: Int
)

enum class ScannerReadiness {
    LOW_LIGHT,
    NOT_LEVEL,
    NO_GARMENT,
    TOO_FAR,
    TOO_CLOSE_OR_CROPPED,
    READY_HOLD_STEADY
}

enum class ScannerHapticCue {
    TICK,
    WARNING,
    LOCKED_CAPTURE
}

data class ScannerFrameGuidance(
    val lightPercent: Int,
    val needsTorch: Boolean,
    val readiness: ScannerReadiness,
    val stepNumber: Int,
    val stepLabel: String,
    val detectedTitle: String,
    val guidanceText: String,
    val spokenCue: String?,
    val hapticCue: ScannerHapticCue?,
    val isReadyForAutoCapture: Boolean,
    val shouldTriggerAutoCapture: Boolean
)

data class TiltState(
    val isSensorAvailable: Boolean = false,
    val isLevel: Boolean = true,
    val guidanceHint: String = ""
)
