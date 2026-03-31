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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import com.example.studypredict.localization.localize

@Composable
fun ExercisesStepScreen(
    stepIndex: Int,
    totalSteps: Int,
    onBack: () -> Unit,
    onNext: (Int) -> Unit,
) {
    val bg = Color(0xFFF2F6FF)
    val purple = Color(0xFFB04BFF)
    val percent = (stepIndex.toFloat() / totalSteps.toFloat()).coerceIn(0f, 1f)

    var valueText by remember { mutableStateOf("15") }
    val valueInt = valueText.toIntOrNull() ?: 0

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
                    modifier = Modifier
                        .weight(1.3f)
                        .height(58.dp),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Text("←", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.size(10.dp))
                    Text("Retour", fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = { onNext(valueInt) },
                    modifier = Modifier
                        .weight(1.7f)
                        .height(58.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = purple,
                        contentColor = Color.White
                    )
                ) {
                    Text(localize("Suivant"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.size(10.dp))
                    Text("→", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                color = purple
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.MenuBook,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(Modifier.height(22.dp))

            Text(
                text = localize("Exercices pratiques"),
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0B1220)
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = localize("Combien d'exercices complétez-vous par mois ?"),
                fontSize = 16.sp,
                color = Color(0xFF6B7280)
            )

            Spacer(Modifier.height(26.dp))

            val cardShape = RoundedCornerShape(22.dp)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
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
                            text = valueInt.toString(),
                            fontSize = 64.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = purple
                        )
                        Spacer(Modifier.size(12.dp))
                        Text(
                            text = localize("exercices"),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE146C2),
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                    }

                    Spacer(Modifier.height(18.dp))

                    TextField(
                        value = valueText,
                        onValueChange = { new ->
                            valueText = new.filter { it.isDigit() }.take(3)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Center,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF0F0F0),
                            unfocusedContainerColor = Color(0xFFF0F0F0),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color(0xFF111827)
                        )
                    )
                }
            }

            Spacer(Modifier.weight(1f))
        }
    }
}
