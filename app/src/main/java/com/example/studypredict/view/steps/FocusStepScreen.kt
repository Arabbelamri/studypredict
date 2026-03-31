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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.studypredict.localization.localize

@Composable
fun FocusStepScreen(
    stepIndex: Int,
    totalSteps: Int,
    isSubmitting: Boolean,
    onBack: () -> Unit,
    onSeeResult: (Int) -> Unit,
) {
    val bg = Color(0xFFF2F6FF)
    val orange = Color(0xFFFF6A00)
    val percent = (stepIndex.toFloat() / totalSteps.toFloat()).coerceIn(0f, 1f)

    var focus by remember { mutableFloatStateOf(7f) }

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
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(58.dp),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Text("<-", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.size(10.dp))
                    Text("Retour", fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = { onSeeResult(focus.roundToInt()) },
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .weight(1.7f)
                        .height(58.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = orange,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (isSubmitting) localize("Chargement...") else localize("Voir le résultat"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(Modifier.size(10.dp))
                    Text("*", fontSize = 18.sp)
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
                    color = Color(0xFF111827),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${(percent * 100).roundToInt()}%",
                    color = Color(0xFF4B3CFF),
                    fontWeight = FontWeight.Bold
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
                color = orange
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Psychology,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(Modifier.height(22.dp))

            Text(
                text = localize("Niveau de concentration"),
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0B1220)
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = localize("Comment évaluez-vous votre concentration ?"),
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
                            text = focus.roundToInt().toString(),
                            fontSize = 64.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = orange
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "/10",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = orange,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Slider(
                        value = focus,
                        onValueChange = { focus = it },
                        valueRange = 1f..10f,
                        steps = 8
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("1/10", color = Color(0xFF6B7280))
                        Text("10/10", color = Color(0xFF6B7280))
                    }
                }
            }

            Spacer(Modifier.weight(1f))
        }
    }
}
