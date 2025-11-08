package com.example.eduverse.data.model

data class Quiz(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val teacherId: String = "",
    val teacherName: String = "",
    val subject: String = "",
    val questions: List<QuizQuestion> = emptyList(),
    val xpReward: Int = 0,
    val timeLimit: Int = 0, // in seconds, 0 means no limit
    val createdAt: Long = 0L,
    val isPublic: Boolean = true
)

data class QuizQuestion(
    val id: String = "",
    val question: String = "",
    val options: List<String> = emptyList(),
    val correctAnswer: Int = 0,
    val explanation: String = "",
    val imageUrl: String = "" // optional image
)
