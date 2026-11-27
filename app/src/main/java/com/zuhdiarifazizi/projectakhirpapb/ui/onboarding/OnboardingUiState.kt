package com.zuhdiarifazizi.projectakhirpapb.ui.onboarding

data class OnboardingSlide(
    val badge: String,
    val title: String,
    val description: String,
    val highlight: String
)

data class OnboardingUiState(
    val isCheckingStatus: Boolean = true,
    val isCompleted: Boolean = false,
    val currentPage: Int = 0,
    val slides: List<OnboardingSlide> = DEFAULT_ONBOARDING_SLIDES
) {
    val activeSlide: OnboardingSlide
        get() = slides.getOrElse(currentPage) { slides.first() }

    val isLastPage: Boolean
        get() = currentPage >= slides.lastIndex

    companion object {
        val DEFAULT_ONBOARDING_SLIDES = listOf(
            OnboardingSlide(
                badge = "Bagian 1 dari 3",
                title = "Selamat Datang di AURA",
                description = "Asisten lemari pintar tanpa login dengan kontras tinggi dan panduan suara bagi tunanetra serta buta warna.",
                highlight = "22 koleksi pakaian siap pakai langsung tersedia otomatis di Beranda."
            ),
            OnboardingSlide(
                badge = "Bagian 2 dari 3",
                title = "3 Cara Mudah Tambah Baju",
                description = "Tambahkan pakaian lewat Video Kamera AI yang tenang, Scan Tag RFID 13,56 MHz, atau Isi Formulir Manual.",
                highlight = "Setiap warna dilengkapi nama jelas Bahasa Indonesia serta keterangan kontras."
            ),
            OnboardingSlide(
                badge = "Bagian 3 dari 3",
                title = "AI Harmoni & Kontrol Suara",
                description = "Project Harmony menilai keserasian warna secara offline, dan tiap aksi layar dapat dijalankan lewat suara.",
                highlight = "Tekan tombol mikrofon kapan saja untuk menjalankan aksi di halaman aktif."
            )
        )
    }
}
