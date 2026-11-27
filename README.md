# AURA — Project Akhir PAPB TIF - F

Aplikasi Android berbasis **Kotlin + Jetpack Compose** untuk membantu penyandang **tunanetra** dan **buta warna** dalam mengelola lemari pakaian digital (Video Kamera AI, Scan RFID 13,56 MHz, dan Form Manual), mendapatkan rekomendasi kombinasi outfit menggunakan AI offline **Project Harmony (LiteRT)**, memantau riwayat serta rotasi pemakaian 30 hari, dan mengoperasikan seluruh layar aplikasi melalui interaksi suara (*hands-free*).

---

## Anggota Kelompok & Pembagian Modul

| Nama | NIM | Fokus Modul |
| :--- | :--- | :--- |
| **Khansa Nabilah Sukendro** | 225150207111078 | Voice Interaction System (STT, Intent Recognition, Audio & Haptic Feedback) & UI/UX |
| **Nabiel Tatra Edy Firdaus** | 235150207111059 | Pengelompokan Outfit Otomatis (Fungsi 1), Analisis Riwayat & Rotasi Pakaian (Fungsi 2), UI/UX |
| **Alif Kusuma Fauzan** | 245150201111051 | Voice Interaction System (STT, Intent Recognition, Audio & Haptic Feedback) & Perancangan |
| **Zuhdi Arif Azizi** | 245150207111095 | Manage Clothes (Klasifikasi Atribut & Ekstraksi Warna HSV — Fungsi 3) & Arsitektur Proyek |

---

## Aturan Emas Tim (Cuma 3 Poin)

1. **Jangan ngoding atau push langsung ke `main`** — branch `main` dikunci dan hanya bisa masuk lewat Pull Request (PR).
2. **Satu fitur, satu branch sendiri (`feat/<nama-fitur>`)** dan kerjakan di folder fitur masing-masing supaya tidak terjadi *merge conflict*.
3. **Pastikan build hijau sebelum buka PR** — jalankan `.\gradlew.bat assembleDebug` (Windows) atau `./gradlew assembleDebug` (Mac/Linux/Git Bash).

---

## Panduan Arsitektur Proyek (Clean MVVM 3-Layer)

Seluruh source code ditempatkan di dalam `com.zuhdiarifazizi.projectakhirpapb` mengikuti **3-Layer Clean Architecture + MVVM** dengan arah ketergantungan searah ke dalam:

> **Aturan Dependensi:** `UI Layer → Domain Layer ← Data Layer`  
> • Package `domain/` 100% Kotlin murni (0 import `android.*`, `androidx.*`, `data.*`, atau `ui.*`).  
> • Package `ui/` hanya berkomunikasi dengan `ViewModel`, `UiState`, dan kontrak di `domain/` (0 import `data.*`).  
> • Perakitan seluruh implementasi (*Manual Dependency Injection*) dilakukan di satu titik yaitu `AuraApp.kt`.

```text
app/src/main/java/com/zuhdiarifazizi/projectakhirpapb/
├── MainActivity.kt               # Entry point Activity tipis (19 baris, memanggil AuraApp())
├── AuraApp.kt                    # Composition Root, Manual DI, Lifecycle NFC/Sensor & Scaffold
├── domain/                       # Business Logic & Kontrak Murni (Pure Kotlin)
│   ├── model/                    # Model bersama (ClothingItem, HsvColor, HarmonyPrediction, OutfitCandidate, OutfitHistory, ScannerGuidance, VoiceCommand)
│   ├── repository/               # Interface kontrak (ClothesRepository, OutfitRepository, OnboardingRepository, ClothingClassifier, ProjectHarmonyScorer, NfcTagReader, TextToSpeechManager, SpeechRecognizerManager, IntentRecognizer, PhotoStorageRepository, ActivityCategoryRepository)
│   └── usecase/                  # Logika stateless (CalculateOutfitScoreUseCase, AnalyzeOutfitRotationUseCase, MatchClothingFromWardrobeUseCase, GarmentFrameAnalyzer, RecognizeVoiceIntentUseCase, SecurityInputValidator)
├── data/                         # Implementasi Persistensi, AI Engine & Perangkat Keras
│   ├── local/                    # Room SQLite v4 (AuraDatabase + MIGRATION_1_2..3_4, DAO, Entity, OutfitHistoryCodec)
│   ├── remote/                   # Gemini Vision HTTPS API + HsvColorExtractor (fallback offline otomatis)
│   ├── harmony/                  # Google LiteRT Engine (project_harmony.tflite GPU/CPU) + KotlinProjectHarmonyScorer fallback
│   ├── device/                   # AndroidTextToSpeechManager, AndroidSpeechRecognizerManager, IndonesianIntentRecognizer, AndroidNfcTagReader, AndroidHapticFeedbackManager, CameraXGarmentAnalyzer, DeviceTiltMonitor, ClothingPhotoStorage
│   └── repository/               # Implementasi Room & InMemory + DemoClothingCatalog (22 pakaian awal)
└── ui/                           # Stateless Jetpack Compose Screens & ViewModels
    ├── onboarding/               # Onboarding 3 slide beranimasi persisten tanpa login (Screen, ViewModel, UiState)
    ├── home/                     # Beranda 5 aktivitas & tombol suara/panduan (HomeScreen, HomeViewModel, components/)
    ├── pickoutfit/               # Rekomendasi outfit & hasil Project Harmony (PickOutfitScreen, PickOutfitViewModel, UiState)
    ├── history/                  # Statistik 30 hari & rekomendasi rotasi (HistoryScreen, HistoryViewModel, UiState)
    ├── manageclothes/            # Daftar lemari, 3 Opsi Tambah Baju, CameraX AI, RfidAddScreen, Form & Detail
    ├── voice/                    # VoiceViewModel, VoiceInteractionManager & AuraVoiceRouter (15 VoicePage kontekstual)
    ├── components/               # Komponen aksesibel reusable (AuraButton, AuraSpeakingBanner, ClothingImage, FeedbackBanner)
    ├── navigation/               # Enum destinasi utama (AuraDestination)
    └── theme/                    # Design tokens kontras tinggi (Color, Type, Spacing, Theme)
```

### Peta Pengerjaan per Fitur

| Fitur | Layer Domain (`domain/`) | Layer Data (`data/`) | Layer UI (`ui/`) |
| :--- | :--- | :--- | :--- |
| **Onboarding & Beranda** | `OnboardingRepository`, `ActivityCategoryRepository` | `RoomOnboardingRepository`, `DefaultActivityCategoryRepository` | `ui/onboarding/*`, `ui/home/*` |
| **Fungsi 1: Pick Outfit & Project Harmony** | `OutfitCandidate`, `HarmonyPrediction`, `ProjectHarmonyScorer`, `CalculateOutfitScoreUseCase` | `LiteRtHarmonyEngine`, `AndroidProjectHarmonyScorer`, `RoomOutfitRepository` | `ui/pickoutfit/*` |
| **Fungsi 2: History & Rotasi 30 Hari** | `OutfitHistory`, `AnalyzeOutfitRotationUseCase` | `OutfitHistoryDao`, `OutfitHistoryCodec`, `RoomOutfitRepository` | `ui/history/*` |
| **Fungsi 3: Manage Clothes (Video AI, RFID & Manual)** | `ClothingItem`, `HsvColor`, `ClothingClassifier`, `NfcTagReader`, `GarmentFrameAnalyzer`, `MatchClothingFromWardrobeUseCase` | `AuraDatabase`, `RoomClothesRepository`, `GeminiVisionClothingClassifier`, `HsvColorExtractor`, `AndroidNfcTagReader`, `CameraXGarmentAnalyzer`, `DeviceTiltMonitor`, `ClothingPhotoStorage` | `ui/manageclothes/*` |
| **Fungsi 4: Voice System Kontekstual (15 Layar)** | `VoiceCommand`, `VoiceScreenContext`, `IntentRecognizer`, `SpeechRecognizerManager`, `TextToSpeechManager`, `RecognizeVoiceIntentUseCase` | `IndonesianIntentRecognizer`, `AndroidSpeechRecognizerManager`, `AndroidTextToSpeechManager`, `AndroidHapticFeedbackManager` | `ui/voice/*` (`VoiceViewModel`, `AuraVoiceRouter`) |

### Aturan Pengerjaan MVVM (Supaya Tidak Bentrok)
- **UI hanya komunikasi dengan `ViewModel` & `UiState`:** Jangan panggil repository, database, kamera, atau logika perhitungan langsung dari fungsi `@Composable` dan jangan impor `data.*` dari layer `ui/`.
- **Gunakan interface di `domain/repository/`:** Akses data atau hardware melalui interface kontraknya lalu rakit implementasi konkretnya secara terpusat di `AuraApp.kt` (Manual DI Root), menjaga `MainActivity.kt` tetap ringkas.
- **Diskusi dulu sebelum ubah file bersama:** `domain/model/*`, `domain/repository/*`, `AuraApp.kt`, `MainActivity.kt`, dan `gradle/libs.versions.toml` dipakai semua anggota.

---

## Cara Kerja & Push ke GitHub (5 Langkah Mudah — Tinggal Copy-Paste)

> **Prasyarat Sebelum Mulai:** Pastikan kamu sudah **Accept Collaborator Invitation** dari email atau notifikasi GitHub (`https://github.com/zuhdiarif/ProjectAkhirPapb/invitations`) agar tidak terkena error `403 Permission Denied` saat push.

### Langkah 1: Pertama kali clone & cek build
```bash
git clone https://github.com/zuhdiarif/ProjectAkhirPapb.git
cd ProjectAkhirPapb
```
Buka folder project di **Android Studio**, tunggu Gradle sync selesai, lalu cek build awal:
```powershell
# Windows (PowerShell / CMD)
.\gradlew.bat assembleDebug
```
```bash
# Mac / Linux / Git Bash
./gradlew assembleDebug
```

### Langkah 2: Buat branch khusus fiturmu (satu fitur, satu branch)
Gunakan format `feat/<nama-fitur>` sesuai sub-fitur yang sedang kamu kerjakan:
```bash
# Pilih sesuai fitur yang dikerjakan:
git checkout -b feat/pick-outfit
git checkout -b feat/history-rotation
git checkout -b feat/manage-clothes
git checkout -b feat/voice-stt
git checkout -b feat/voice-tts
```

### Langkah 3: Simpan & push kodingan ke branch kamu
Setiap selesai mengerjakan satu bagian kecil:
```bash
git add .
git commit -m "feat: deskripsi singkat apa yang dikerjakan"
git push -u origin NAMA-BRANCH-KAMU
```
*(Contoh: `git push -u origin feat/pick-outfit`. Untuk push berikutnya di branch yang sama, cukup ketik `git push`.)*

### Langkah 4: Buka Pull Request (PR) & Langsung Merge Sendiri
1. Buka halaman repository di GitHub — klik tombol kuning **"Compare & pull request"** untuk branch yang baru di-push.
2. Isi checklist singkat di template PR, lalu klik **Create pull request**.
3. Tunggu pengecekan otomatis **Android CI** selesai (~2–3 menit sampai muncul tanda centang hijau).
4. **Tidak perlu menunggu review/ACC teman lain!** Begitu **Android CI** centang hijau, kamu bisa **langsung klik `Squash and merge` sendiri** -> **Confirm squash and merge** -> **Delete branch**.

> **PENTING (Wajib Baca):** Setelah PR di-**Squash and merge**, branch lama tersebut dianggap **sudah selesai dan mati**. **JANGAN lanjut ngoding atau push lagi dari branch lama** karena akan memicu konflik ganda! Selalu kembali ke `main` dan buat branch baru (lihat Langkah 5).

### Langkah 5: Ambil update terbaru teman & mulai tugas baru
Setiap kali mau mulai tugas baru setelah ada PR yang masuk ke `main`:
```bash
git checkout main
git pull origin main
git checkout -b feat/nama-fitur-baru
```
*(Jika kamu masih mengerjakan branch yang **belum di-merge** dan butuh update terbaru teman dari `main`, jalankan `git pull origin main` dari dalam branch kamu.)*

---

## Alternatif Tanpa Ketik Terminal (Pakai Tombol Android Studio)

1. **Buat branch baru:** Klik nama branch `main` di pojok kiri atas / kanan bawah Android Studio -> **New Branch...** -> tulis nama branch (misal `feat/pick-outfit`).
2. **Commit & Push:** Tekan `Ctrl + K` (Windows) atau `Cmd + K` (Mac) -> centang file yang diubah -> tulis pesan commit -> klik panah kecil di tombol **Commit** lalu pilih **Commit and Push...**.
3. **Ambil update teman:** Klik menu **Git** -> **Update Project** (atau tekan `Ctrl + T` / `Cmd + T`).
4. Setelah push, buka GitHub di browser -> klik **Compare & pull request** -> tunggu **Android CI** hijau -> langsung **Squash and merge** sendiri (tanpa perlu tunggu ACC teman).

---

## Pertolongan Pertama (Jika Mengalami Masalah Git / CI)

### 1. Terlanjur ngoding atau commit di branch `main` lokal
- **Jika belum di-commit:** Langsung buat branch baru (perubahan otomatis terbawa dan aman):
  ```bash
  git checkout -b feat/nama-fiturmu
  ```
- **Jika sudah terlanjur di-commit di `main` (lalu ditolak saat push):** Pindahkan ke branch baru lalu push branch tersebut:
  ```bash
  git checkout -b feat/nama-fiturmu
  git push -u origin feat/nama-fiturmu
  ```
  Setelah PR-nya di-merge, sinkronkan ulang `main` lokalmu dengan:
  ```bash
  git checkout main
  git reset --hard origin/main
  ```

### 2. Android CI silang merah (❌) di halaman Pull Request
- **Jangan tutup PR atau buat PR baru.** Klik **Details** di samping tanda silang untuk melihat baris yang error, perbaiki di laptopmu pada branch yang sama, lalu push ulang:
  ```bash
  git add .
  git commit -m "fix: perbaiki error build"
  git push
  ```
  PR dan Android CI akan langsung ter-update otomatis.

### 3. Terjadi *Merge Conflict*
1. Di dalam branch kamu, tarik update terbaru: `git pull origin main`
2. Buka Android Studio -> klik **Resolve Conflicts** (atau menu **Git** -> **Resolve Conflicts...**).
3. Pilih kode yang ingin dipertahankan (**Accept Left** / **Accept Right** / gabungkan keduanya) -> **Apply**.
4. Tes build lokal (`.\gradlew.bat assembleDebug`), lalu `git add .`, `git commit`, dan `git push`.

---

## Catatan Konfigurasi Lokal

- File `local.properties` otomatis diabaikan oleh `.gitignore` dan akan dibuat oleh Android Studio sesuai lokasi Android SDK di masing-masing laptop.
- *(Opsional)* Tambahkan `GEMINI_API_KEY=<kunci-anda>` di `local.properties` untuk mengaktifkan klasifikasi foto cloud Gemini Vision via `BuildConfig.GEMINI_API_KEY`. Jika kosong atau perangkat offline, aplikasi otomatis menggunakan ekstraksi warna HSV lokal dan model `project_harmony.tflite` secara penuh (*offline-first*). **Jangan pernah** menuliskan API key asli ke file yang dilacak Git.
- Izin perangkat yang dideklarasikan di `AndroidManifest.xml`: `CAMERA`, `RECORD_AUDIO`, `VIBRATE`, `INTERNET`, dan `NFC` (fitur `camera` & `nfc` bertanda `required="false"` agar perangkat tanpa NFC tetap dapat menjalankan mode simulasi demo & galeri).
