package com.example.eduverse.data.model

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val role: UserRole = UserRole.STUDENT,
    val profileImageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

enum class UserRole {
    ADMIN,
    TEACHER,
    STUDENT
}
