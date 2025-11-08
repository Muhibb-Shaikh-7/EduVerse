package com.example.eduverse.data.repository

import android.content.Context
import com.example.eduverse.data.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudentProgressRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Mock storage - replace with Firestore later
    private val progressMap = mutableMapOf<String, StudentProgress>()
    private val _progressFlow = MutableStateFlow<StudentProgress?>(null)

    // Gamification constants
    companion object {
        const val XP_PER_QUIZ = 20
        const val XP_PER_CORRECT_ANSWER = 10
        const val XP_FOR_STREAK = 5

        // Badge thresholds
        const val BADGE_FIRST_QUIZ = 1
        const val BADGE_10_QUIZZES = 10
        const val BADGE_50_QUIZZES = 50
        const val BADGE_100_XP = 100
        const val BADGE_500_XP = 500
        const val BADGE_7_DAY_STREAK = 7
        const val BADGE_30_DAY_STREAK = 30
    }

    suspend fun getStudentProgress(userId: String): Result<StudentProgress> {
        return try {
            val progress = progressMap.getOrPut(userId) {
                StudentProgress(userId = userId)
            }
            _progressFlow.value = progress
            Result.success(progress)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeStudentProgress(userId: String): Flow<StudentProgress?> {
        // Initialize if not exists
        if (!progressMap.containsKey(userId)) {
            progressMap[userId] = StudentProgress(userId = userId)
        }
        _progressFlow.value = progressMap[userId]
        return _progressFlow.asStateFlow()
    }

    suspend fun completeQuiz(
        userId: String,
        quiz: com.example.eduverse.data.model.Quiz,
        score: Int,
        totalQuestions: Int,
        answers: List<QuizAnswer>
    ): Result<StudentProgress> {
        return try {
            val currentProgress = progressMap.getOrPut(userId) {
                StudentProgress(userId = userId)
            }

            // Calculate XP earned
            val correctAnswers = answers.count { it.isCorrect }
            val xpEarned = XP_PER_QUIZ + (correctAnswers * XP_PER_CORRECT_ANSWER)

            // Check and update streak
            val currentTime = System.currentTimeMillis()
            val oneDayMillis = 24 * 60 * 60 * 1000L
            val timeSinceLastActivity = currentTime - currentProgress.lastActivityDate

            val newStreak = when {
                currentProgress.lastActivityDate == 0L -> 1 // First activity
                timeSinceLastActivity < oneDayMillis * 2 -> currentProgress.streak + 1 // Maintain streak
                else -> 1 // Reset streak
            }

            val streakBonus = if (newStreak > currentProgress.streak) XP_FOR_STREAK else 0
            val totalXp = currentProgress.xp + xpEarned + streakBonus

            // Calculate level (100 XP per level)
            val newLevel = (totalXp / 100) + 1

            // Create quiz result
            val quizResult = QuizResult(
                quizId = quiz.id,
                quizTitle = quiz.title,
                score = score,
                totalQuestions = totalQuestions,
                xpEarned = xpEarned + streakBonus,
                completedAt = currentTime,
                answers = answers
            )

            // Update progress
            val updatedProgress = currentProgress.copy(
                xp = totalXp,
                level = newLevel,
                streak = newStreak,
                lastActivityDate = currentTime,
                completedQuizzes = currentProgress.completedQuizzes + 1,
                totalQuizScore = currentProgress.totalQuizScore + score,
                quizResults = currentProgress.quizResults + quizResult,
                badges = checkAndAwardBadges(
                    currentProgress.copy(
                        xp = totalXp,
                        level = newLevel,
                        streak = newStreak,
                        completedQuizzes = currentProgress.completedQuizzes + 1
                    )
                )
            )

            progressMap[userId] = updatedProgress
            _progressFlow.value = updatedProgress

            Result.success(updatedProgress)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun studyFlashcardSet(userId: String, flashcardSetId: String): Result<StudentProgress> {
        return try {
            val currentProgress = progressMap.getOrPut(userId) {
                StudentProgress(userId = userId)
            }

            // Add flashcard set to studied list if not already there
            val updatedSets = if (flashcardSetId !in currentProgress.studiedFlashcardSets) {
                currentProgress.studiedFlashcardSets + flashcardSetId
            } else {
                currentProgress.studiedFlashcardSets
            }

            val updatedProgress = currentProgress.copy(
                studiedFlashcardSets = updatedSets
            )

            progressMap[userId] = updatedProgress
            _progressFlow.value = updatedProgress

            Result.success(updatedProgress)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun checkAndAwardBadges(progress: StudentProgress): List<Badge> {
        val badges = progress.badges.toMutableList()
        val currentTime = System.currentTimeMillis()

        // Helper to check if badge already exists
        fun hasBadge(badgeId: String) = badges.any { it.id == badgeId }

        // First Quiz Badge
        if (progress.completedQuizzes >= BADGE_FIRST_QUIZ && !hasBadge("first_quiz")) {
            badges.add(
                Badge(
                    id = "first_quiz",
                    title = "First Steps",
                    description = "Complete your first quiz",
                    emoji = "🏅",
                    unlockedAt = currentTime
                )
            )
        }

        // 10 Quizzes Badge
        if (progress.completedQuizzes >= BADGE_10_QUIZZES && !hasBadge("quiz_10")) {
            badges.add(
                Badge(
                    id = "quiz_10",
                    title = "Quiz Novice",
                    description = "Complete 10 quizzes",
                    emoji = "🎯",
                    unlockedAt = currentTime
                )
            )
        }

        // 50 Quizzes Badge
        if (progress.completedQuizzes >= BADGE_50_QUIZZES && !hasBadge("quiz_50")) {
            badges.add(
                Badge(
                    id = "quiz_50",
                    title = "Quiz Expert",
                    description = "Complete 50 quizzes",
                    emoji = "🏆",
                    unlockedAt = currentTime
                )
            )
        }

        // 100 XP Badge
        if (progress.xp >= BADGE_100_XP && !hasBadge("xp_100")) {
            badges.add(
                Badge(
                    id = "xp_100",
                    title = "Rising Star",
                    description = "Earn 100 XP",
                    emoji = "⭐",
                    unlockedAt = currentTime
                )
            )
        }

        // 500 XP Badge
        if (progress.xp >= BADGE_500_XP && !hasBadge("xp_500")) {
            badges.add(
                Badge(
                    id = "xp_500",
                    title = "Super Star",
                    description = "Earn 500 XP",
                    emoji = "🌟",
                    unlockedAt = currentTime
                )
            )
        }

        // 7-Day Streak Badge
        if (progress.streak >= BADGE_7_DAY_STREAK && !hasBadge("streak_7")) {
            badges.add(
                Badge(
                    id = "streak_7",
                    title = "Week Warrior",
                    description = "Maintain a 7-day streak",
                    emoji = "🔥",
                    unlockedAt = currentTime
                )
            )
        }

        // 30-Day Streak Badge
        if (progress.streak >= BADGE_30_DAY_STREAK && !hasBadge("streak_30")) {
            badges.add(
                Badge(
                    id = "streak_30",
                    title = "Dedication Master",
                    description = "Maintain a 30-day streak",
                    emoji = "💪",
                    unlockedAt = currentTime
                )
            )
        }

        return badges
    }

    suspend fun resetProgress(userId: String): Result<Unit> {
        return try {
            progressMap[userId] = StudentProgress(userId = userId)
            _progressFlow.value = progressMap[userId]
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // TODO: Replace with Firestore implementation
    /*
    private val firestore = FirebaseFirestore.getInstance()
    
    suspend fun getStudentProgress(userId: String): Result<StudentProgress> {
        return try {
            val document = firestore.collection("student_progress")
                .document(userId)
                .get()
                .await()
            
            val progress = document.toObject(StudentProgress::class.java)
                ?: StudentProgress(userId = userId)
            
            Result.success(progress)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateStudentProgress(progress: StudentProgress): Result<Unit> {
        return try {
            firestore.collection("student_progress")
                .document(progress.userId)
                .set(progress)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    */
}
