package com.example.eduverse.data.model

/**
 * Report data model
 *
 * For content moderation and abuse reporting
 */
data class Report(
    val id: String = "",
    val type: ReportType = ReportType.OTHER,
    val entityType: ReportEntityType = ReportEntityType.COURSE,
    val entityId: String = "",
    val entityTitle: String = "",
    val reason: String = "",
    val description: String = "",
    val reportedBy: String = "",
    val reportedByName: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val status: ReportStatus = ReportStatus.OPEN,
    val resolvedBy: String = "",
    val resolvedByName: String = "",
    val resolvedAt: Long = 0L,
    val resolution: String = "",
    val actionTaken: String = ""
)

enum class ReportType {
    SPAM,
    INAPPROPRIATE_CONTENT,
    HARASSMENT,
    COPYRIGHT,
    MISINFORMATION,
    TECHNICAL_ISSUE,
    OTHER;

    companion object {
        fun fromString(value: String): ReportType {
            return try {
                valueOf(value.uppercase())
            } catch (e: Exception) {
                OTHER
            }
        }
    }
}

enum class ReportEntityType {
    COURSE,
    LESSON,
    COMMENT,
    USER,
    ANNOUNCEMENT;

    companion object {
        fun fromString(value: String): ReportEntityType {
            return try {
                valueOf(value.uppercase())
            } catch (e: Exception) {
                COURSE
            }
        }
    }
}

enum class ReportStatus {
    OPEN,
    IN_REVIEW,
    RESOLVED,
    DISMISSED;

    companion object {
        fun fromString(value: String): ReportStatus {
            return try {
                valueOf(value.uppercase())
            } catch (e: Exception) {
                OPEN
            }
        }
    }
}
