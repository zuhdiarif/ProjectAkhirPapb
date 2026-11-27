package com.zuhdiarifazizi.projectakhirpapb.ui.auth

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.zuhdiarifazizi.projectakhirpapb.R
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraCard
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraPrimaryButton
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraSecondaryButton
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraTextField
import com.zuhdiarifazizi.projectakhirpapb.ui.home.components.VoiceActionButton
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraCardBorder
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraCardSelectedBg
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraHelperTextStyle
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraIndigoSoft
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraNavy
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraNavyDeep
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraSpacing
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraSurface
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraTextPrimary
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraTextSecondary

@Composable
fun AuthScreen(
    uiState: AuthUiState,
    isVoiceListening: Boolean = false,
    partialTranscript: String = "",
    onVoiceClick: () -> Unit = {},
    onSpeakGuidance: () -> Unit = {},
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onToggleConfirmPasswordVisibility: () -> Unit,
    onSubmit: () -> Unit,
    onShowRegister: () -> Unit,
    onShowLogin: () -> Unit,
    onContinueAsDemo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    if (uiState.isSessionLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator(color = AuraNavy)
                Text(
                    text = "Memuat sesi Aura…",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                )
            }
        }
        return
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AuraSpacing.lg, vertical = AuraSpacing.md)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.semantics { heading() }
            ) {
                Text(
                    text = "AURA",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = AuraNavy
                    )
                )
                Text(
                    text = "  Asisten Outfit Ramah Aksesibilitas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AuraTextSecondary
                )
            }

            Spacer(Modifier.height(AuraSpacing.md))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .semantics(mergeDescendants = true) {
                        contentDescription = "Dengarkan panduan halaman masuk Aura. Ketuk dua kali untuk mendengarkan instruksi suara."
                        role = Role.Button
                    }
                    .clickable(
                        onClickLabel = "memutar panduan halaman masuk",
                        role = Role.Button,
                        onClick = onSpeakGuidance
                    ),
                shape = RoundedCornerShape(12.dp),
                color = AuraSurface,
                border = BorderStroke(1.dp, AuraCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_volume_up),
                        contentDescription = null,
                        tint = AuraNavy,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Dengarkan Panduan Halaman",
                        style = AuraHelperTextStyle.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = AuraNavy
                        )
                    )
                }
            }

            Spacer(Modifier.height(AuraSpacing.sm))

            VoiceActionButton(
                isListening = isVoiceListening,
                onClick = onVoiceClick,
                partialTranscript = partialTranscript
            )

            Spacer(Modifier.height(AuraSpacing.md))

            Surface(
                color = AuraIndigoSoft,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(AuraSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Akses Cepat Disabilitas",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = AuraNavyDeep
                        ),
                        modifier = Modifier.semantics { heading() }
                    )
                    Text(
                        text = "Langsung masuk ke Beranda tanpa mengetik email atau password. Koleksi pakaian contoh siap pakai tersedia otomatis.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AuraNavyDeep
                    )
                    AuraPrimaryButton(
                        text = "Masuk Cepat Tanpa Ketik (Akun Lokal)",
                        onClick = onContinueAsDemo,
                        enabled = !uiState.isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                contentDescription = "Masuk cepat tanpa ketik ke akun lokal Aura. Langsung menuju halaman Beranda."
                            }
                    )
                }
            }

            Spacer(Modifier.height(AuraSpacing.lg))
            Text(
                text = if (uiState.isRegister) "Atau buat akun pribadi lokal" else "Atau masuk dengan akun lokal",
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.titleLarge,
                color = AuraTextPrimary
            )
            Spacer(Modifier.height(AuraSpacing.xs))
            Text(
                text = if (uiState.isRegister) {
                    "Data akun disimpan aman di perangkat ini dengan koleksi pakaian awal yang beragam."
                } else {
                    "Masuk dengan akun lokal yang pernah didaftarkan di perangkat ini. Sekali masuk, sesi tersimpan otomatis."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = AuraTextSecondary
            )

            Spacer(Modifier.height(AuraSpacing.md))

            if (uiState.isRegister) {
                AuraTextField(
                    value = uiState.name,
                    onValueChange = onNameChange,
                    label = "Nama pengguna",
                    placeholder = "Nama lengkap",
                    errorMessage = uiState.nameError,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )
                Spacer(Modifier.height(AuraSpacing.md))
            }

            AuraTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                label = "Email",
                placeholder = "nama@email.com",
                errorMessage = uiState.emailError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )
            Spacer(Modifier.height(AuraSpacing.md))

            AuraTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                label = "Password",
                placeholder = "Minimal 8 karakter",
                errorMessage = uiState.passwordError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = if (uiState.isRegister) ImeAction.Next else ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                    onDone = {
                        focusManager.clearFocus()
                        onSubmit()
                    }
                ),
                visualTransformation = if (uiState.isPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingAction = {
                    TextButton(
                        onClick = onTogglePasswordVisibility,
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .semantics {
                                contentDescription = if (uiState.isPasswordVisible) "Sembunyikan password" else "Tampilkan password"
                            }
                    ) {
                        Text(
                            text = if (uiState.isPasswordVisible) "Sembunyikan" else "Tampilkan",
                            style = MaterialTheme.typography.labelSmall,
                            color = AuraNavy
                        )
                    }
                }
            )

            if (uiState.isRegister) {
                Spacer(Modifier.height(AuraSpacing.md))
                AuraTextField(
                    value = uiState.confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    label = "Konfirmasi password",
                    placeholder = "Ulangi password",
                    errorMessage = uiState.confirmPasswordError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            onSubmit()
                        }
                    ),
                    visualTransformation = if (uiState.isConfirmPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingAction = {
                        TextButton(
                            onClick = onToggleConfirmPasswordVisibility,
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .semantics {
                                    contentDescription = if (uiState.isConfirmPasswordVisible) {
                                        "Sembunyikan konfirmasi password"
                                    } else {
                                        "Tampilkan konfirmasi password"
                                    }
                                }
                        ) {
                            Text(
                                text = if (uiState.isConfirmPasswordVisible) "Sembunyikan" else "Tampilkan",
                                style = MaterialTheme.typography.labelSmall,
                                color = AuraNavy
                            )
                        }
                    }
                )
            }

            uiState.errorMessage?.let { err ->
                Spacer(Modifier.height(AuraSpacing.md))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { liveRegion = LiveRegionMode.Assertive },
                    shape = MaterialTheme.shapes.medium,
                    color = Color(0xFFFEE2E2)
                ) {
                    Text(
                        text = err,
                        modifier = Modifier.padding(AuraSpacing.md),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF991B1B)
                    )
                }
            }

            uiState.statusMessage?.let { message ->
                Spacer(Modifier.height(AuraSpacing.md))
                AuraCard(
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AuraTextPrimary
                    )
                }
            }

            Spacer(Modifier.height(AuraSpacing.md))
            AuraSecondaryButton(
                text = if (uiState.isRegister) "Register Akun Lokal" else "Login Akun Lokal",
                onClick = onSubmit,
                enabled = !uiState.isSubmitting,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = AuraSpacing.sm),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (uiState.isRegister) "Sudah punya akun?" else "Belum punya akun?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AuraTextSecondary
                )
                TextButton(
                    onClick = if (uiState.isRegister) onShowLogin else onShowRegister,
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text(
                        text = if (uiState.isRegister) "Login" else "Register",
                        color = AuraNavy
                    )
                }
            }
        }
    }
}
