package com.zuhdiarifazizi.projectakhirpapb.ui.auth

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraPrimaryButton
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraSecondaryButton
import com.zuhdiarifazizi.projectakhirpapb.ui.components.AuraTextField
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraCardSelectedBg
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraNavy
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraSpacing
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraTextPrimary
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraTextSecondary

@Composable
fun AuthScreen(
    uiState: AuthUiState,
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "AURA",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = AuraNavy
                    )
                )
                Text(
                    text = "  Asisten Outfit",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AuraTextSecondary
                )
            }

            Spacer(Modifier.height(AuraSpacing.xl))
            Text(
                text = if (uiState.isRegister) "Buat akun" else "Selamat datang",
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.headlineSmall,
                color = AuraTextPrimary
            )
            Spacer(Modifier.height(AuraSpacing.xs))
            Text(
                text = if (uiState.isRegister) {
                    "Daftar untuk mulai mengatur pilihan pakaianmu."
                } else {
                    "Masuk ke AURA untuk menemukan pakaian yang sesuai."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = AuraTextSecondary
            )

            Spacer(Modifier.height(AuraSpacing.lg))

            if (uiState.isRegister) {
                AuraTextField(
                    value = uiState.name,
                    onValueChange = onNameChange,
                    label = "Nama pengguna",
                    placeholder = "Nama lengkap",
                    errorMessage = uiState.nameError
                )
                Spacer(Modifier.height(AuraSpacing.md))
            }

            AuraTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                label = "Email",
                placeholder = "nama@email.com",
                errorMessage = uiState.emailError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            Spacer(Modifier.height(AuraSpacing.md))

            AuraTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                label = "Password",
                placeholder = "Minimal 8 karakter",
                errorMessage = uiState.passwordError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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

            uiState.statusMessage?.let { message ->
                Spacer(Modifier.height(AuraSpacing.md))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { liveRegion = LiveRegionMode.Polite },
                    shape = MaterialTheme.shapes.medium,
                    color = AuraCardSelectedBg
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(AuraSpacing.md),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AuraTextPrimary
                    )
                }
            }

            Spacer(Modifier.height(AuraSpacing.lg))
            AuraPrimaryButton(
                text = if (uiState.isRegister) "Register" else "Login",
                onClick = onSubmit,
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

            if (!uiState.isRegister) {
                Spacer(Modifier.height(AuraSpacing.xs))
                AuraSecondaryButton(
                    text = "Jelajahi sebagai demo",
                    onClick = onContinueAsDemo,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(AuraSpacing.xs))
                Text(
                    text = "Mode demo tidak membuat akun atau mengirim data login.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics(mergeDescendants = true) {},
                    style = MaterialTheme.typography.bodySmall,
                    color = AuraTextSecondary
                )
            }
        }
    }
}
