package com.zuhdiarifazizi.projectakhirpapb.ui.manageclothes

import android.content.Context
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.zuhdiarifazizi.projectakhirpapb.R
import com.zuhdiarifazizi.projectakhirpapb.data.scanner.CameraXGarmentAnalyzer
import com.zuhdiarifazizi.projectakhirpapb.data.scanner.DeviceTiltMonitor
import com.zuhdiarifazizi.projectakhirpapb.domain.scanner.GarmentFrameAnalyzer
import com.zuhdiarifazizi.projectakhirpapb.domain.scanner.ScannerFrameGuidance
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraPrimaryButton
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraSecondaryButton
import com.zuhdiarifazizi.projectakhirpapb.ui.components.LocalClothingPhoto
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraHighContrastInk
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraIndigoSoft
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraIndigoWash
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraNavyDeep
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraSpacing
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraSuccessGreen
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraSurfaceTint
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraTextSecondary
import java.io.File
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@Composable
fun ClothingCameraScreen(
    hud: ScannerHudState,
    captureRequestId: Int,
    capturedPhotoUri: String?,
    onBack: () -> Unit,
    onOpenGallery: () -> Unit,
    onToggleTorch: () -> Unit,
    onToggleCameraFacing: () -> Unit,
    onToggleVoiceGuidance: () -> Unit,
    onSpeakInstruction: () -> Unit,
    onFrameEvaluated: (ScannerFrameGuidance) -> Unit,
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
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }

    val frameAnalyzer = remember { GarmentFrameAnalyzer() }
    val tiltMonitor = remember(context) { DeviceTiltMonitor(context) }
    val tiltState by tiltMonitor.tiltState.collectAsState()
    val latestTiltState by rememberUpdatedState(tiltState)
    val latestFrameCallback by rememberUpdatedState(onFrameEvaluated)

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var boundCamera by remember { mutableStateOf<Camera?>(null) }
    var lastRequestId by remember { mutableIntStateOf(captureRequestId) }

    DisposableEffect(tiltMonitor) {
        tiltMonitor.start()
        onDispose { tiltMonitor.stop() }
    }

    DisposableEffect(Unit) {
        onDispose { analysisExecutor.shutdown() }
    }

    DisposableEffect(lifecycleOwner, previewView, hud.useFrontCamera, capturedPhotoUri) {
        if (capturedPhotoUri != null) {
            return@DisposableEffect onDispose {}
        }
        frameAnalyzer.reset()
        val providerFuture = ProcessCameraProvider.getInstance(context)
        var boundProvider: ProcessCameraProvider? = null
        var disposed = false

        val selector = if (hud.useFrontCamera) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }

        val imageAnalyzer = CameraXGarmentAnalyzer { grid ->
            val tilt = latestTiltState
            val guidance = frameAnalyzer.evaluateFrame(
                grid = grid,
                isDeviceLevel = tilt.isLevel,
                tiltHint = tilt.guidanceHint
            )
            ContextCompat.getMainExecutor(context).execute {
                latestFrameCallback(guidance)
            }
        }

        providerFuture.addListener({
            if (disposed) return@addListener
            runCatching {
                val provider = providerFuture.get()
                val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
                val capture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { it.setAnalyzer(analysisExecutor, imageAnalyzer) }

                provider.unbindAll()
                val camera = provider.bindToLifecycle(lifecycleOwner, selector, preview, capture, analysis)
                boundProvider = provider
                boundCamera = camera
                imageCapture = capture
                onCameraReady()
            }.onFailure { onCameraUnavailable() }
        }, mainExecutor)

        onDispose {
            disposed = true
            imageCapture = null
            boundCamera = null
            boundProvider?.unbindAll()
        }
    }

    LaunchedEffect(boundCamera, hud.isTorchOn) {
        runCatching {
            if (boundCamera?.cameraInfo?.hasFlashUnit() == true) {
                boundCamera?.cameraControl?.enableTorch(hud.isTorchOn)
            }
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
        modifier = modifier
            .fillMaxSize()
            .background(AuraSurfaceTint)
    ) {
        ScannerTopHeader(
            title = if (hud.isFindMode) "Kenali Baju via Kamera" else "Tambah Baju Baru",
            onBack = onBack,
            onSpeakInstruction = onSpeakInstruction
        )

        if (capturedPhotoUri != null) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(AuraSpacing.md),
                verticalArrangement = Arrangement.spacedBy(AuraSpacing.md)
            ) {
                LocalClothingPhoto(
                    imageUri = capturedPhotoUri,
                    contentDescription = "Foto pakaian yang baru diambil",
                    modifier = Modifier.weight(1f),
                    imageHeight = 320.dp
                )
                Text(
                    text = "Foto berhasil diambil. Gunakan foto ini untuk analisis AI dan ekstraksi warna HSV, atau ambil ulang.",
                    style = MaterialTheme.typography.bodyLarge
                )
                AuraPrimaryButton("Gunakan Foto & Analisis AI", onUsePhoto, modifier = Modifier.fillMaxWidth())
                AuraSecondaryButton("Ambil Ulang", onRetake, modifier = Modifier.fillMaxWidth())
            }
        } else {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(AuraNavyDeep)
            ) {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier
                        .fillMaxSize()
                        .semantics {
                            contentDescription = "Pratinjau video pemindai pakaian langsung. ${hud.guidanceText}"
                        }
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    AuraNavyDeep.copy(alpha = 0.72f),
                                    Color.Transparent,
                                    AuraNavyDeep.copy(alpha = 0.90f)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(AuraSpacing.md),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(AuraSpacing.sm)) {
                        TopUtilityQuickActionBar(
                            lightPercent = hud.lightPercent,
                            isTorchOn = hud.isTorchOn,
                            onClose = onBack,
                            onToggleTorch = onToggleTorch,
                            onFlipCamera = onToggleCameraFacing
                        )
                        LiveScannerStatusBanner(
                            hud = hud,
                            onSpeakInstruction = onSpeakInstruction
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(vertical = AuraSpacing.sm),
                        contentAlignment = Alignment.Center
                    ) {
                        ViewfinderCornerBrackets(
                            isLocked = hud.isLockedForAutoCapture,
                            modifier = Modifier
                                .fillMaxWidth(0.84f)
                                .height(220.dp)
                        )
                    }

                    BottomCameraControls(
                        isVoiceGuidanceOn = hud.isVoiceGuidanceOn,
                        onOpenGallery = onOpenGallery,
                        onTakePhoto = takePhoto,
                        onToggleVoiceGuidance = onToggleVoiceGuidance
                    )
                }
            }
        }
    }
}

@Composable
private fun ScannerTopHeader(
    title: String,
    onBack: () -> Unit,
    onSpeakInstruction: () -> Unit
) {
    Surface(color = AuraSurfaceTint.copy(alpha = 0.95f), shadowElevation = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = AuraSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .semantics { contentDescription = "Kembali" }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = null,
                        tint = AuraHighContrastInk,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = AuraHighContrastInk
                    ),
                    modifier = Modifier.semantics { heading() }
                )
            }
            IconButton(
                onClick = onSpeakInstruction,
                modifier = Modifier
                    .size(48.dp)
                    .semantics { contentDescription = "Ulangi panduan suara pemindai" }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_volume_up),
                    contentDescription = null,
                    tint = AuraNavyDeep,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun TopUtilityQuickActionBar(
    lightPercent: Int,
    isTorchOn: Boolean,
    onClose: () -> Unit,
    onToggleTorch: () -> Unit,
    onFlipCamera: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            onClick = onClose,
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.92f),
            modifier = Modifier
                .size(48.dp)
                .semantics { contentDescription = "Tutup kamera dan kembali" }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = null,
                    tint = AuraNavyDeep,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Surface(
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.92f),
            modifier = Modifier
                .height(48.dp)
                .semantics { contentDescription = "Tingkat cahaya $lightPercent persen" }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_info),
                    contentDescription = null,
                    tint = AuraNavyDeep,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Cahaya $lightPercent%",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = AuraNavyDeep
                    )
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(
                onClick = onToggleTorch,
                shape = CircleShape,
                color = if (isTorchOn) AuraIndigoSoft else Color.White.copy(alpha = 0.92f),
                modifier = Modifier
                    .size(48.dp)
                    .semantics {
                        contentDescription = if (isTorchOn) "Matikan lampu senter" else "Nyalakan lampu senter"
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isTorchOn) "⚡On" else "⚡",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = AuraNavyDeep
                        )
                    )
                }
            }

            Surface(
                onClick = onFlipCamera,
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.92f),
                modifier = Modifier
                    .size(48.dp)
                    .semantics { contentDescription = "Balikkan arah kamera" }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.ic_refresh),
                        contentDescription = null,
                        tint = AuraNavyDeep,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveScannerStatusBanner(
    hud: ScannerHudState,
    onSpeakInstruction: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (hud.isLockedForAutoCapture) AuraSuccessGreen else AuraNavyDeep)
                    )
                    Text(
                        text = "AI PEMINDAI AKTIF",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = AuraNavyDeep
                        )
                    )
                }
                Text(
                    text = hud.stepLabel,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = AuraTextSecondary
                    )
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AuraIndigoSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_mic),
                        contentDescription = null,
                        tint = AuraNavyDeep,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = hud.detectedTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF131B2E)
                        )
                    )
                    Text(
                        text = hud.guidanceText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = AuraTextSecondary
                        )
                    )
                }
            }

            Surface(
                onClick = onSpeakInstruction,
                shape = RoundedCornerShape(8.dp),
                color = AuraIndigoWash,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_volume_up),
                        contentDescription = null,
                        tint = AuraNavyDeep,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Dengarkan Petunjuk Posisi",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = AuraNavyDeep
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ViewfinderCornerBrackets(
    isLocked: Boolean,
    modifier: Modifier = Modifier
) {
    val bracketColor = if (isLocked) Color(0xFF76FF03) else Color.White
    Canvas(modifier = modifier) {
        val len = 36.dp.toPx()
        val stroke = 6.dp.toPx()
        val w = size.width
        val h = size.height

        // Top-Left
        drawLine(bracketColor, Offset(0f, 0f), Offset(len, 0f), stroke, StrokeCap.Round)
        drawLine(bracketColor, Offset(0f, 0f), Offset(0f, len), stroke, StrokeCap.Round)
        // Top-Right
        drawLine(bracketColor, Offset(w - len, 0f), Offset(w, 0f), stroke, StrokeCap.Round)
        drawLine(bracketColor, Offset(w, 0f), Offset(w, len), stroke, StrokeCap.Round)
        // Bottom-Left
        drawLine(bracketColor, Offset(0f, h - len), Offset(0f, h), stroke, StrokeCap.Round)
        drawLine(bracketColor, Offset(0f, h), Offset(len, h), stroke, StrokeCap.Round)
        // Bottom-Right
        drawLine(bracketColor, Offset(w, h - len), Offset(w, h), stroke, StrokeCap.Round)
        drawLine(bracketColor, Offset(w - len, h), Offset(w, h), stroke, StrokeCap.Round)
    }
}

@Composable
private fun BottomCameraControls(
    isVoiceGuidanceOn: Boolean,
    onOpenGallery: () -> Unit,
    onTakePhoto: () -> Unit,
    onToggleVoiceGuidance: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable(role = Role.Button, onClick = onOpenGallery)
                    .semantics(mergeDescendants = true) {
                        contentDescription = "Pilih foto pakaian dari galeri"
                    }
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_wardrobe),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text("Galeri", color = Color.White, fontWeight = FontWeight.SemiBold)
            }

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(6.dp)
                    .clip(CircleShape)
                    .background(AuraNavyDeep)
                    .clickable(role = Role.Button, onClick = onTakePhoto)
                    .semantics {
                        contentDescription = "Ambil foto pakaian sekarang"
                        role = Role.Button
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_clothes),
                        contentDescription = null,
                        tint = AuraNavyDeep,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable(role = Role.Button, onClick = onToggleVoiceGuidance)
                    .semantics(mergeDescendants = true) {
                        contentDescription = if (isVoiceGuidanceOn) "Suara aktif, ketuk untuk matikan" else "Suara nonaktif, ketuk untuk aktifkan"
                    }
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isVoiceGuidanceOn) Color.White.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_volume_up),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (isVoiceGuidanceOn) "Suara Aktif" else "Suara Mati",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Button(
            onClick = onTakePhoto,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = AuraNavyDeep)
        ) {
            Text(
                text = "Ambil Foto Sekarang",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        Text(
            text = "Panduan suara akan berbunyi saat sudah terdeteksi",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFFDAE2FD).copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
        )
    }
}
