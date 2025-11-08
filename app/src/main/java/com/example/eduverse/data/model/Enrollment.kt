package com.example.eduverse.data.model

/**
 * Enrollment data model
 *
 * Tracks student enrollment and progress in courses
 */
data class Enrollment(
    val enrollmentId: String = "",
    val userId: String = "",
    val userName: String = "",
    val courseId: String = "",
    val courseTitle: String = "",
    val progressPct: Int = 0,
    val completedLessons: List<String> = emptyList(), // Lesson IDs
    val lastActivityAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long = 0L,
    val status: EnrollmentStatus = EnrollmentStatus.ACTIVE,
    val certificateUrl: String = ""
)

enum class EnrollmentStatus {
    ACTIVE,
    COMPLETED,
    DROPPED,
    SUSPENDED;

    companion object {
        fun fromString(value: String): EnrollmentStatus {
            return try {
                valueOf(value.uppercase())
            } catch (e: Exception) {
                ACTIVE
            }
        }
    }
}
