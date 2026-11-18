# AURA — Project Akhir PAPB TIF - F

Aplikasi Android berbasis **Kotlin + Jetpack Compose** untuk membantu penyandang tunanetra dalam mengelola lemari pakaian digital, mendapatkan rekomendasi kombinasi outfit sesuai acara dan harmoni warna, memantau riwayat serta rotasi pemakaian, dan mengoperasikan aplikasi melalui interaksi suara (*hands-free*).

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

## Panduan Arsitektur Proyek (MVVM Layer-First)

Seluruh source code ditempatkan di dalam `com.zuhdiarifazizi.projectakhirpapb` dengan struktur **MVVM** yang terbagi menjadi 3 layer utama:

```text
app/src/main/java/com/zuhdiarifazizi/projectakhirpapb/
├── domain/                 # Aturan bisnis murni & kontrak data
│   ├── model/              # Model data bersama (User, ClothingItem, HsvColor, OutfitCandidate, OutfitHistory, VoiceIntent)
│   ├── classifier/         # Interface ClothingClassifier & ClassificationResult
│   ├── repository/         # Interface AuthRepository, ClothesRepository, OutfitRepository
│   └── usecase/            # Logika utama (CalculateOutfitScore, AnalyzeOutfitRotation, RecognizeVoiceIntent)
├── data/                   # Implementasi sumber data & layanan pemrosesan
│   ├── classifier/         # DefaultClothingClassifier & HsvColorExtractor
│   └── repository/         # Implementasi Repository (Auth, Clothes, Outfit)
├── ui/                     # Tampilan Compose & State Management per fitur
│   ├── auth/               # Login & Registrasi (AuthScreen, AuthViewModel, AuthUiState)
│   ├── manageclothes/      # Modul Manage Clothes (ManageClothesScreen, ManageClothesViewModel, ManageClothesUiState)
│   ├── pickoutfit/         # Modul Pick Outfit (PickOutfitScreen, PickOutfitViewModel, PickOutfitUiState)
│   ├── history/            # Modul History & Rotasi (HistoryScreen, HistoryViewModel, HistoryUiState)
│   ├── voice/              # State & controller Voice Interaction (VoiceViewModel, VoiceInteractionState)
│   ├── navigation/         # Daftar rute halaman (AuraDestination)
│   └── theme/              # Design tokens (Color, Type, Theme)
└── MainActivity.kt         # Entry point dan shell navigasi aplikasi
```

### Peta Pengerjaan per Fitur

| Fitur | Layer Domain (`domain/`) | Layer Data (`data/`) | Layer UI (`ui/`) |
| :--- | :--- | :--- | :--- |
| **Auth (Shared)** | `User`, `AuthRepository` | `InMemoryAuthRepository` *(atau implementasi persistence final)* | `ui/auth/*` |
| **Fungsi 1: Pick Outfit** | `OutfitCandidate`, `CalculateOutfitScoreUseCase` | `OutfitRepository` | `ui/pickoutfit/*` |
| **Fungsi 2: History & Rotasi** | `OutfitHistory`, `AnalyzeOutfitRotationUseCase` | `OutfitRepository` | `ui/history/*` |
| **Fungsi 3: Manage Clothes** | `ClothingItem`, `HsvColor`, `ClothingClassifier`, `ClothesRepository` | `HsvColorExtractor`, `DefaultClothingClassifier`, `ClothesRepository` | `ui/manageclothes/*` |
| **Fungsi 4: Voice System** | `VoiceIntent`, `RecognizeVoiceIntentUseCase` | Layanan input/output suara & haptic | `ui/voice/*` & integrasi navigasi |

### Aturan Pengerjaan MVVM (Supaya Tidak Bentrok)
- **UI hanya komunikasi dengan `ViewModel` & `UiState`:** Jangan panggil repository, kamera, atau logika perhitungan langsung dari fungsi `@Composable`.
- **Gunakan interface di `domain/repository/`:** Jika butuh data pakaian atau riwayat, panggil lewat `ClothesRepository` / `OutfitRepository`.
- **Diskusi dulu sebelum ubah file bersama:** `domain/model/*`, `domain/repository/*`, `MainActivity.kt`, dan `gradle/libs.versions.toml` dipakai semua anggota.

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
- Izin perangkat yang sudah disiapkan di `AndroidManifest.xml`: `CAMERA`, `RECORD_AUDIO`, dan `VIBRATE`.
