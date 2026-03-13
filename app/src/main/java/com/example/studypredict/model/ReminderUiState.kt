package com.example.studypredict.model

data class ReminderUiState(
    val title: String = "Révision",
    val message: String = "Pense à réviser 25 minutes",
    val pickedHour: Int = 18,
    val pickedMinute: Int = 0
)