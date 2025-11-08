package com.example.eduverse.data.model

data class StudentProgress(
    val userId: String = "",
    val xp: Int = 0,
    val level: Int = 1,
    val streak: Int = 0,
    val lastActivityDate: Long = 0L, // timestamp
    val completedQuizzes: Int = 0,
    val totalQuizScore: Int = 0,
    val badges: List<Badge> = emptyList(),
    val quizResults: List<QuizResult> = emptyList(),
    val studiedFlashcardSets: List<String> = emptyList() // IDs of studied flashcard sets
)

data class Badge(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val emoji: String = "",
    val unlockedAt: Long = 0L // timestamp
)

data class QuizResult(
    val quizId: String = "",
    val quizTitle: String = "",
    val score: Int = 0,
    val totalQuestions: Int = 0,
    val xpEarned: Int = 0,
    val completedAt: Long = 0L, // timestamp
    val answers: List<QuizAnswer> = emptyList()
)

data class QuizAnswer(
    val questionId: String = "",
    val selectedAnswer: Int = -1,
    val correctAnswer: Int = -1,
    val isCorrect: Boolean = false
)
