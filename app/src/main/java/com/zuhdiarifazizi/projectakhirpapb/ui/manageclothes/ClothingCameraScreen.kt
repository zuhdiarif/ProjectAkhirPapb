package com.zuhdiarifazizi.projectakhirpapb.ui.manageclothes

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraPrimaryButton
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraSecondaryButton
import com.zuhdiarifazizi.projectakhirpapb.ui.components.LocalClothingPhoto
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraSpacing
import java.io.File
import java.util.concurrent.Executor

@Composable
fun ClothingCameraScreen(
    captureRequestId: Int,
    capturedPhotoUri: String?,
    onBack: () -> Unit,
    onCameraReady: () -> Unit,
    onCameraUnavailable: () -> Unit,
    onCaptureFailed: () -> Unit,
    onPhotoCaptured: (String) -> Unit,
    onUsePhoto: () -> Unit,
    onRetake: () -> Unit,
    createCameraFile: () -> File,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember(context) {
        PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
    }
    val mainExecutor: Executor = remember(context) { ContextCompat.getMainExecutor(context) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var lastRequestId by remember { mutableIntStateOf(captureRequestId) }

    DisposableEffect(lifecycleOwner, previewView) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        var boundProvider: ProcessCameraProvider? = null
        var disposed = false
        providerFuture.addListener({
            if (disposed) return@addListener
            runCatching {
                val provider = providerFuture.get()
                val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
                val capture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()
                provider.unbindAll()
                provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, capture)
                boundProvider = provider
                imageCapture = capture
                onCameraReady()
            }.onFailure { onCameraUnavailable() }
        }, mainExecutor)
        onDispose {
            disposed = true
            imageCapture = null
            boundProvider?.unbindAll()
        }
    }

    val takePhoto: () -> Unit = {
        val capture = imageCapture
        if (capture == null) {
            onCaptureFailed()
        } else {
            runCatching { createCameraFile() }
                .onSuccess { file ->
                    val output = ImageCapture.OutputFileOptions.Builder(file).build()
                    capture.takePicture(output, mainExecutor, object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(result: ImageCapture.OutputFileResults) {
                            onPhotoCaptured(file.toURI().toString())
                        }

                        override fun onError(exception: ImageCaptureException) {
                            file.delete()
                            onCaptureFailed()
                        }
                    })
                }
                .onFailure { onCaptureFailed() }
        }
    }

    LaunchedEffect(captureRequestId) {
        if (captureRequestId != lastRequestId) {
            lastRequestId = captureRequestId
            takePhoto()
        }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = AuraSpacing.md, vertical = AuraSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(AuraSpacing.sm)
    ) {
        AuraSecondaryButton("Kembali ke Form Pakaian", onBack, modifier = Modifier.fillMaxWidth())
        Text("Kamera Pakaian", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Kamera aktif. Arahkan pakaian ke tengah pratinjau, lalu tekan Ambil Foto. Tidak ada deteksi otomatis.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
        )
        if (capturedPhotoUri != null) {
            LocalClothingPhoto(
                imageUri = capturedPhotoUri,
                contentDescription = "Foto pakaian baru saja diambil",
                modifier = Modifier.weight(1f),
                imageHeight = 320.dp
            )
            Text("Foto berhasil diambil. Anda dapat menggunakannya atau mengambil ulang.", style = MaterialTheme.typography.bodyMedium)
            AuraPrimaryButton("Gunakan Foto", onUsePhoto, modifier = Modifier.fillMaxWidth())
            AuraSecondaryButton("Ambil Ulang", onRetake, modifier = Modifier.fillMaxWidth())
        } else {
            AndroidView(
                factory = { previewView },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "Pratinjau kamera langsung. Arahkan pakaian ke tengah, lalu gunakan tombol Ambil Foto."
                    }
            )
            AuraPrimaryButton("Ambil Foto", takePhoto, modifier = Modifier.fillMaxWidth())
        }
    }
}
