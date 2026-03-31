package com.example.studypredict.view.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studypredict.localization.LocalAppLocaleState
import com.example.studypredict.localization.localize
import com.example.studypredict.localization.translate

@Composable
fun AuthScreen(
    initialIsLoginMode: Boolean,
    isSubmitting: Boolean,
    serverError: String?,
    onSubmit: (
        isLoginMode: Boolean,
        email: String,
        password: String,
        displayName: String?
    ) -> Unit,
    onBack: () -> Unit
) {
    var isLoginMode by remember { mutableStateOf(initialIsLoginMode) }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val localeState = LocalAppLocaleState.current

    fun validateAndContinue() {
        val cleanEmail = email.trim()
        if (!isLoginMode && fullName.trim().isEmpty()) {
            error = localeState.locale.translate("Le nom est obligatoire.")
            return
        }
        if (cleanEmail.isEmpty() || !cleanEmail.contains("@")) {
            error = localeState.locale.translate("Entrez un email valide.")
            return
        }
        if (password.length < 6) {
            error = localeState.locale.translate("Le mot de passe doit contenir au moins 6 caracteres.")
            return
        }
        if (!isLoginMode && password != confirmPassword) {
            error = localeState.locale.translate("Les mots de passe ne correspondent pas.")
            return
        }
        error = null
        onSubmit(
            isLoginMode,
            cleanEmail,
            password,
            fullName.trim().ifBlank { null }
        )
    }

    val backgroundGradient = Brush.verticalGradient(
        listOf(Color(0xFFF3F7FF), Color(0xFFEAF0FF))
    )
    val primaryGradient = Brush.horizontalGradient(
        listOf(Color(0xFF4B3CFF), Color(0xFFB400FF))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = localize("StudyPredict"),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2E2B5F)
                )
                Text(
                    text = if (isLoginMode) localize("Connexion") else localize("Creer un compte"),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF55506E)
                )

                Spacer(modifier = Modifier.height(4.dp))

                AuthModeRow(
                    isLoginMode = isLoginMode,
                    onLogin = { isLoginMode = true; error = null },
                    onSignup = { isLoginMode = false; error = null }
                )

                if (!isLoginMode) {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text(localize("Nom complet")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                        label = { Text(localize("Email")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                        label = { Text(localize("Mot de passe")) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (!isLoginMode) {
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text(localize("Confirmer le mot de passe")) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                val message = error ?: serverError
                if (message != null) {
                    Text(
                        text = message,
                        color = Color(0xFFB42318),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Button(
                    onClick = ::validateAndContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !isSubmitting,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(primaryGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isSubmitting) localize("Chargement...") else if (isLoginMode) localize("Se connecter") else localize("S'inscrire"),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = if (isLoginMode) localize("Pas de compte ? Creez-en un.") else localize("Deja inscrit ? Connectez-vous."),
                    color = Color(0xFF6B7280),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE6E8F0),
                        contentColor = Color(0xFF2D3142)
                    )
                ) {
                    Text(localize("Retour"))
                }
            }
        }
    }
}

@Composable
private fun AuthModeRow(
    isLoginMode: Boolean,
    onLogin: () -> Unit,
    onSignup: () -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Button(
            onClick = onLogin,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isLoginMode) Color(0xFF4B3CFF) else Color(0xFFE6E8F0),
                contentColor = if (isLoginMode) Color.White else Color(0xFF2D3142)
            )
        ) {
            Text(localize("Login"))
        }
        Button(
            onClick = onSignup,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (!isLoginMode) Color(0xFF4B3CFF) else Color(0xFFE6E8F0),
                contentColor = if (!isLoginMode) Color.White else Color(0xFF2D3142)
            )
        ) {
            Text(localize("Signup"))
        }
    }
}
