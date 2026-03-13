package com.example.studypredict.view.steps

import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.drawToBitmap
import com.example.studypredict.controller.ResultController
import com.example.studypredict.model.AnalysisResult
import com.example.studypredict.ui.components.ShareResultDialog
import com.example.studypredict.utils.saveBitmapToGallery
import com.example.studypredict.utils.saveTextToDocuments
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun ResultScreen(
    result: AnalysisResult,
    onShare: () -> Unit,
    onNewAnalysis: () -> Unit,
) {
    val bg = Color(0xFFF2F6FF)
    val purple = Color(0xFF6D41FF)
    val orange = Color(0xFFFF6A00)
    val green = Color(0xFF10B981)
    val dark = Color(0xFF0B1220)

    val controller = remember { ResultController() }

    var startAnim by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { startAnim = true }

    val targetProgress = (result.scorePercent.coerceIn(0, 100) / 100f)
    val animatedProgress by animateFloatAsState(
        targetValue = if (startAnim) targetProgress else 0f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "progress"
    )
    val animatedPercent = (animatedProgress * 100).roundToInt()

    var showShareDialog by remember { mutableStateOf(false) }
    var selectedShareTarget by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val view = LocalView.current

    val exportText = remember(result) { controller.buildExportText(result) }

    LaunchedEffect(result) {
        controller.saveToHistory(result)
    }

    Scaffold(containerColor = bg) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 18.dp),
            contentPadding = PaddingValues(top = 18.dp, bottom = 18.dp)
        ) {
            item {
                val topShape = RoundedCornerShape(26.dp)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(18.dp, topShape, clip = false),
                    shape = topShape,
                    color = Color(0xFFF6EDE6)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.EmojiEvents,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.height(44.dp)
                        )

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = "Votre Résultat",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = dark
                        )

                        Spacer(Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = animatedPercent.toString(),
                                fontSize = 72.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = purple
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "%",
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Bold,
                                color = purple,
                                modifier = Modifier
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = orange,
                            modifier = Modifier
                                .height(54.dp)
                                .widthIn(min = 140.dp)
                                .shadow(10.dp, RoundedCornerShape(16.dp), clip = false)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 18.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.WarningAmber,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD9B3)
                                )
                                Spacer(Modifier.width(14.dp))
                                Text(
                                    text = result.grade,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(18.dp))

                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(99.dp)),
                            color = Color(0xFF111827),
                            trackColor = Color(0xFFD1D5DB)
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }

            item {
                SectionCard(
                    title = "Badges débloqués",
                    leadingIcon = "✨",
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        result.badges.take(3).forEach { badge ->
                            AssistChip(
                                onClick = { },
                                label = { Text(badge, fontWeight = FontWeight.SemiBold) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.EmojiEvents,
                                        contentDescription = null
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = Color(0xFF5B55FF),
                                    labelColor = Color.White,
                                    leadingIconContentColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }

            item {
                SectionCard(title = "Détails") {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        DetailItem(
                            modifier = Modifier.weight(1f),
                            label = "Travail",
                            value = "${result.hoursPerWeek}h",
                            trendColor = orange,
                            trendArrow = "↘"
                        )
                        DetailItem(
                            modifier = Modifier.weight(1f),
                            label = "Présence",
                            value = "${result.attendancePercent}%",
                            trendColor = green,
                            trendArrow = "↗"
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        DetailItem(
                            modifier = Modifier.weight(1f),
                            label = "Exercices",
                            value = result.exercisesPerMonth.toString(),
                            trendColor = green,
                            trendArrow = "↗"
                        )
                        DetailItem(
                            modifier = Modifier.weight(1f),
                            label = "Sommeil",
                            value = "${result.sleepHours}h",
                            trendColor = green,
                            trendArrow = "↗"
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }

            item {
                SectionCard(title = "Conseils") {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFFF7E6),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Book,
                                contentDescription = null,
                                tint = Color(0xFFB45309)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = result.advice,
                                color = dark,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(18.dp)) }

            item {
                OutlinedButton(
                    onClick = {
                        selectedShareTarget = null
                        showShareDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Icon(imageVector = Icons.Outlined.IosShare, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text("Partager mon résultat", fontWeight = FontWeight.SemiBold)
                }
            }

            item { Spacer(Modifier.height(12.dp)) }

            item {
                Button(
                    onClick = onNewAnalysis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = purple,
                        contentColor = Color.White
                    )
                ) {
                    Icon(imageVector = Icons.Outlined.Refresh, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text("Nouvelle analyse", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }

        if (showShareDialog) {
            ShareResultDialog(
                link = "https://5c17885f-ce2d-407e-a970-15a",
                selectedTarget = selectedShareTarget,
                onSelectTarget = { target -> selectedShareTarget = target },
                onOpenNetwork = { network ->
                    val url = when (network) {
                        "Facebook" -> "https://www.facebook.com"
                        "Twitter" -> "https://twitter.com"
                        "LinkedIn" -> "https://www.linkedin.com"
                        else -> "https://www.google.com"
                    }
                    controller.openUrl(context, url)
                    showShareDialog = false
                },
                onDismiss = { showShareDialog = false },
                onShare = {
                    val target = selectedShareTarget
                    if (target == null) {
                        Toast.makeText(context, "Choisis WhatsApp ou Email d'abord", Toast.LENGTH_SHORT).show()
                        return@ShareResultDialog
                    }

                    when (target) {
                        "WhatsApp" -> controller.shareToWhatsApp(context, exportText)
                        "Email" -> controller.shareToEmail(context, "Mes résultats StudyPredict", exportText)
                    }

                    onShare()
                    showShareDialog = false
                },
                onSaveImage = {
                    showShareDialog = false
                    scope.launch {
                        listState.animateScrollToItem(0)
                        delay(150)

                        try {
                            val bitmap = view.drawToBitmap()
                            val uri = saveBitmapToGallery(
                                context = context,
                                bitmap = bitmap,
                                displayName = "StudyPredict_${System.currentTimeMillis()}.png"
                            )
                            Toast.makeText(
                                context,
                                if (uri != null) "Image enregistrée ✅" else "Échec de l'enregistrement",
                                Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onSaveText = {
                    showShareDialog = false
                    val uri = saveTextToDocuments(
                        context = context,
                        text = exportText,
                        displayName = "StudyPredict_${System.currentTimeMillis()}.txt"
                    )
                    Toast.makeText(
                        context,
                        if (uri != null) "Texte enregistré ✅" else "Échec de l'enregistrement",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onCopyLink = { },
                onShareTo = { platform ->
                    when (platform) {
                        "WhatsApp" -> selectedShareTarget = "WhatsApp"
                        "Email" -> selectedShareTarget = "Email"
                        "Facebook" -> {
                            controller.openUrl(context, "https://www.facebook.com")
                            showShareDialog = false
                        }
                        "Twitter" -> {
                            controller.openUrl(context, "https://twitter.com")
                            showShareDialog = false
                        }
                        "LinkedIn" -> {
                            controller.openUrl(context, "https://www.linkedin.com")
                            showShareDialog = false
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    leadingIcon: String? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(22.dp)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(14.dp, shape, clip = false),
        shape = shape,
        color = Color(0xFFF7FAFF)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (leadingIcon != null) {
                    Text(leadingIcon, fontSize = 18.sp)
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0B1220)
                )
            }
            Spacer(Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun DetailItem(
    modifier: Modifier,
    label: String,
    value: String,
    trendColor: Color,
    trendArrow: String
) {
    Column(modifier = modifier) {
        Text(label, color = Color(0xFF6B7280))
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
            value,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF0B1220)
        )
            Spacer(Modifier.width(8.dp))
            Text(trendArrow, color = trendColor, fontWeight = FontWeight.ExtraBold)
        }
    }
}