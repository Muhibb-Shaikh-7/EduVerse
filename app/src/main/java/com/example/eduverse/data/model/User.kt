package com.example.eduverse.data.model

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val role: UserRole = UserRole.STUDENT,
    val avatarUrl: String = "",
    val bio: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = 0L,
    val isActive: Boolean = true,
    val enrolledCourses: List<String> = emptyList(), // Course IDs
    val createdCourses: List<String> = emptyList() // Course IDs (for instructors)
)

enum class UserRole {
    ADMIN,
    TEACHER,
    STUDENT;

    companion object {
        fun fromString(value: String): UserRole {
            return try {
                valueOf(value.uppercase())
            } catch (e: Exception) {
                STUDENT
            }
        }
    }
}
