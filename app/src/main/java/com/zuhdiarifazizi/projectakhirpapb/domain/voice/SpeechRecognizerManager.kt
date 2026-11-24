package com.zuhdiarifazizi.projectakhirpapb.domain.voice

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

interface SpeechRecognizerManager {
    fun startListening(
        onResult: (String) -> Unit,
        onError: (VoiceRecognitionError) -> Unit,
        onPartialResult: ((String) -> Unit)? = null,
        onListeningStateChanged: ((Boolean) -> Unit)? = null
    )
    fun stopListening()
    fun destroy()
    val isListening: Boolean
}
