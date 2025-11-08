package com.example.eduverse.data.model

/**
 * Category data model
 *
 * For organizing courses into categories
 */
data class Category(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val iconUrl: String = "",
    val emoji: String = "📚",
    val order: Int = 0,
    val courseCount: Int = 0,
    val color: String = "#6366F1", // Hex color
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
