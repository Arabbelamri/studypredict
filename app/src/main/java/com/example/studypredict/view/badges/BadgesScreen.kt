package com.example.studypredict.view.badges

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.studypredict.badges.Badge
import com.example.studypredict.badges.BadgeCategory
import com.example.studypredict.badges.badgesFromBackend
import com.example.studypredict.badges.demoBadges
import com.example.studypredict.network.ApiResult
import com.example.studypredict.network.BackendApi
import kotlin.math.ceil
import kotlin.math.roundToInt

@Composable
fun BadgesScreen(
    onBack: () -> Unit,
    token: String?,
    onUnauthorized: () -> Unit
) {
    var selectedBadge by remember { mutableStateOf<Badge?>(null) }

    val bg = Color(0xFFF2F6FF)
    val card = Color(0xFFF7FAFF)

    var selected by remember { mutableStateOf(BadgeCategory.ALL) }
    var loading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var allBadges by remember { mutableStateOf<List<Badge>>(emptyList()) }

    LaunchedEffect(token) {
        if (token.isNullOrBlank()) {
            loading = false
            loadError = "Connectez-vous pour voir vos badges."
            allBadges = demoBadges()
            return@LaunchedEffect
        }
        loading = true
        loadError = null
        when (val result = BackendApi.getMyBadges(token)) {
            is ApiResult.Success -> {
                allBadges = badgesFromBackend(result.data)
                loading = false
            }
            is ApiResult.Failure -> {
                loading = false
                if (result.unauthorized) {
                    onUnauthorized()
                } else {
                    loadError = result.message
                    allBadges = demoBadges()
                }
            }
        }
    }
    val unlocked = allBadges.filter { it.unlocked }
    val locked = allBadges.filter { !it.unlocked }

    val total = allBadges.size.coerceAtLeast(1)
    val unlockedCount = unlocked.size
    val lockedCount = locked.size
    val progressTarget = unlockedCount.toFloat() / total.toFloat()

    // animation du ring
    var startAnim by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { startAnim = true }

    val progressAnim by animateFloatAsState(
        targetValue = if (startAnim) progressTarget else 0f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "badgeProgress"
    )
    val percent = (progressAnim * 100).roundToInt()

    val filteredUnlocked = unlocked.filter { selected == BadgeCategory.ALL || it.category == selected }
    val filteredLocked = locked.filter { selected == BadgeCategory.ALL || it.category == selected }

    Scaffold(containerColor = bg) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 18.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Retour")
                    }
                    Text("Retour", fontWeight = FontWeight.SemiBold)
                }
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Mes Badges",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0B1220)
                    )
                }
                Text("Collectionnez tous les badges", color = Color(0xFF6B7280))
            }

            if (loading) {
                item {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }

            loadError?.let { message ->
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFF1F2)
                    ) {
                        Text(
                            text = message,
                            color = Color(0xFF9F1239),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            item {
                val shape = RoundedCornerShape(22.dp)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(14.dp, shape, clip = false),
                    shape = shape,
                    color = card
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text("Progression", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                                Text("$unlockedCount sur $total badges débloqués", color = Color(0xFF6B7280))
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                ProgressRing(
                                    progress = progressAnim,
                                    modifier = Modifier.size(54.dp),
                                    ringColor = Color(0xFF5B55FF)
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "$percent%",
                                        color = Color(0xFF5B55FF),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 28.sp
                                    )
                                    Text("Complété", color = Color(0xFF6B7280))
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { progressAnim },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(99.dp)),
                            color = Color(0xFF111827),
                            trackColor = Color(0xFFD1D5DB)
                        )

                        Spacer(Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatMiniCard(
                                title = unlockedCount.toString(),
                                subtitle = "Débloqués",
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.weight(1f)
                            )
                            StatMiniCard(
                                title = lockedCount.toString(),
                                subtitle = "Verrouillés",
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.weight(1f)
                            )
                            StatMiniCard(
                                title = "$percent%",
                                subtitle = "Progrès",
                                tint = Color(0xFF5B55FF),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(
                        listOf(
                            BadgeCategory.ALL to "Tous",
                            BadgeCategory.TRAVAIL to "Travail",
                            BadgeCategory.PRESENCE to "Présence",
                            BadgeCategory.EXERCICES to "Exercices",
                            BadgeCategory.SOMMEIL to "Sommeil",
                            BadgeCategory.CONCENTRATION to "Concentration",
                            BadgeCategory.SUCCES to "Succès",
                        )
                    ) { (cat, label) ->
                        FilterChip(
                            selected = (selected == cat),
                            onClick = { selected = cat },
                            label = { Text(label, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF5B55FF),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            item {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 4 }
                ) {
                    Column {
                        Text(
                            "✅ Débloqués (${filteredUnlocked.size})",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                        Spacer(Modifier.height(10.dp))
                        BadgesGrid(
                            badges = filteredUnlocked,
                            locked = false,
                            onBadgeClick = { selectedBadge = it }
                        )
                    }
                }
            }

            item {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 4 }
                ) {
                    Column {
                        Text(
                            "Verrouillés (${filteredLocked.size})",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                        Spacer(Modifier.height(10.dp))
                        BadgesGrid(
                            badges = filteredLocked,
                            locked = true,
                            onBadgeClick = { selectedBadge = it }
                        )
                    }
                }
            }
        }

        selectedBadge?.let { badge ->
            BadgeDetailsDialog(
                badge = badge,
                onDismiss = { selectedBadge = null }
            )
        }
    }
}

@Composable
private fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    ringColor: Color = Color(0xFF5B55FF),
    trackColor: Color = Color(0xFFE5E7EB)
) {
    Canvas(modifier = modifier) {
        val stroke = 8.dp.toPx()
        val inset = stroke / 2
        val size = Size(this.size.width - stroke, this.size.height - stroke)

        drawArc(
            color = trackColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = size,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )

        drawArc(
            color = ringColor,
            startAngle = -90f,
            sweepAngle = 360f * progress.coerceIn(0f, 1f),
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = size,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun StatMiniCard(
    title: String,
    subtitle: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(18.dp)
    Surface(
        modifier = modifier.height(92.dp),
        shape = shape,
        color = Color.White
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = tint)
            Text(
                text = subtitle,
                color = Color(0xFF6B7280),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun BadgesGrid(
    badges: List<Badge>,
    locked: Boolean,
    onBadgeClick: (Badge) -> Unit
) {
    val shape = RoundedCornerShape(22.dp)
    val cardHeight = 190.dp
    val vSpacing = 14.dp

    val rows = ceil(badges.size / 2f).toInt().coerceAtLeast(1)
    val gridHeight = (cardHeight * rows) + (vSpacing * (rows - 1))

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(vSpacing),
        modifier = Modifier
            .fillMaxWidth()
            .height(gridHeight),
        userScrollEnabled = false
    ) {
        items(badges) { b ->
            Surface(
                onClick = { onBadgeClick(b) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cardHeight)
                    .shadow(12.dp, shape, clip = false),
                shape = shape,
                color = Color.White
            ) {
                Box(Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .then(if (locked) Modifier.alpha(0.35f).blur(3.dp) else Modifier),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = b.color,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = b.icon,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(b.title, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0B1220))
                        Spacer(Modifier.height(6.dp))
                        Text(b.description, color = Color(0xFF6B7280), fontSize = 13.sp)
                        Spacer(Modifier.height(10.dp))

                        val pillColor = if (!locked) Color(0xFF10B981) else Color(0xFF9CA3AF)
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = pillColor
                        ) {
                            Row(
                                Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (!locked) Icons.Outlined.EmojiEvents else Icons.Outlined.Lock,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = if (!locked) "Débloqué" else "Verrouillé",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    if (locked) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 14.dp)
                                .size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeDetailsDialog(
    badge: Badge,
    onDismiss: () -> Unit
) {
    var showSparkles by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { showSparkles = true }

    Dialog(onDismissRequest = onDismiss) {
        val shape = RoundedCornerShape(22.dp)

        Surface(
            shape = shape,
            color = Color.White,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(Modifier.fillMaxWidth()) {
                if (showSparkles) {
                    SparklesOverlay(modifier = Modifier.fillMaxWidth().height(220.dp))
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = badge.color,
                        modifier = Modifier.size(86.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = badge.icon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = badge.title,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = Color(0xFF0B1220)
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = badge.description,
                        color = Color(0xFF6B7280),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(14.dp))

                    val details = badgeDetailsText(badge)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF7FAFF),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text("Détails", fontWeight = FontWeight.ExtraBold, color = Color(0xFF0B1220))
                            Spacer(Modifier.height(8.dp))
                            Text(details, color = Color(0xFF374151))
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    val statusText = if (badge.unlocked) "Débloqué" else "Verrouillé"
                    val statusColor = if (badge.unlocked) Color(0xFF10B981) else Color(0xFF9CA3AF)

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = statusColor
                    ) {
                        Text(
                            text = statusText,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5B55FF))
                    ) {
                        Text("Fermer", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

private fun badgeDetailsText(b: Badge): String {
    return when (b.category) {
        BadgeCategory.TRAVAIL ->
            "Ce badge récompense ta régularité de travail. Objectif : augmenter progressivement ton volume hebdomadaire. " +
                    "Astuce : planifie 4 sessions de 30–45 minutes, puis augmente."

        BadgeCategory.PRESENCE ->
            "Ce badge mesure ton assiduité. Une présence élevée améliore la compréhension et réduit le stress avant les examens. " +
                    "Astuce : vise une routine simple (arriver 5 min avant, notes claires)."

        BadgeCategory.EXERCICES ->
            "Ce badge valorise la pratique active. Les exercices renforcent la mémoire et la vitesse de résolution. " +
                    "Astuce : fais 10 minutes d’exercices après chaque cours."

        BadgeCategory.SOMMEIL ->
            "Ce badge récompense un sommeil équilibré. Un bon sommeil améliore la concentration et l’apprentissage. " +
                    "Astuce : heure fixe + écran coupé 30 minutes avant."

        BadgeCategory.CONCENTRATION ->
            "Ce badge reflète ta capacité à rester focus. Une meilleure concentration réduit les erreurs et augmente la productivité. " +
                    "Astuce : méthode Pomodoro (25/5) + objectif clair par session."

        BadgeCategory.SUCCES ->
            "Ce badge célèbre une étape importante de ton parcours. Chaque petite victoire compte. " +
                    "Astuce : garde une trace de tes progrès et répète ce qui marche."

        else ->
            "Continue sur cette lancée ! Chaque badge débloqué te rapproche d’un profil d’étude plus solide."
    }
}

@Composable
private fun SparklesOverlay(modifier: Modifier = Modifier) {
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(400),
        label = "sparklesAlpha"
    )

    Box(modifier = modifier) {
        Text("✨", modifier = Modifier.offset(18.dp, 10.dp).alpha(alpha), fontSize = 18.sp)
        Text("✨", modifier = Modifier.offset(240.dp, 24.dp).alpha(alpha * 0.8f), fontSize = 16.sp)
        Text("✨", modifier = Modifier.offset(60.dp, 90.dp).alpha(alpha * 0.7f), fontSize = 14.sp)
        Text("✨", modifier = Modifier.offset(280.dp, 120.dp).alpha(alpha * 0.6f), fontSize = 14.sp)
        Text("✨", modifier = Modifier.offset(120.dp, 160.dp).alpha(alpha * 0.7f), fontSize = 16.sp)
    }
}
