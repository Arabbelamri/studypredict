package com.example.studypredict.view.tips

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private enum class TipCategory(val label: String) {
    ALL("Tous"),
    ORGA("Organisation"),
    REVISION("Révisions"),
    EXAMS("Examens"),
    HEALTH("Santé"),
    FOCUS("Focus"),
}

private data class Tip(
    val title: String,
    val category: TipCategory,
    val icon: @Composable () -> Unit,
    val summary: String,
    val details: String,
)

@Composable
fun TipsScreen(
    onBack: () -> Unit
) {
    val bg = Color(0xFFF2F6FF)
    val card = Color(0xFFF7FAFF)
    val dark = Color(0xFF0B1220)
    val gray = Color(0xFF6B7280)
    val purple = Color(0xFF6D41FF)

    val primaryGrad = Brush.horizontalGradient(
        listOf(Color(0xFF4B3CFF), Color(0xFFB400FF))
    )

    var selectedCat by remember { mutableStateOf(TipCategory.ALL) }

    val tips = remember {
        listOf(
            Tip(
                title = "Planifie ta semaine (15 min)",
                category = TipCategory.ORGA,
                icon = { Icon(Icons.Outlined.Timer, null) },
                summary = "Dimanche soir : objectifs + créneaux.",
                details = "Note 3 objectifs maximum pour la semaine. Bloque 2 séances de révision courtes de 45 à 60 minutes et 1 séance de rattrapage. L’idée : la régularité vaut mieux qu’un gros rush de dernière minute."
            ),
            Tip(
                title = "Active recall + QCM",
                category = TipCategory.REVISION,
                icon = { Icon(Icons.Outlined.Lightbulb, null) },
                summary = "Teste-toi au lieu de relire.",
                details = "Après un cours, écris 5 questions ou fais un mini QCM. Réponds sans regarder tes notes. Ensuite seulement, vérifie et corrige. C’est une des méthodes les plus efficaces pour mémoriser durablement."
            ),
            Tip(
                title = "Méthode Pomodoro",
                category = TipCategory.FOCUS,
                icon = { Icon(Icons.Outlined.AutoAwesome, null) },
                summary = "25 min focus / 5 min pause.",
                details = "Travaille 25 minutes sans distraction, puis prends 5 minutes de pause. Répète 4 cycles puis fais une pause plus longue de 15 à 20 minutes. C’est simple, concret, et très efficace pour rester concentré."
            ),
            Tip(
                title = "Sommeil = points gratuits",
                category = TipCategory.HEALTH,
                icon = { Icon(Icons.Outlined.FitnessCenter, null) },
                summary = "7h+ et horaires stables.",
                details = "Le cerveau consolide les informations pendant le sommeil. Si tu dors mal, tu perds à la fois en mémorisation, en énergie et en concentration. Garde une heure de coucher stable autant que possible."
            ),
            Tip(
                title = "Stratégie examens",
                category = TipCategory.EXAMS,
                icon = { Icon(Icons.Outlined.School, null) },
                summary = "Entraîne-toi sur des sujets types.",
                details = "Cherche des annales, anciens TD ou exercices types. Fais-les dans des conditions proches du vrai examen avec un temps limité. Ensuite, identifie tes erreurs récurrentes et crée une fiche anti-erreurs."
            ),
            Tip(
                title = "Révise en couches",
                category = TipCategory.REVISION,
                icon = { Icon(Icons.Outlined.Lightbulb, null) },
                summary = "Comprendre, résumer, s’entraîner.",
                details = "Ne passe pas directement aux exercices si tu n’as rien compris, mais ne reste pas non plus bloqué dans la lecture. Travaille en 3 couches : comprendre, résumer avec tes mots, puis t’entraîner."
            ),
            Tip(
                title = "Évite le faux travail",
                category = TipCategory.FOCUS,
                icon = { Icon(Icons.Outlined.AutoAwesome, null) },
                summary = "Être occupé n’est pas toujours progresser.",
                details = "Relire passivement, surligner partout ou regarder 10 vidéos sans pratiquer donne souvent l’impression de travailler sans vrai progrès. Préfère toujours une action qui te force à produire une réponse."
            )
        )
    }

    val filtered = remember(selectedCat, tips) {
        if (selectedCat == TipCategory.ALL) tips else tips.filter { it.category == selectedCat }
    }

    val pagerState = rememberPagerState(pageCount = { 3 })

    Scaffold(
        containerColor = bg
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 18.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Retour", tint = dark)
                    }
                    Text("Retour", color = dark, fontWeight = FontWeight.SemiBold)
                }
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .shadow(10.dp, RoundedCornerShape(16.dp), clip = false)
                            .clip(RoundedCornerShape(16.dp))
                            .background(primaryGrad),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = Color.White)
                    }

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Conseils",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = dark
                        )
                        Text(
                            text = "Des actions simples pour réussir ton année",
                            color = gray
                        )
                    }
                }
            }

            item {
                val pagerShape = RoundedCornerShape(26.dp)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(14.dp, pagerShape, clip = false),
                    shape = pagerShape,
                    color = card
                ) {
                    Column(Modifier.padding(14.dp)) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxWidth()
                        ) { page ->
                            when (page) {
                                0 -> HeroCard(
                                    title = "Conseil du jour",
                                    subtitle = "Fais une mini révision maintenant",
                                    emoji = "✨",
                                    body = "Commence tout de suite par 5 questions sur le dernier cours, sans regarder tes notes.",
                                    accent = purple
                                )

                                1 -> HeroCard(
                                    title = "Routine gagnante",
                                    subtitle = "La régularité fait la différence",
                                    emoji = "🗓️",
                                    body = "Prévois 2 sessions de 45 minutes dans la semaine et 1 créneau de rattrapage.",
                                    accent = Color(0xFF10B981)
                                )

                                else -> HeroCard(
                                    title = "Focus mode",
                                    subtitle = "Réduis les distractions",
                                    emoji = "🎯",
                                    body = "Travaille 25 minutes sans téléphone, puis prends 5 minutes de pause.",
                                    accent = Color(0xFFF59E0B)
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        PagerDots(
                            current = pagerState.currentPage,
                            total = 3,
                            activeColor = Color(0xFF5B55FF)
                        )
                    }
                }
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(end = 8.dp)
                ) {
                    items(TipCategory.entries.size) { i ->
                        val cat = TipCategory.entries[i]
                        FilterChip(
                            selected = selectedCat == cat,
                            onClick = { selectedCat = cat },
                            label = {
                                Text(
                                    cat.label,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF5B55FF),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    filtered.forEach { tip ->
                        ExpandableTipCard(
                            tip = tip,
                            cardColor = Color.White,
                            dark = dark,
                            gray = gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroCard(
    title: String,
    subtitle: String,
    emoji: String,
    body: String,
    accent: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 20.sp)
            Spacer(Modifier.width(8.dp))
            Column {
                Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Text(subtitle, color = Color(0xFF6B7280))
            }
        }

        Spacer(Modifier.height(10.dp))

        Surface(
            color = accent.copy(alpha = 0.10f),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = body,
                modifier = Modifier.padding(14.dp),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun PagerDots(
    current: Int,
    total: Int,
    activeColor: Color
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(total) { idx ->
            val selected = idx == current
            val w by animateFloatAsState(
                targetValue = if (selected) 22f else 10f,
                animationSpec = tween(250, easing = FastOutSlowInEasing),
                label = "dot"
            )

            Box(
                modifier = Modifier
                    .height(10.dp)
                    .width(w.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(if (selected) activeColor else Color(0xFFD1D5DB))
            )
        }
    }
}

@Composable
private fun ExpandableTipCard(
    tip: Tip,
    cardColor: Color,
    dark: Color,
    gray: Color
) {
    var expanded by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(22.dp)

    Surface(
        onClick = { expanded = !expanded },
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, shape, clip = false),
        shape = shape,
        color = cardColor
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.heightIn(min = 56.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFEEF2FF)),
                    contentAlignment = Alignment.Center
                ) {
                    CompositionLocalProvider(LocalContentColor provides Color(0xFF5B55FF)) {
                        tip.icon()
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        text = tip.title,
                        fontWeight = FontWeight.ExtraBold,
                        color = dark
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = tip.summary,
                        color = gray,
                        fontSize = 13.sp
                    )
                }

                AssistChip(
                    onClick = { expanded = !expanded },
                    label = { Text(if (expanded) "Fermer" else "Voir") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color(0xFF5B55FF),
                        labelColor = Color.White
                    )
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(200)) + expandVertically(tween(220)),
                exit = fadeOut(tween(150)) + shrinkVertically(tween(180))
            ) {
                Column {
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        color = Color(0xFFF2F6FF),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = tip.details,
                            modifier = Modifier.padding(14.dp),
                            color = dark
                        )
                    }
                }
            }
        }
    }
}