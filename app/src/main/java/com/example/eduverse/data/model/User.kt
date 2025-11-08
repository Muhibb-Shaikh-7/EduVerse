package com.example.eduverse.data.model

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val role: UserRole = UserRole.STUDENT
)

enum class UserRole {
    ADMIN,
    TEACHER,
    STUDENT
}
