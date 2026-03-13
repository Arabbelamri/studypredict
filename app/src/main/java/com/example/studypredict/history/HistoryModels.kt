package com.example.studypredict.history

data class AnalysisRecord(
    val id: String,
    val scorePercent: Int,
    val grade: String,
    val dateLabel: String, // “11 mars 2026 à 10:07”
    val hoursPerWeek: Int,
    val attendancePercent: Int,
)

fun demoHistoryEmpty(): List<AnalysisRecord> = emptyList()

fun demoHistoryOne(): List<AnalysisRecord> = listOf(
    AnalysisRecord(
        id = "r1",
        scorePercent = 60,
        grade = "D",
        dateLabel = "11 mars 2026 à 10:07",
        hoursPerWeek = 10,
        attendancePercent = 80
    )
)