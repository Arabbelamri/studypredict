package com.example.studypredict.model

data class AnalysisResult(
    val scorePercent: Int,
    val grade: String,
    val hoursPerWeek: Int,
    val attendancePercent: Int,
    val exercisesPerMonth: Int,
    val sleepHours: Int,
    val previousScores: Int,
    val tutoringSessions: Int,
    val physicalActivityHours: Int,
    val extracurricularActivities: Boolean,
    val badges: List<String>,
    val advice: String
)
