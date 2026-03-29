package com.example.studypredict.model

data class NoteItem(
    val id: String,
    val noteType: String,
    val title: String,
    val content: String,
    val createdAt: Long,
    val audioUrl: String?
)
