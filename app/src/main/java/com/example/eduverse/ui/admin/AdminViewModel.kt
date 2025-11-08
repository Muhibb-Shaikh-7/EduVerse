package com.example.eduverse.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eduverse.data.firebase.AnalyticsService
import com.example.eduverse.data.model.*
import com.example.eduverse.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Admin ViewModel
 *
 * Manages state and operations for the admin dashboard and all admin features
 */
@HiltViewModel
class AdminViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val lessonRepository: LessonRepository,
    private val userRepository: UserRepository,
    private val announcementRepository: AnnouncementRepository,
    private val reportRepository: ReportRepository,
    private val analyticsService: AnalyticsService
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    // Courses
    private val _courses = MutableStateFlow<List<Course>>(emptyList())
    val courses: StateFlow<List<Course>> = _courses.asStateFlow()

    // Users
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    // Announcements
    private val _announcements = MutableStateFlow<List<Announcement>>(emptyList())
    val announcements: StateFlow<List<Announcement>> = _announcements.asStateFlow()

    // Reports
    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports: StateFlow<List<Report>> = _reports.asStateFlow()

    // Dashboard Stats
    private val _dashboardStats = MutableStateFlow(DashboardStats())
    val dashboardStats: StateFlow<DashboardStats> = _dashboardStats.asStateFlow()

    init {
        loadAllData()
    }

    /**
     * Load all data for admin dashboard
     */
    fun loadAllData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Load courses
            courseRepository.getAllCourses().onSuccess { coursesList ->
                _courses.value = coursesList
            }

            // Load users
            userRepository.getAllUsers().onSuccess { usersList ->
                _users.value = usersList
            }

            // Load announcements
            announcementRepository.getAllAnnouncements().onSuccess { announcementsList ->
                _announcements.value = announcementsList
            }

            // Load reports
            reportRepository.getAllReports().onSuccess { reportsList ->
                _reports.value = reportsList
            }

            // Calculate dashboard stats
            calculateDashboardStats()

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    /**
     * Calculate dashboard statistics
     */
    private fun calculateDashboardStats() {
        val courses = _courses.value
        val users = _users.value
        val reports = _reports.value

        val stats = DashboardStats(
            totalUsers = users.size,
            totalCourses = courses.size,
            publishedCourses = courses.count { it.published },
            draftCourses = courses.count { !it.published },
            totalInstructors = users.count { it.role == UserRole.TEACHER },
            totalStudents = users.count { it.role == UserRole.STUDENT },
            openReports = reports.count { it.status == ReportStatus.OPEN },
            resolvedReports = reports.count { it.status == ReportStatus.RESOLVED },
            totalEnrollments = courses.sumOf { it.enrollmentCount }
        )

        _dashboardStats.value = stats
    }

    // ==================== Course Management ====================

    /**
     * Create a new course
     */
    fun createCourse(course: Course) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            courseRepository.createCourse(course)
                .onSuccess { courseId ->
                    analyticsService.logCourseCreate(courseId, course.title, course.categoryName)
                    analyticsService.logAdminAction(
                        "course_create",
                        "Created course: ${course.title}"
                    )
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Course created successfully"
                        )
                    }
                    loadAllData()
                }
                .onFailure { exception ->
                    _uiState.update { it.copy(isLoading = false, error = exception.message) }
                }
        }
    }

    /**
     * Update a course
     */
    fun updateCourse(courseId: String, updates: Map<String, Any>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            courseRepository.updateCourse(courseId, updates)
                .onSuccess {
                    analyticsService.logAdminAction("course_update", "Updated course: $courseId")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Course updated successfully"
                        )
                    }
                    loadAllData()
                }
                .onFailure { exception ->
                    _uiState.update { it.copy(isLoading = false, error = exception.message) }
                }
        }
    }

    /**
     * Delete a course
     */
    fun deleteCourse(courseId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            courseRepository.deleteCourse(courseId)
                .onSuccess {
                    analyticsService.logAdminAction("course_delete", "Deleted course: $courseId")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Course deleted successfully"
                        )
                    }
                    loadAllData()
                }
                .onFailure { exception ->
                    _uiState.update { it.copy(isLoading = false, error = exception.message) }
                }
        }
    }

    /**
     * Toggle course publish status
     */
    fun toggleCoursePublishStatus(courseId: String, published: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            courseRepository.togglePublishStatus(courseId, published)
                .onSuccess {
                    analyticsService.logAdminAction(
                        "course_publish_toggle",
                        "Set course $courseId published: $published"
                    )
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = if (published) "Course published" else "Course unpublished"
                        )
                    }
                    loadAllData()
                }
                .onFailure { exception ->
                    _uiState.update { it.copy(isLoading = false, error = exception.message) }
                }
        }
    }

    // ==================== Lesson Management ====================

    /**
     * Create a new lesson
     */
    fun createLesson(courseId: String, lesson: Lesson) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            lessonRepository.createLesson(courseId, lesson)
                .onSuccess { lessonId ->
                    analyticsService.logAdminAction(
                        "lesson_create",
                        "Created lesson: ${lesson.title} in course: $courseId"
                    )
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Lesson created successfully"
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update { it.copy(isLoading = false, error = exception.message) }
                }
        }
    }

    /**
     * Update a lesson
     */
    fun updateLesson(courseId: String, lessonId: String, updates: Map<String, Any>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            lessonRepository.updateLesson(courseId, lessonId, updates)
                .onSuccess {
                    analyticsService.logAdminAction("lesson_update", "Updated lesson: $lessonId")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Lesson updated successfully"
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update { it.copy(isLoading = false, error = exception.message) }
                }
        }
    }

    /**
     * Delete a lesson
     */
    fun deleteLesson(courseId: String, lessonId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            lessonRepository.deleteLesson(courseId, lessonId)
                .onSuccess {
                    analyticsService.logAdminAction("lesson_delete", "Deleted lesson: $lessonId")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Lesson deleted successfully"
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update { it.copy(isLoading = false, error = exception.message) }
                }
        }
    }

    /**
     * Reorder lessons
     */
    fun reorderLessons(courseId: String, lessonOrders: Map<String, Int>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            lessonRepository.reorderLessons(courseId, lessonOrders)
                .onSuccess {
                    analyticsService.logAdminAction(
                        "lesson_reorder",
                        "Reordered lessons in course: $courseId"
                    )
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Lessons reordered successfully"
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update { it.copy(isLoading = false, error = exception.message) }
                }
        }
    }

    // ==================== User Management ====================

    /**
     * Update user role
     */
    fun updateUserRole(userId: String, newRole: UserRole, currentUser: User) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Get current user to track old role
            userRepository.getUser(userId)
                .onSuccess { user ->
                    user?.let { user ->
                        userRepository.updateUserRole(userId, newRole)
                            .onSuccess {
                                analyticsService.logUserRoleChange(
                                    userId,
                                    user.role.name,
                                    newRole.name
                                )
                                analyticsService.logAdminAction(
                                    "user_role_change",
                                    "Changed user $userId role from ${user.role} to $newRole"
                                )
                                _uiState.update { state ->
                                    state.copy(
                                        isLoading = false,
                                        successMessage = "User role updated successfully"
                                    )
                                }
                                loadAllData()
                            }
                            .onFailure { exception ->
                                _uiState.update { state ->
                                    state.copy(
                                        isLoading = false,
                                        error = exception.message
                                    )
                                }
                            }
                    }
                }
                .onFailure { exception ->
                    _uiState.update { it.copy(isLoading = false, error = exception.message) }
                }
        }
    }

    /**
     * Search users
     */
    fun searchUsers(query: String) {
        viewModelScope.launch {
            userRepository.searchUsers(query)
                .onSuccess { usersList ->
                    _users.value = usersList
                }
                .onFailure { exception ->
                    _uiState.update { it.copy(error = exception.message) }
                }
        }
    }

    // ==================== Announcement Management ====================

    /**
     * Create announcement
     */
    fun createAnnouncement(announcement: Announcement) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            announcementRepository.createAnnouncement(announcement)
                .onSuccess { announcementId ->
                    analyticsService.logAdminAction(
                        "announcement_create",
                        "Created announcement: ${announcement.title}"
                    )
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Announcement created successfully"
                        )
                    }
                    loadAllData()
                }
                .onFailure { exception ->
                    _uiState.update { it.copy(isLoading = false, error = exception.message) }
                }
        }
    }

    /**
     * Update announcement
     */
    fun updateAnnouncement(id: String, updates: Map<String, Any>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            announcementRepository.updateAnnouncement(id, updates)
                .onSuccess {
                    analyticsService.logAdminAction(
                        "announcement_update",
                        "Updated announcement: $id"
                    )
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Announcement updated successfully"
                        )
                    }
                    loadAllData()
                }
                .onFailure { exception ->
                    _uiState.update { it.copy(isLoading = false, error = exception.message) }
                }
        }
    }

    /**
     * Delete announcement
     */
    fun deleteAnnouncement(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            announcementRepository.deleteAnnouncement(id)
                .onSuccess {
                    analyticsService.logAdminAction(
                        "announcement_delete",
                        "Deleted announcement: $id"
                    )
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Announcement deleted successfully"
                        )
                    }
                    loadAllData()
                }
                .onFailure { exception ->
                    _uiState.update { it.copy(isLoading = false, error = exception.message) }
                }
        }
    }

    // ==================== Report Management ====================

    /**
     * Resolve report
     */
    fun resolveReport(
        reportId: String,
        resolvedBy: String,
        resolvedByName: String,
        resolution: String,
        actionTaken: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            reportRepository.resolveReport(
                reportId,
                resolvedBy,
                resolvedByName,
                resolution,
                actionTaken
            )
                .onSuccess {
                    analyticsService.logAdminAction("report_resolve", "Resolved report: $reportId")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Report resolved successfully"
                        )
                    }
                    loadAllData()
                }
                .onFailure { exception ->
                    _uiState.update { it.copy(isLoading = false, error = exception.message) }
                }
        }
    }

    /**
     * Update report status
     */
    fun updateReportStatus(reportId: String, status: ReportStatus) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val updates = mapOf("status" to status.name)
            reportRepository.updateReport(reportId, updates)
                .onSuccess {
                    analyticsService.logAdminAction(
                        "report_status_update",
                        "Updated report $reportId status to $status"
                    )
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Report status updated"
                        )
                    }
                    loadAllData()
                }
                .onFailure { exception ->
                    _uiState.update { it.copy(isLoading = false, error = exception.message) }
                }
        }
    }

    // ==================== UI Helpers ====================

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Clear success message
     */
    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
}

/**
 * Admin UI State
 */
data class AdminUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

/**
 * Dashboard Statistics
 */
data class DashboardStats(
    val totalUsers: Int = 0,
    val totalCourses: Int = 0,
    val publishedCourses: Int = 0,
    val draftCourses: Int = 0,
    val totalInstructors: Int = 0,
    val totalStudents: Int = 0,
    val openReports: Int = 0,
    val resolvedReports: Int = 0,
    val totalEnrollments: Int = 0
)
