package com.example.studypredict.view.steps

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import com.example.studypredict.localization.localize

@Composable
fun StudyHoursStepScreen(
    stepIndex: Int,
    totalSteps: Int,
    onNext: (Int) -> Unit,
    onBack: () -> Unit,
) {
    val bg = Color(0xFFF2F6FF)
    val primary = Color(0xFF1E88FF)
    val percent = (stepIndex.toFloat() / totalSteps.toFloat()).coerceIn(0f, 1f)

    var hours by remember { mutableFloatStateOf(10f) }
    val maxHours = 60f

    Scaffold(
        containerColor = bg,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 60.dp)
            ) {
                Button(
                    onClick = { onNext(hours.roundToInt()) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF06B6D4),
                        contentColor = Color.White
                    )
                ) {
                    Text(localize("Suivant"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(0.dp))
                    Text("  →", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = localize("Étape %d sur %d", stepIndex, totalSteps),
                    color = Color(0xFF111827)
                )
                Text(
                    text = "${(percent * 100).roundToInt()}%",
                    color = Color(0xFF4B3CFF)
                )
            }

            Spacer(Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { percent },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(99.dp)),
                color = Color(0xFF111827),
                trackColor = Color(0xFFE5E7EB)
            )

            Spacer(Modifier.height(26.dp))

            Surface(
                modifier = Modifier
                    .size(88.dp)
                    .shadow(18.dp, RoundedCornerShape(22.dp), clip = false),
                shape = RoundedCornerShape(22.dp),
                color = primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }

            Spacer(Modifier.height(22.dp))

            Text(
                text = localize("Heures de travail"),
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0B1220)
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = localize("Combien d'heures étudiez-vous par semaine ?"),
                fontSize = 16.sp,
                color = Color(0xFF6B7280)
            )

            Spacer(Modifier.height(26.dp))

            val cardShape = RoundedCornerShape(22.dp)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .shadow(18.dp, cardShape, clip = false),
                shape = cardShape,
                color = Color(0xFFF7FAFF)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = hours.roundToInt().toString(),
                            fontSize = 64.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = primary
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = localize("h/sem"),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = primary
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Slider(
                        value = hours,
                        onValueChange = { hours = it },
                        valueRange = 0f..maxHours
                    )

                    Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(localize("%dh/sem", 0), color = Color(0xFF6B7280))
                            Text(localize("%dh/sem", maxHours.roundToInt()), color = Color(0xFF6B7280))
                        }
                }
            }

            Spacer(Modifier.weight(1f))
        }
    }
}
