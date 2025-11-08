package com.example.eduverse.data.firebase

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Analytics Service
 *
 * Wraps Firebase Analytics to track user actions and events
 */
@Singleton
class AnalyticsService @Inject constructor(
    private val analytics: FirebaseAnalytics
) {
    // Event Names
    companion object {
        // Course Events
        const val EVENT_COURSE_VIEW = "course_view"
        const val EVENT_COURSE_ENROLL = "course_enroll"
        const val EVENT_COURSE_CREATE = "course_create"
        const val EVENT_COURSE_UPDATE = "course_update"
        const val EVENT_COURSE_DELETE = "course_delete"
        const val EVENT_COURSE_PUBLISH = "course_publish"

        // Lesson Events
        const val EVENT_LESSON_START = "lesson_start"
        const val EVENT_LESSON_COMPLETE = "lesson_complete"
        const val EVENT_LESSON_CREATE = "lesson_create"

        // Quiz Events
        const val EVENT_QUIZ_START = "quiz_start"
        const val EVENT_QUIZ_COMPLETE = "quiz_complete"

        // Admin Events
        const val EVENT_ADMIN_ACTION = "admin_action"
        const val EVENT_USER_ROLE_CHANGE = "user_role_change"
        const val EVENT_ANNOUNCEMENT_CREATE = "announcement_create"
        const val EVENT_REPORT_RESOLVE = "report_resolve"

        // Search Events
        const val EVENT_SEARCH = "search"

        // User Events
        const val EVENT_LOGIN = "login"
        const val EVENT_LOGOUT = "logout"
        const val EVENT_SIGNUP = "sign_up"

        // Parameters
        const val PARAM_COURSE_ID = "course_id"
        const val PARAM_COURSE_TITLE = "course_title"
        const val PARAM_LESSON_ID = "lesson_ID"
        const val PARAM_LESSON_TITLE = "lesson_title"
        const val PARAM_USER_ROLE = "user_role"
        const val PARAM_ACTION_TYPE = "action_type"
        const val PARAM_SEARCH_TERM = "search_term"
        const val PARAM_CATEGORY = "category"
        const val PARAM_CONTENT_TYPE = "content_type"
    }

    /**
     * Track course view
     */
    fun logCourseView(courseId: String, courseTitle: String) {
        val bundle = Bundle().apply {
            putString(PARAM_COURSE_ID, courseId)
            putString(PARAM_COURSE_TITLE, courseTitle)
        }
        analytics.logEvent(EVENT_COURSE_VIEW, bundle)
    }

    /**
     * Track course enrollment
     */
    fun logCourseEnroll(courseId: String, courseTitle: String) {
        val bundle = Bundle().apply {
            putString(PARAM_COURSE_ID, courseId)
            putString(PARAM_COURSE_TITLE, courseTitle)
        }
        analytics.logEvent(EVENT_COURSE_ENROLL, bundle)
    }

    /**
     * Track lesson start
     */
    fun logLessonStart(lessonId: String, lessonTitle: String, courseId: String) {
        val bundle = Bundle().apply {
            putString(PARAM_LESSON_ID, lessonId)
            putString(PARAM_LESSON_TITLE, lessonTitle)
            putString(PARAM_COURSE_ID, courseId)
        }
        analytics.logEvent(EVENT_LESSON_START, bundle)
    }

    /**
     * Track lesson completion
     */
    fun logLessonComplete(lessonId: String, lessonTitle: String, courseId: String) {
        val bundle = Bundle().apply {
            putString(PARAM_LESSON_ID, lessonId)
            putString(PARAM_LESSON_TITLE, lessonTitle)
            putString(PARAM_COURSE_ID, courseId)
        }
        analytics.logEvent(EVENT_LESSON_COMPLETE, bundle)
    }

    /**
     * Track quiz completion
     */
    fun logQuizComplete(quizId: String, score: Int, totalQuestions: Int) {
        val bundle = Bundle().apply {
            putString("quiz_id", quizId)
            putInt("score", score)
            putInt("total_questions", totalQuestions)
            putDouble("percentage", (score.toDouble() / totalQuestions) * 100)
        }
        analytics.logEvent(EVENT_QUIZ_COMPLETE, bundle)
    }

    /**
     * Track search
     */
    fun logSearch(searchTerm: String) {
        val bundle = Bundle().apply {
            putString(PARAM_SEARCH_TERM, searchTerm)
        }
        analytics.logEvent(EVENT_SEARCH, bundle)
    }

    /**
     * Track admin action
     */
    fun logAdminAction(actionType: String, details: String = "") {
        val bundle = Bundle().apply {
            putString(PARAM_ACTION_TYPE, actionType)
            putString("details", details)
        }
        analytics.logEvent(EVENT_ADMIN_ACTION, bundle)
    }

    /**
     * Track user role change
     */
    fun logUserRoleChange(userId: String, oldRole: String, newRole: String) {
        val bundle = Bundle().apply {
            putString("user_id", userId)
            putString("old_role", oldRole)
            putString("new_role", newRole)
        }
        analytics.logEvent(EVENT_USER_ROLE_CHANGE, bundle)
    }

    /**
     * Track course creation
     */
    fun logCourseCreate(courseId: String, courseTitle: String, category: String) {
        val bundle = Bundle().apply {
            putString(PARAM_COURSE_ID, courseId)
            putString(PARAM_COURSE_TITLE, courseTitle)
            putString(PARAM_CATEGORY, category)
        }
        analytics.logEvent(EVENT_COURSE_CREATE, bundle)
    }

    /**
     * Track login
     */
    fun logLogin(method: String, userRole: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, method)
            putString(PARAM_USER_ROLE, userRole)
        }
        analytics.logEvent(EVENT_LOGIN, bundle)
    }

    /**
     * Set user properties
     */
    fun setUserProperties(userId: String, role: String) {
        analytics.setUserId(userId)
        analytics.setUserProperty(PARAM_USER_ROLE, role)
    }

    /**
     * Log custom event with parameters
     */
    fun logEvent(eventName: String, params: Map<String, Any> = emptyMap()) {
        val bundle = Bundle().apply {
            params.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Double -> putDouble(key, value)
                    is Boolean -> putBoolean(key, value)
                }
            }
        }
        analytics.logEvent(eventName, bundle)
    }
}
