package com.example.eduverse.data.model

/**
 * Rating data model
 *
 * For course ratings and reviews
 */
data class Rating(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userAvatarUrl: String = "",
    val courseId: String = "",
    val courseTitle: String = "",
    val value: Int = 5, // 1-5 stars
    val comment: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val verified: Boolean = false, // Verified purchase/enrollment
    val helpful: Int = 0, // Number of users who found this helpful
    val reported: Boolean = false
)
