package com.example.eduverse.data.model

/**
 * Lesson data model
 *
 * Represents individual lessons within a course
 */
data class Lesson(
    val lessonId: String = "",
    val courseId: String = "",
    val title: String = "",
    val description: String = "",
    val order: Int = 0,
    val contentType: LessonContentType = LessonContentType.ARTICLE,
    val durationSec: Int = 0,
    val storagePath: String = "", // For video/PDF files
    val videoUrl: String = "",
    val body: String = "", // For article content (markdown)
    val quizSpec: QuizSpec? = null,
    val thumbnailUrl: String = "",
    val completed: Boolean = false,
    val locked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class LessonContentType {
    VIDEO,
    ARTICLE,
    QUIZ,
    DOCUMENT,
    INTERACTIVE;

    companion object {
        fun fromString(value: String): LessonContentType {
            return try {
                valueOf(value.uppercase())
            } catch (e: Exception) {
                ARTICLE
            }
        }
    }
}

/**
 * Quiz specification for lesson quizzes
 */
data class QuizSpec(
    val questions: List<QuizQuestion> = emptyList(),
    val passingScore: Int = 70,
    val allowRetry: Boolean = true,
    val showAnswers: Boolean = true
)
