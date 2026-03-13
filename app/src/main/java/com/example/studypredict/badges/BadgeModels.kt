package com.example.studypredict.badges

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class BadgeCategory {
    ALL, TRAVAIL, PRESENCE, EXERCICES, SOMMEIL, CONCENTRATION, SUCCES
}

data class Badge(
    val id: String,
    val title: String,
    val description: String,
    val category: BadgeCategory,
    val icon: ImageVector,
    val color: Color,
    val unlocked: Boolean
)

fun demoBadges(): List<Badge> = listOf(
    Badge(
        id = "debutant",
        title = "Débutant",
        description = "Travailler 10h par semaine",
        category = BadgeCategory.TRAVAIL,
        icon = Icons.Outlined.MenuBook,
        color = Color(0xFFFF6A00),
        unlocked = true
    ),
    Badge(
        id = "studieux",
        title = "Studieux",
        description = "Travailler 20h par semaine",
        category = BadgeCategory.TRAVAIL,
        icon = Icons.Outlined.MenuBook,
        color = Color(0xFFFF6A00),
        unlocked = false
    ),
    Badge(
        id = "regulier",
        title = "Régulier",
        description = "75% de présence",
        category = BadgeCategory.PRESENCE,
        icon = Icons.Outlined.Groups,
        color = Color(0xFF10B981),
        unlocked = true
    ),
    Badge(
        id = "assidu",
        title = "Assidu",
        description = "85% de présence",
        category = BadgeCategory.PRESENCE,
        icon = Icons.Outlined.Groups,
        color = Color(0xFF10B981),
        unlocked = false
    ),
    Badge(
        id = "pratiquant",
        title = "Pratiquant",
        description = "10 exercices complétés",
        category = BadgeCategory.EXERCICES,
        icon = Icons.Outlined.TrendingUp,
        color = Color(0xFFFF6A00),
        unlocked = true
    ),
    Badge(
        id = "devoue",
        title = "Dévoué",
        description = "30 exercices complétés",
        category = BadgeCategory.EXERCICES,
        icon = Icons.Outlined.TrendingUp,
        color = Color(0xFFFF6A00),
        unlocked = false
    ),
    Badge(
        id = "repose",
        title = "Reposé",
        description = "6–9h de sommeil",
        category = BadgeCategory.SOMMEIL,
        icon = Icons.Outlined.NightsStay,
        color = Color(0xFF7C5CFF),
        unlocked = true
    ),
    Badge(
        id = "equilibre",
        title = "Équilibré",
        description = "7–8h de sommeil optimal",
        category = BadgeCategory.SOMMEIL,
        icon = Icons.Outlined.NightsStay,
        color = Color(0xFF7C5CFF),
        unlocked = true
    ),
    Badge(
        id = "concentre",
        title = "Concentré",
        description = "Concentration 6+/10",
        category = BadgeCategory.CONCENTRATION,
        icon = Icons.Outlined.Psychology,
        color = Color(0xFFFF2D2D),
        unlocked = true
    ),
    Badge(
        id = "premiere_analyse",
        title = "Première Analyse",
        description = "Compléter votre première analyse",
        category = BadgeCategory.SUCCES,
        icon = Icons.Outlined.AutoAwesome,
        color = Color(0xFFF4B400),
        unlocked = true
    )
)