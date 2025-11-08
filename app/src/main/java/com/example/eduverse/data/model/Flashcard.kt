package com.example.eduverse.data.model

data class FlashcardSet(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val teacherId: String = "",
    val teacherName: String = "",
    val subject: String = "",
    val cards: List<Flashcard> = emptyList(),
    val createdAt: Long = 0L,
    val isPublic: Boolean = true
)

data class Flashcard(
    val id: String = "",
    val front: String = "",
    val back: String = "",
    val imageUrl: String = "" // optional image
)
