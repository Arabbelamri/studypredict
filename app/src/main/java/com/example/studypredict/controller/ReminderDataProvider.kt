package com.example.studypredict.controller

import com.example.studypredict.model.ReminderItem

object ReminderDataProvider {
    fun defaultReminders(): List<ReminderItem> {
        return listOf(
            ReminderItem(
                id = 1001,
                title = "Révision",
                message = "Active recall : 5 questions",
                hour = 18,
                minute = 30,
                enabled = true
            ),
            ReminderItem(
                id = 1002,
                title = "Sommeil",
                message = "Prépare-toi à dormir tôt",
                hour = 23,
                minute = 0,
                enabled = false
            )
        )
    }
}