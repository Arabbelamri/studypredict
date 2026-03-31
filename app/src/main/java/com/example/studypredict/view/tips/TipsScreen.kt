package com.example.studypredict.view.tips

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studypredict.localization.localize
import com.example.studypredict.network.ApiResult
import com.example.studypredict.network.BackendApi
import com.example.studypredict.network.RemoteTip

@Composable
fun TipsScreen(
    token: String?,
    onBack: () -> Unit,
    onUnauthorized: () -> Unit
) {
    val bg = Color(0xFFF2F6FF)

    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var tips by remember { mutableStateOf<List<RemoteTip>>(emptyList()) }

    LaunchedEffect(token) {
        if (token.isNullOrBlank()) {
            loading = false
            error = "Connectez-vous pour voir vos conseils."
            tips = emptyList()
            return@LaunchedEffect
        }
        loading = true
        error = null
        when (val result = BackendApi.getLatestTips(token)) {
            is ApiResult.Success -> {
                loading = false
                tips = result.data
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

    Scaffold(containerColor = bg) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 18.dp)
        ) {
            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = localize("Retour"))
                }
                Text(localize("Retour"), fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(8.dp))
            Text(localize("Conseils"), fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0B1220))
            Text(localize("Basés sur votre dernière prédiction"), color = Color(0xFF6B7280))
            Spacer(Modifier.height(14.dp))

            when {
                loading -> CircularProgressIndicator()
                tips.isEmpty() -> {
                    Text(
                        text = error ?: localize("Aucun conseil pour le moment. Lancez une prédiction d'abord."),
                        color = Color(0xFF374151)
                    )
                }

                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(tips, key = { it.id }) { tip ->
                            TipRow(tip = tip)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TipRow(tip: RemoteTip) {
    val shape = RoundedCornerShape(16.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, shape, clip = false),
        shape = shape,
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(tip.title, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0B1220))
            Spacer(Modifier.height(6.dp))
            Text(tip.description, color = Color(0xFF374151))
            Spacer(Modifier.height(8.dp))
            Text(localize("Catégorie : %s", tip.category), color = Color(0xFF6B7280), fontSize = 12.sp)
        }
    }
}
