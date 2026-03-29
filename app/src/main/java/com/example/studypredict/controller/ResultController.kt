package com.example.studypredict.controller

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.studypredict.history.AnalysisRecord
import com.example.studypredict.history.HistoryStore
import com.example.studypredict.model.AnalysisInput
import com.example.studypredict.model.AnalysisResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ResultController {

    fun buildResult(input: AnalysisInput): AnalysisResult {
        val score = computeScore(
            hoursPerWeek = input.hoursPerWeek,
            attendancePercent = input.attendancePercent,
            exercisesPerMonth = input.exercisesPerMonth,
            sleepHours = input.sleepHours,
            previousScores = input.previousScores,
            tutoringSessions = input.tutoringSessions,
            physicalActivityHours = input.physicalActivityHours,
            focusLevel = input.focusLevel
        )

        return buildResultFromScore(input, score)
    }

    fun buildResultFromScore(input: AnalysisInput, scorePercent: Int): AnalysisResult {
        val score = scorePercent.coerceIn(0, 100)
        return AnalysisResult(
            scorePercent = score,
            grade = computeGrade(score),
            hoursPerWeek = input.hoursPerWeek,
            attendancePercent = input.attendancePercent,
            exercisesPerMonth = input.exercisesPerMonth,
            sleepHours = input.sleepHours,
            previousScores = input.previousScores,
            tutoringSessions = input.tutoringSessions,
            physicalActivityHours = input.physicalActivityHours,
            extracurricularActivities = input.extracurricularActivities,
            badges = computeBadges(
                score = score,
                attendancePercent = input.attendancePercent,
                sleepHours = input.sleepHours,
                exercisesPerMonth = input.exercisesPerMonth
            ),
            advice = buildAdvice(
                score = score,
                hoursPerWeek = input.hoursPerWeek,
                attendancePercent = input.attendancePercent,
                exercisesPerMonth = input.exercisesPerMonth,
                sleepHours = input.sleepHours,
                previousScores = input.previousScores,
                tutoringSessions = input.tutoringSessions,
                physicalActivityHours = input.physicalActivityHours,
                focusLevel = input.focusLevel
            )
        )
    }

    private fun computeScore(
        hoursPerWeek: Int,
        attendancePercent: Int,
        exercisesPerMonth: Int,
        sleepHours: Int,
        previousScores: Int,
        tutoringSessions: Int,
        physicalActivityHours: Int,
        focusLevel: Int
    ): Int {
        val studyScore = (hoursPerWeek.coerceIn(0, 60) / 60f) * 15f
        val attendanceScore = (attendancePercent.coerceIn(0, 100) / 100f) * 15f
        val exerciseScore = (exercisesPerMonth.coerceIn(0, 120) / 120f) * 12f
        val sleepScore = (sleepHours.coerceIn(0, 12) / 12f) * 12f
        val previousScorePart = (previousScores.coerceIn(0, 100) / 100f) * 18f
        val tutoringPart = (tutoringSessions.coerceIn(0, 60) / 60f) * 10f
        val physicalPart = (physicalActivityHours.coerceIn(0, 30) / 30f) * 8f
        val focusScore = (focusLevel.coerceIn(0, 10) / 10f) * 10f

        return (
            studyScore +
                attendanceScore +
                exerciseScore +
                sleepScore +
                previousScorePart +
                tutoringPart +
                physicalPart +
                focusScore
            ).toInt().coerceIn(0, 100)
    }

    fun computeGrade(score: Int): String {
        return when {
            score >= 80 -> "A"
            score >= 60 -> "B"
            score >= 40 -> "C"
            else -> "D"
        }
    }

    private fun computeBadges(
        score: Int,
        attendancePercent: Int,
        sleepHours: Int,
        exercisesPerMonth: Int
    ): List<String> {
        val badges = mutableListOf<String>()

        if (score >= 70) badges += "Bon potentiel"
        if (attendancePercent >= 85) badges += "Assidu"
        if (sleepHours >= 7) badges += "Equilibre"
        if (exercisesPerMonth >= 12) badges += "Regulier"

        if (badges.isEmpty()) badges += "En progression"
        return badges
    }

    private fun buildAdvice(
        score: Int,
        hoursPerWeek: Int,
        attendancePercent: Int,
        exercisesPerMonth: Int,
        sleepHours: Int,
        previousScores: Int,
        tutoringSessions: Int,
        physicalActivityHours: Int,
        focusLevel: Int
    ): String {
        return when {
            hoursPerWeek < 15 ->
                "Augmente tes heures de travail hebdomadaire pour consolider tes acquis."
            attendancePercent < 75 ->
                "Ameliore ta presence en cours, elle impacte directement ta progression."
            exercisesPerMonth < 8 ->
                "Fais plus d'exercices pratiques chaque mois pour mieux retenir."
            sleepHours < 7 ->
                "Ton sommeil est trop faible. Essaie de viser au moins 7 heures par nuit."
            previousScores < 60 ->
                "Reprends les bases des chapitres precedents pour renforcer ton niveau global."
            tutoringSessions < 2 ->
                "Ajoute des sessions de tutorat pour corriger plus vite tes blocages."
            physicalActivityHours < 2 ->
                "Ajoute un peu d'activite physique chaque semaine pour garder une bonne energie."
            focusLevel < 5 ->
                "Travaille ta concentration avec des sessions courtes et sans distractions."
            score >= 75 ->
                "Tu es sur une bonne dynamique. Reste regulier et continue tes efforts."
            else ->
                "Tu peux progresser rapidement avec plus de regularite et un meilleur equilibre."
        }
    }

    fun buildExportText(result: AnalysisResult): String {
        return """
StudyPredict - Resultats

Score: ${result.scorePercent}%
Grade: ${result.grade}

Details:
- Travail: ${result.hoursPerWeek}h/semaine
- Presence: ${result.attendancePercent}%
- Exercices: ${result.exercisesPerMonth}/mois
- Sommeil: ${result.sleepHours}h/nuit
- Score precedent: ${result.previousScores}%
- Tutorat: ${result.tutoringSessions} session(s)
- Activite physique: ${result.physicalActivityHours}h/semaine
- Activites extrascolaires: ${if (result.extracurricularActivities) "Oui" else "Non"}

Badges: ${result.badges.joinToString(", ")}

Conseil:
${result.advice}
""".trimIndent()
    }

    fun saveToHistory(result: AnalysisResult) {
        val sdf = SimpleDateFormat("d MMM yyyy 'a' HH:mm", Locale.FRENCH)
        val dateLabel = sdf.format(Date())

        HistoryStore.add(
            AnalysisRecord(
                id = UUID.randomUUID().toString(),
                scorePercent = result.scorePercent,
                grade = result.grade,
                dateLabel = dateLabel,
                hoursPerWeek = result.hoursPerWeek,
                attendancePercent = result.attendancePercent,
                exercisesPerMonth = result.exercisesPerMonth,
                sleepHours = result.sleepHours
            )
        )
    }

    fun shareToWhatsApp(context: Context, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            setPackage("com.whatsapp")
        }

        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "WhatsApp n'est pas installe", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareToEmail(context: Context, subject: String, body: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        try {
            context.startActivity(Intent.createChooser(intent, "Envoyer un email"))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "Aucune app email trouvee", Toast.LENGTH_SHORT).show()
        }
    }

    fun openUrl(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "Impossible d'ouvrir le navigateur", Toast.LENGTH_SHORT).show()
        }
    }
}
