package com.example.eduverse.data.model

/**
 * Course data model
 *
 * Represents a complete course with metadata, content, and publishing status
 */
data class Course(
    val courseId: String = "",
    val title: String = "",
    val description: String = "",
    val thumbnailUrl: String = "",
    val tags: List<String> = emptyList(),
    val categoryId: String = "",
    val categoryName: String = "",
    val published: Boolean = false,
    val createdBy: String = "", // User UID
    val createdByName: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val price: Double = 0.0,
    val difficulty: CourseDifficulty = CourseDifficulty.BEGINNER,
    val duration: Int = 0, // Total duration in minutes
    val enrollmentCount: Int = 0,
    val ratingCount: Int = 0,
    val avgRating: Double = 0.0,
    val lessonCount: Int = 0,
    val language: String = "en",
    val featured: Boolean = false
)

enum class CourseDifficulty {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
    EXPERT;

    companion object {
        fun fromString(value: String): CourseDifficulty {
            return try {
                valueOf(value.uppercase())
            } catch (e: Exception) {
                BEGINNER
            }
        }
    }
}
