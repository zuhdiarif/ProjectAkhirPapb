package com.zuhdiarifazizi.projectakhirpapb.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
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
import com.zuhdiarifazizi.projectakhirpapb.R
import com.zuhdiarifazizi.projectakhirpapb.ui.home.components.VoiceActionButton
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraBackground
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraCardBorder
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraIndigoSoft
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraNavy
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraNavyDeep
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraShapes
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraSpacing
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraSurface
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraTextMuted
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraTextPrimary
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraTextSecondary

@Composable
fun OnboardingScreen(
    uiState: OnboardingUiState,
    isVoiceListening: Boolean = false,
    partialTranscript: String = "",
    onVoiceClick: () -> Unit = {},
    onSpeakSlide: () -> Unit = {},
    onNext: () -> Unit = {},
    onPrevious: () -> Unit = {},
    onSkip: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (uiState.isCheckingStatus) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(AuraBackground),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator(color = AuraNavy)
                Text(
                    text = "Menyiapkan AURA…",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                )
            }
        }
        return
    }

    var heroVisible by remember { mutableStateOf(false) }
    val cardScrollState = rememberScrollState()
    LaunchedEffect(Unit) {
        heroVisible = true
    }
    LaunchedEffect(uiState.currentPage) {
        cardScrollState.scrollTo(0)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFF3F6FF), AuraBackground, AuraSurface)
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxSize()
                .padding(horizontal = AuraSpacing.md, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AURA",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AuraNavyDeep,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.semantics { heading() }
                )
                TextButton(
                    onClick = onSkip,
                    modifier = Modifier
                        .defaultMinSize(minHeight = 40.dp)
                        .semantics {
                            contentDescription = "Lewati pengenalan Aura dan langsung ke Beranda"
                            role = Role.Button
                        }
                ) {
                    Text(
                        text = "Lewati",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = AuraNavy
                        )
                    )
                }
            }

            AnimatedVisibility(
                visible = heroVisible,
                enter = fadeIn(tween(450)) + slideInVertically(tween(450)) { -it / 4 }
            ) {
                AnimatedAuraLogoHero()
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.semantics {
                    contentDescription = "Halaman ${uiState.currentPage + 1} dari ${uiState.slides.size}"
                }
            ) {
                uiState.slides.indices.forEach { idx ->
                    val active = idx == uiState.currentPage
                    val pillWidth by animateDpAsState(
                        targetValue = if (active) 32.dp else 10.dp,
                        animationSpec = spring(dampingRatio = 0.72f),
                        label = "onboardingPillWidth"
                    )
                    val pillColor by animateColorAsState(
                        targetValue = if (active) Color(0xFFFE5E00) else AuraCardBorder,
                        animationSpec = tween(260),
                        label = "onboardingPillColor"
                    )
                    Box(
                        modifier = Modifier
                            .height(10.dp)
                            .width(pillWidth)
                            .clip(CircleShape)
                            .background(pillColor)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(cardScrollState),
                contentAlignment = Alignment.TopCenter
            ) {
                AnimatedContent(
                    targetState = uiState.currentPage,
                    transitionSpec = {
                        val forward = targetState >= initialState
                        val enterOffset: (Int) -> Int = { fullWidth -> if (forward) fullWidth / 3 else -fullWidth / 3 }
                        val exitOffset: (Int) -> Int = { fullWidth -> if (forward) -fullWidth / 3 else fullWidth / 3 }
                        (fadeIn(tween(260)) + slideInHorizontally(tween(280), enterOffset))
                            .togetherWith(fadeOut(tween(180)) + slideOutHorizontally(tween(220), exitOffset))
                    },
                    label = "onboardingSlideContent"
                ) { pageIndex ->
                    val slide = uiState.slides.getOrElse(pageIndex) { uiState.slides.first() }
                    Surface(
                        shape = AuraShapes.medium,
                        color = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { liveRegion = LiveRegionMode.Polite }
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = AuraIndigoSoft,
                                    shape = RoundedCornerShape(999.dp)
                                ) {
                                    Text(
                                        text = slide.badge,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AuraNavyDeep
                                        ),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }

                                Surface(
                                    onClick = onSpeakSlide,
                                    shape = RoundedCornerShape(999.dp),
                                    color = AuraSurface,
                                    border = BorderStroke(1.dp, AuraCardBorder),
                                    modifier = Modifier
                                        .defaultMinSize(minHeight = 36.dp)
                                        .semantics {
                                            contentDescription = "Dengarkan penjelasan pengenalan Aura halaman ${pageIndex + 1}"
                                            role = Role.Button
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_volume_up),
                                            contentDescription = null,
                                            tint = AuraNavy,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = "Dengarkan",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = AuraNavy
                                            )
                                        )
                                    }
                                }
                            }

                            Text(
                                text = slide.title,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontSize = 19.sp,
                                    lineHeight = 25.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AuraTextPrimary
                                ),
                                modifier = Modifier.semantics { heading() }
                            )

                            Text(
                                text = slide.description,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    color = AuraTextSecondary
                                )
                            )

                            Surface(
                                color = Color(0xFFFFF4ED),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFFFED7AA)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = slide.highlight,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 12.5.sp,
                                        lineHeight = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF7C2D12)
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VoiceActionButton(
                    isListening = isVoiceListening,
                    onClick = onVoiceClick,
                    partialTranscript = partialTranscript
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AuraSpacing.sm)
                ) {
                    if (uiState.currentPage > 0) {
                        OutlinedButton(
                            onClick = onPrevious,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = AuraShapes.medium,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AuraTextPrimary)
                        ) {
                            Text(
                                text = "Sebelumnya",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                softWrap = false,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    val nextLabel = if (uiState.isLastPage) "Mulai AURA" else "Selanjutnya"
                    val nextSemanticLabel = if (uiState.isLastPage) "Mulai Gunakan AURA" else "Selanjutnya"
                    Button(
                        onClick = onNext,
                        modifier = Modifier
                            .weight(if (uiState.currentPage > 0) 1.15f else 1f)
                            .height(52.dp)
                            .semantics { contentDescription = nextSemanticLabel },
                        shape = AuraShapes.medium,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            text = nextLabel,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Text(
                    text = "Ucapkan selanjutnya, kembali, dengarkan, atau lewati.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    ),
                    color = AuraTextMuted,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun AnimatedAuraLogoHero() {
    val infiniteTransition = rememberInfiniteTransition(label = "auraLogoPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoBreath"
    )
    val outerRingProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outerRingPulse"
    )

    Box(
        modifier = Modifier
            .padding(top = 2.dp)
            .size(96.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val baseRadius = size.minDimension * 0.34f
            val ringRadius = baseRadius + (size.minDimension * 0.13f) * outerRingProgress
            val ringAlpha = (1f - outerRingProgress) * 0.45f
            drawCircle(
                color = Color(0xFF01237A).copy(alpha = ringAlpha),
                radius = ringRadius,
                style = Stroke(width = 3.dp.toPx())
            )
            drawCircle(
                color = Color(0xFFFE5E00).copy(alpha = 0.22f),
                radius = size.minDimension * 0.42f,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            shadowElevation = 6.dp,
            color = Color(0xFF01237A),
            border = BorderStroke(2.dp, Color(0xFFE0E7FF)),
            modifier = Modifier
                .size(70.dp)
                .graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                }
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Logo resmi AURA",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
            )
        }
    }
}
