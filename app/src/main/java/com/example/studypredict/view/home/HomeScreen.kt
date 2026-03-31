package com.example.studypredict.view.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studypredict.localization.AppLocale
import com.example.studypredict.localization.LocalAppLocaleState
import com.example.studypredict.localization.localize
import com.example.studypredict.ui.theme.TrainingTheme

@Composable
fun StudyPredictHomeScreen(
    isLoggedIn: Boolean,
    onStartAnalysis: () -> Unit,
    onPrediction: () -> Unit,
    onBadges: () -> Unit,
    onTips: () -> Unit,
    onHistory: () -> Unit,
    onReminders: () -> Unit,
    onNotes: () -> Unit,
    onLogin: () -> Unit,
    onProfile: () -> Unit,
) {
    fun runOrAskAuth(action: () -> Unit) {
        if (isLoggedIn) action() else onLogin()
    }

    val screenBg = Color(0xFFF2F6FF)
    val cardBg = Color(0xFFF7FAFF)

    val primaryGrad = Brush.horizontalGradient(
        listOf(Color(0xFF4B3CFF), Color(0xFFB400FF))
    )
    val logoGrad = Brush.radialGradient(
        colors = listOf(Color(0xFF4B3CFF), Color(0xFFB400FF))
    )
    val titleGrad = Brush.horizontalGradient(
        listOf(Color(0xFF4B3CFF), Color(0xFFB400FF))
    )

    val localeState = LocalAppLocaleState.current

    Scaffold(containerColor = screenBg) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = {
                        val next = if (localeState.locale == AppLocale.French) {
                            AppLocale.English
                        } else {
                            AppLocale.French
                        }
                        localeState.onLocaleChange(next)
                    }
                ) {
                    Text(localeState.locale.label)
                }
                Spacer(Modifier.width(4.dp))
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier
                        .shadow(6.dp, CircleShape)
                ) {
                    IconButton(onClick = { if (isLoggedIn) onProfile() else onLogin() }) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = localize("Profil"),
                            tint = Color(0xFF4B3CFF)
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            Box(
                modifier = Modifier
                    .size(86.dp)
                    .shadow(10.dp, CircleShape, clip = false)
                    .clip(CircleShape)
                    .background(logoGrad),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.School,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(Modifier.height(18.dp))

            GradientText(
                text = localize("StudyPredict"),
                brush = titleGrad,
                fontSize = 44.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = localize("Decouvrez votre potentiel academique"),
                color = Color(0xFF6B7280),
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(26.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                FeatureCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.LocationOn,
                    iconTint = Color(0xFF5B55FF),
                    title = localize("Reviser\npres de moi"),
                    background = cardBg,
                    onClick = { runOrAskAuth(onPrediction) }
                )
                FeatureCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.EmojiEvents,
                    iconTint = Color(0xFFF59E0B),
                    title = localize("Badges"),
                    background = cardBg,
                    onClick = { runOrAskAuth(onBadges) }
                )
                FeatureCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.AutoAwesome,
                    iconTint = Color(0xFF8B5CF6),
                    title = localize("Conseils"),
                    background = cardBg,
                    onClick = { runOrAskAuth(onTips) }
                )
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                FeatureCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.NotificationsActive,
                    iconTint = Color(0xFF3B82F6),
                    title = localize("Rappels"),
                    background = cardBg,
                    onClick = { runOrAskAuth(onReminders) }
                )
                FeatureCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Mic,
                    iconTint = Color(0xFF10B981),
                    title = localize("Notes"),
                    background = cardBg,
                    onClick = { runOrAskAuth(onNotes) }
                )
                FeatureCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.History,
                    iconTint = Color(0xFF6B7280),
                    title = localize("Historique"),
                    background = cardBg,
                    onClick = { runOrAskAuth(onHistory) }
                )
            }

            Spacer(Modifier.height(26.dp))

            GradientButton(
                text = localize("Commencer l'analyse"),
                brush = primaryGrad,
                onClick = { runOrAskAuth(onStartAnalysis) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            )

            Spacer(Modifier.height(18.dp))

            Text(
                text = localize("Analyse basee sur 5 criteres essentiels"),
                color = Color(0xFF8A93A3),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FeatureCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    title: String,
    background: Color,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)

    Surface(
        modifier = modifier
            .height(92.dp)
            .shadow(8.dp, shape, clip = false),
        shape = shape,
        color = background,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = Color(0xFF111827),
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun GradientButton(
    text: String,
    brush: Brush,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(18.dp)

    Button(
        onClick = onClick,
        modifier = modifier,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(0.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun GradientText(
    text: String,
    brush: Brush,
    fontSize: TextUnit,
    fontWeight: FontWeight
) {
    Text(
        text = text,
        style = TextStyle(
            brush = brush,
            fontSize = fontSize,
            fontWeight = fontWeight
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewStudyPredictHome() {
    TrainingTheme {
        StudyPredictHomeScreen(
            isLoggedIn = false,
            onStartAnalysis = {},
            onPrediction = {},
            onBadges = {},
            onTips = {},
            onHistory = {},
            onReminders = {},
            onNotes = {},
            onLogin = {},
            onProfile = {}
        )
    }
}
