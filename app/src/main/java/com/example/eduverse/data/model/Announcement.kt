package com.example.eduverse.data.model

/**
 * Announcement data model
 *
 * System-wide or course-specific announcements
 */
data class Announcement(
    val id: String = "",
    val scope: AnnouncementScope = AnnouncementScope.GLOBAL,
    val courseId: String = "", // Empty for global announcements
    val courseTitle: String = "",
    val title: String = "",
    val body: String = "",
    val priority: AnnouncementPriority = AnnouncementPriority.NORMAL,
    val scheduledAt: Long = 0L,
    val publishedAt: Long = 0L,
    val createdBy: String = "",
    val createdByName: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val published: Boolean = false,
    val imageUrl: String = "",
    val targetRoles: List<UserRole> = listOf(UserRole.STUDENT, UserRole.TEACHER)
)

enum class AnnouncementScope {
    GLOBAL,
    COURSE;

    companion object {
        fun fromString(value: String): AnnouncementScope {
            return try {
                valueOf(value.uppercase())
            } catch (e: Exception) {
                GLOBAL
            }
        }
    }
}

enum class AnnouncementPriority {
    LOW,
    NORMAL,
    HIGH,
    URGENT;

    companion object {
        fun fromString(value: String): AnnouncementPriority {
            return try {
                valueOf(value.uppercase())
            } catch (e: Exception) {
                NORMAL
            }
        }
    }
}
