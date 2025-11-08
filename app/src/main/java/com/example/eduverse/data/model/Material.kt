package com.example.eduverse.data.model

/**
 * Material data model for educational content
 *
 * Represents uploaded materials like PDFs, documents, images, etc.
 */
data class Material(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val fileUrl: String = "",
    val fileType: String = "", // "PDF", "IMAGE", "DOCUMENT"
    val uploadedBy: String = "", // Teacher UID
    val uploadedByName: String = "",
    val uploadedAt: Long = 0L,
    val subject: String = "",
    val tags: List<String> = emptyList(),
    val downloadCount: Int = 0,
    val isPublic: Boolean = true
)
