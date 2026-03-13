package com.example.studypredict.model

data class AnalysisInput(
    val hoursPerWeek: Int = 0,
    val attendancePercent: Int = 0,
    val exercisesPerMonth: Int = 0,
    val sleepHours: Int = 0,
    val focusLevel: Int = 0
)