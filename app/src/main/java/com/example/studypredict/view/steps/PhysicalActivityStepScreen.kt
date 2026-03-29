package com.example.studypredict.view.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

@Composable
fun PhysicalActivityStepScreen(
    stepIndex: Int,
    totalSteps: Int,
    onBack: () -> Unit,
    onNext: (hours: Int, extracurricular: Boolean) -> Unit,
) {
    val bg = Color(0xFFF2F6FF)
    val accent = Color(0xFF16A34A)
    val percent = (stepIndex.toFloat() / totalSteps.toFloat()).coerceIn(0f, 1f)
    var value by remember { mutableFloatStateOf(3f) }
    var extracurricular by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = bg,
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 18.dp, bottom = 40.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1.2f).height(58.dp),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Text("<-", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.size(10.dp))
                    Text("Retour", fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = { onNext(value.roundToInt(), extracurricular) },
                    modifier = Modifier.weight(2f).height(58.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color.White)
                ) {
                    Text("Suivant", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.size(10.dp))
                    Text("->", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Etape $stepIndex sur $totalSteps", color = Color(0xFF111827), fontWeight = FontWeight.SemiBold)
                Text("${(percent * 100).roundToInt()}%", color = Color(0xFF4B3CFF), fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { percent },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(99.dp)),
                color = Color(0xFF111827),
                trackColor = Color(0xFFE5E7EB)
            )
            Spacer(Modifier.height(26.dp))
            Surface(
                modifier = Modifier.size(88.dp).shadow(18.dp, RoundedCornerShape(22.dp), clip = false),
                shape = RoundedCornerShape(22.dp),
                color = accent
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.DirectionsRun, contentDescription = null, tint = Color.White, modifier = Modifier.size(44.dp))
                }
            }
            Spacer(Modifier.height(22.dp))
            Text("Activite physique", fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0B1220))
            Spacer(Modifier.height(10.dp))
            Text("Heures d'activite par semaine + activites extrascolaires", fontSize = 16.sp, color = Color(0xFF6B7280))
            Spacer(Modifier.height(26.dp))

            val cardShape = RoundedCornerShape(22.dp)
            Surface(
                modifier = Modifier.fillMaxWidth().height(220.dp).shadow(18.dp, cardShape, clip = false),
                shape = cardShape,
                color = Color(0xFFF7FAFF)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("${value.roundToInt()} h/semaine", fontSize = 46.sp, fontWeight = FontWeight.ExtraBold, color = accent)
                    Spacer(Modifier.height(10.dp))
                    Slider(value = value, onValueChange = { value = it }, valueRange = 0f..24f)
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Activites extrascolaires")
                        Spacer(Modifier.size(12.dp))
                        Switch(checked = extracurricular, onCheckedChange = { extracurricular = it })
                    }
                }
            }
            Spacer(Modifier.weight(1f))
        }
    }
}
