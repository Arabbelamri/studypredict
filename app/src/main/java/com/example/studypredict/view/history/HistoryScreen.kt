package com.example.studypredict.view.history

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studypredict.network.ApiResult
import com.example.studypredict.network.BackendApi
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException
import kotlin.math.roundToInt

private data class HistoryUiItem(
    val id: Int,
    val scorePercent: Int,
    val grade: String,
    val dateLabel: String,
    val hoursWorked: Double,
    val attendancePercent: Int,
)

private data class HistorySummary(
    val lastScore: Int,
    val averageScore: Int,
    val bestScore: Int,
    val evolution: Int,
)

@Composable
fun HistoryScreen(
    token: String?,
    onBack: () -> Unit,
    onStartAnalysis: () -> Unit,
    onUnauthorized: () -> Unit,
) {
    val bg = Color(0xFFF2F6FF)
    val dark = Color(0xFF0B1220)
    val gray = Color(0xFF6B7280)

    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var records by remember { mutableStateOf<List<HistoryUiItem>>(emptyList()) }

    LaunchedEffect(token) {
        if (token.isNullOrBlank()) {
            loading = false
            error = "Connectez-vous pour voir votre historique."
            records = emptyList()
            return@LaunchedEffect
        }

        loading = true
        error = null

        when (val result = BackendApi.getPredictions(token)) {
            is ApiResult.Success -> {
                records = result.data.map {
                    HistoryUiItem(
                        id = it.id,
                        scorePercent = it.predictedScore.roundToInt().coerceIn(0, 100),
                        grade = it.grade ?: "-",
                        dateLabel = formatIsoDate(it.createdAt),
                        hoursWorked = it.hoursStudied,
                        attendancePercent = it.attendance.roundToInt().coerceIn(0, 100),
                    )
                }
                loading = false
            }

            is ApiResult.Failure -> {
                loading = false
                if (result.unauthorized) {
                    onUnauthorized()
                } else {
                    error = result.message
                }
            }
        }
    }

    if (!loading && records.isEmpty()) {
        HistoryEmpty(
            bg = bg,
            onBack = onBack,
            onStartAnalysis = onStartAnalysis,
            message = error ?: "Aucun historique pour le moment.",
        )
        return
    }

    val summary = computeSummary(records)

    Scaffold(containerColor = bg) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 18.dp),
        ) {
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Retour")
                }
                Text("Retour", fontWeight = FontWeight.SemiBold)
            }

            if (loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 24.dp),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.History,
                                contentDescription = null,
                                tint = Color(0xFF5B55FF),
                                modifier = Modifier.size(34.dp),
                            )
                            Spacer(Modifier.size(10.dp))
                            Text(
                                "Historique",
                                fontSize = 34.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = dark,
                            )
                        }

                        Text("Suivez votre evolution dans le temps", color = gray)
                        Spacer(Modifier.height(12.dp))

                        if (summary != null) {
                            SummaryGrid(summary = summary)
                        }
                    }

                    item {
                        ScoreChartCard(
                            points = records.map { it.scorePercent },
                        )
                    }

                    item {
                        Text(
                            "Analyses precedentes (${records.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = dark,
                        )
                    }

                    items(records, key = { it.id }) { record ->
                        HistoryRow(record = record)
                    }

                    item { Spacer(Modifier.height(10.dp)) }
                }
            }
        }
    }
}

private fun computeSummary(records: List<HistoryUiItem>): HistorySummary? {
    if (records.isEmpty()) return null

    val latest = records.first().scorePercent
    val oldest = records.last().scorePercent
    val average = (records.sumOf { it.scorePercent }.toDouble() / records.size).roundToInt()
    val best = records.maxOf { it.scorePercent }

    return HistorySummary(
        lastScore = latest,
        averageScore = average,
        bestScore = best,
        evolution = latest - oldest,
    )
}

@Composable
private fun SummaryGrid(summary: HistorySummary) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SummaryCard(
                modifier = Modifier.weight(1f),
                title = "Dernier",
                value = "${summary.lastScore}%",
                valueColor = Color(0xFF4F46E5),
            )
            SummaryCard(
                modifier = Modifier.weight(1f),
                title = "Moyenne",
                value = "${summary.averageScore}%",
                valueColor = Color(0xFF2563EB),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SummaryCard(
                modifier = Modifier.weight(1f),
                title = "Record",
                value = "${summary.bestScore}%",
                valueColor = Color(0xFFD97706),
            )
            SummaryCard(
                modifier = Modifier.weight(1f),
                title = "Evolution",
                value = "${if (summary.evolution > 0) "+" else ""}${summary.evolution}%",
                valueColor = if (summary.evolution < 0) Color(0xFFDC2626) else Color(0xFF16A34A),
            )
        }
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier,
    title: String,
    value: String,
    valueColor: Color,
) {
    val shape = RoundedCornerShape(16.dp)
    Surface(
        modifier = modifier.shadow(8.dp, shape, clip = false),
        shape = shape,
        color = Color(0xFFF7F9FF),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, color = Color(0xFF6B7280), fontSize = 13.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, color = valueColor, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp)
        }
    }
}

@Composable
private fun ScoreChartCard(points: List<Int>) {
    val shape = RoundedCornerShape(18.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, shape, clip = false),
        shape = shape,
        color = Color(0xFFF7F9FF),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.ShowChart,
                    contentDescription = null,
                    tint = Color(0xFF5B55FF),
                )
                Spacer(Modifier.size(6.dp))
                Text(
                    "Evolution des scores",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937),
                )
            }

            Spacer(Modifier.height(10.dp))

            val chartPoints = if (points.size <= 12) points else points.take(12)

            if (chartPoints.size < 2) {
                Text("Pas assez de donnees pour la courbe.", color = Color(0xFF6B7280))
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp),
                ) {
                    val leftPad = 18f
                    val rightPad = 14f
                    val topPad = 12f
                    val bottomPad = 18f
                    val chartW = size.width - leftPad - rightPad
                    val chartH = size.height - topPad - bottomPad

                    for (i in 0..4) {
                        val y = topPad + (chartH / 4f) * i
                        drawLine(
                            color = Color(0xFFE5E7EB),
                            start = Offset(leftPad, y),
                            end = Offset(leftPad + chartW, y),
                            strokeWidth = 2f,
                        )
                    }

                    val path = Path()
                    val fillPath = Path()
                    chartPoints.forEachIndexed { index, score ->
                        val x =
                            leftPad + if (chartPoints.size == 1) 0f else (chartW / (chartPoints.size - 1)) * index
                        val y = topPad + (1f - (score.coerceIn(0, 100) / 100f)) * chartH
                        if (index == 0) {
                            path.moveTo(x, y)
                            fillPath.moveTo(x, topPad + chartH)
                            fillPath.lineTo(x, y)
                        } else {
                            path.lineTo(x, y)
                            fillPath.lineTo(x, y)
                        }
                    }

                    val lastX = leftPad + chartW
                    fillPath.lineTo(lastX, topPad + chartH)
                    fillPath.close()

                    drawPath(path = fillPath, color = Color(0x1A5B55FF), style = Fill)
                    drawPath(
                        path = path,
                        color = Color(0xFF5B55FF),
                        style = Stroke(width = 5f, cap = StrokeCap.Round),
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryEmpty(
    bg: Color,
    onBack: () -> Unit,
    onStartAnalysis: () -> Unit,
    message: String,
) {
    val dark = Color(0xFF0B1220)
    val gray = Color(0xFF6B7280)

    Scaffold(containerColor = bg) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 18.dp),
        ) {
            Row(
                modifier = Modifier.align(Alignment.TopStart),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Retour")
                }
                Text("Retour", fontWeight = FontWeight.SemiBold)
            }

            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Outlined.History,
                    contentDescription = null,
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(74.dp),
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    "Aucun historique",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = dark,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = message,
                    color = gray,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(18.dp))
                Button(onClick = onStartAnalysis) {
                    Text("Commencer une analyse")
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(record: HistoryUiItem) {
    val shape = RoundedCornerShape(18.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, shape, clip = false),
        shape = shape,
        color = Color.White,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val badgeColor = scoreBadgeColor(record.scorePercent)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = badgeColor,
                modifier = Modifier.size(54.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "${record.scorePercent}%",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                    )
                }
            }

            Spacer(Modifier.size(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    GradeChip(record.grade)
                    Text(record.dateLabel, color = Color(0xFF6B7280), fontSize = 13.sp)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "${formatHours(record.hoursWorked)}h travail  •  ${record.attendancePercent}% presence",
                    color = Color(0xFF4B5563),
                    fontSize = 14.sp,
                )
            }
        }
    }
}

@Composable
private fun GradeChip(grade: String) {
    val bg =
        when (grade.trim().uppercase()) {
            "A+" -> Color(0xFF10B981)
            "A" -> Color(0xFF22C55E)
            "B" -> Color(0xFF3B82F6)
            "C" -> Color(0xFFF59E0B)
            "D" -> Color(0xFFF97316)
            else -> Color(0xFF9CA3AF)
        }

    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(grade, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

private fun scoreBadgeColor(score: Int): Color {
    return when {
        score >= 85 -> Color(0xFF10B981)
        score >= 70 -> Color(0xFF3B82F6)
        score >= 55 -> Color(0xFFF59E0B)
        else -> Color(0xFFF97316)
    }
}

private fun formatHours(value: Double): String {
    val rounded = value.roundToInt()
    return if (kotlin.math.abs(value - rounded) < 0.01) {
        rounded.toString()
    } else {
        String.format("%.1f", value)
    }
}

private fun formatIsoDate(value: String): String {
    return try {
        val parsed = OffsetDateTime.parse(value)
        "${parsed.dayOfMonth}/${parsed.monthValue}/${parsed.year} ${parsed.hour.toString().padStart(2, '0')}:${parsed.minute.toString().padStart(2, '0')}"
    } catch (_: DateTimeParseException) {
        value
    }
}
