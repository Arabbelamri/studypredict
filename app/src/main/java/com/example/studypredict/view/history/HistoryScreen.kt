package com.example.studypredict.view.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import androidx.compose.runtime.collectAsState
import com.example.studypredict.history.AnalysisRecord
import com.example.studypredict.history.HistoryStore

@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onStartAnalysis: () -> Unit,
) {
    val bg = Color(0xFFF2F6FF)
    val purple = Color(0xFF5B55FF)
    val dark = Color(0xFF0B1220)
    val gray = Color(0xFF6B7280)

    // Pour tester :
    // - vide: demoHistoryEmpty()
    // - non vide: demoHistoryOne()
    val records by HistoryStore.records.collectAsState()

    if (records.isEmpty()) {
        HistoryEmpty(
            bg = bg,
            onBack = onBack,
            onStartAnalysis = onStartAnalysis
        )
        return
    }

    val last = records.first() // on suppose le plus récent en premier
    val avg = records.map { it.scorePercent }.average()
    val best = records.maxOf { it.scorePercent }
    val prev = records.getOrNull(1)?.scorePercent
    val evolution = if (prev == null) 0 else (last.scorePercent - prev)

    Scaffold(containerColor = bg) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 18.dp)
        ) {
            Spacer(Modifier.height(10.dp))

            // Top row: back + clear all
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Retour")
                }
                Text("Retour", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))

                Button(
                    onClick = { HistoryStore.clear() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827)),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, tint = Color.Red)
                    Spacer(Modifier.width(10.dp))
                    Text("Effacer tout", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(8.dp))

            // Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.History,
                    contentDescription = null,
                    tint = purple,
                    modifier = Modifier.size(34.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    "Historique",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = dark
                )
            }
            Text("Suivez votre évolution dans le temps", color = gray)

            Spacer(Modifier.height(18.dp))

            // 4 stat cards
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Adjust,
                    title = "Dernier",
                    value = "${last.scorePercent}%",
                    valueColor = purple
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.BarChart,
                    title = "Moyenne",
                    value = "${avg.roundToInt()}%",
                    valueColor = Color(0xFF2563EB)
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.EmojiEvents,
                    title = "Record",
                    value = "${best}%",
                    valueColor = Color(0xFFF59E0B)
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.TrendingUp,
                    title = "Évolution",
                    value = "${evolution}%",
                    valueColor = if (evolution >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                )
            }

            Spacer(Modifier.height(18.dp))

            // List title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = dark)
                Spacer(Modifier.width(10.dp))
                Text(
                    "Analyses précédentes (${records.size})",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = dark
                )
            }

            Spacer(Modifier.height(12.dp))

            // List (simple Column scroll not needed here for small list; if long, switch to LazyColumn)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                records.forEach { r ->
                    AnalysisRow(
                        record = r,
                        onDelete = { HistoryStore.delete(r.id) }
                    )
                }
            }

            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun HistoryEmpty(
    bg: Color,
    onBack: () -> Unit,
    onStartAnalysis: () -> Unit
) {
    val purple = Color(0xFF5B55FF)
    val dark = Color(0xFF0B1220)
    val gray = Color(0xFF6B7280)

    Scaffold(containerColor = bg) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 18.dp)
        ) {
            // Top back
            Row(
                modifier = Modifier.align(Alignment.TopStart),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Retour")
                }
                Text("Retour", fontWeight = FontWeight.SemiBold)
            }

            // Center empty state
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Outlined.History,
                    contentDescription = null,
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(74.dp)
                )

                Spacer(Modifier.height(14.dp))

                Text(
                    "Aucun historique",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = dark
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    "Effectuez votre première analyse pour commencer\nà suivre vos progrès",
                    color = gray,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(18.dp))

                Button(
                    onClick = onStartAnalysis,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = purple),
                    modifier = Modifier.height(44.dp)
                ) {
                    Text("Commencer une analyse", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    valueColor: Color
) {
    val shape = RoundedCornerShape(18.dp)
    Surface(
        modifier = modifier
            .height(86.dp)
            .shadow(10.dp, shape, clip = false),
        shape = shape,
        color = Color.White
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = valueColor)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, color = Color(0xFF6B7280))
                Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = valueColor)
            }
        }
    }
}

@Composable
private fun AnalysisRow(
    record: AnalysisRecord,
    onDelete: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, shape, clip = false),
        shape = shape,
        color = Color.White
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Score pill
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFF6A00),
                modifier = Modifier.size(62.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "${record.scorePercent}%",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFF6A00)
                    ) {
                        Text(
                            record.grade,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(record.dateLabel, color = Color(0xFF6B7280))
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    "${record.hoursPerWeek}h travail   •   ${record.attendancePercent}% présence",
                    color = Color(0xFF374151),
                    fontWeight = FontWeight.Medium
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = "Supprimer", tint = Color(0xFF111827))
            }
        }
    }
}