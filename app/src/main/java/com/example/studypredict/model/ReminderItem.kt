package com.example.studypredict.model

data class ReminderItem(
    val id: Int,
    val title: String,
    val message: String,
    val hour: Int,
    val minute: Int,
    val enabled: Boolean = true
)