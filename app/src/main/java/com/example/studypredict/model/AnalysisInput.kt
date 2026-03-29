package com.example.studypredict.model

data class AnalysisInput(
    val hoursPerWeek: Int = 0,
    val attendancePercent: Int = 0,
    val exercisesPerMonth: Int = 0,
    val sleepHours: Int = 0,
    val previousScores: Int = 0,
    val tutoringSessions: Int = 0,
    val physicalActivityHours: Int = 0,
    val extracurricularActivities: Boolean = false,
    val focusLevel: Int = 0
)
