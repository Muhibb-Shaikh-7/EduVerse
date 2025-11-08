package com.example.eduverse.data.firebase

import com.example.eduverse.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore Database Service
 *
 * Handles all database operations for:
 * - Materials (PDFs, documents)
 * - Quizzes and results
 * - Flashcards
 * - Student progress
 * - User data
 */
@Singleton
class FirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val USERS_COLLECTION = "users"
        private const val MATERIALS_COLLECTION = "materials"
        private const val QUIZZES_COLLECTION = "quizzes"
        private const val FLASHCARDS_COLLECTION = "flashcards"
        private const val PROGRESS_COLLECTION = "student_progress"
        private const val QUIZ_RESULTS_COLLECTION = "quiz_results"
    }

    // ==================== User Operations ====================

    /**
     * Get user data by UID
     */
    suspend fun getUser(uid: String): Result<User?> {
        return try {
            val doc = firestore.collection(USERS_COLLECTION)
                .document(uid)
                .get()
                .await()

            if (!doc.exists()) {
                return Result.success(null)
            }

            val user = User(
                uid = doc.id,
                email = doc.getString("email") ?: "",
                displayName = doc.getString("displayName") ?: "",
                role = UserRole.valueOf(doc.getString("role") ?: "STUDENT")
            )

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update user data
     */
    suspend fun updateUser(uid: String, data: Map<String, Any>): Result<Unit> {
        return try {
            firestore.collection(USERS_COLLECTION)
                .document(uid)
                .update(data + mapOf("updatedAt" to System.currentTimeMillis()))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== Material Operations ====================

    /**
     * Upload material metadata to Firestore
     */
    suspend fun saveMaterial(material: Material): Result<String> {
        return try {
            val docRef = firestore.collection(MATERIALS_COLLECTION)
                .add(material.toMap())
                .await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get all materials for a teacher
     */
    suspend fun getMaterialsByTeacher(teacherId: String): Result<List<Material>> {
        return try {
            val snapshot = firestore.collection(MATERIALS_COLLECTION)
                .whereEqualTo("uploadedBy", teacherId)
                .orderBy("uploadedAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val materials = snapshot.documents.mapNotNull { doc ->
                doc.toMaterial()
            }

            Result.success(materials)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get all materials (for admin/student)
     */
    suspend fun getAllMaterials(): Result<List<Material>> {
        return try {
            val snapshot = firestore.collection(MATERIALS_COLLECTION)
                .orderBy("uploadedAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val materials = snapshot.documents.mapNotNull { doc ->
                doc.toMaterial()
            }

            Result.success(materials)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete material
     */
    suspend fun deleteMaterial(materialId: String): Result<Unit> {
        return try {
            firestore.collection(MATERIALS_COLLECTION)
                .document(materialId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== Quiz Operations ====================

    /**
     * Save quiz to Firestore
     */
    suspend fun saveQuiz(quiz: Quiz): Result<String> {
        return try {
            val docRef = firestore.collection(QUIZZES_COLLECTION)
                .add(quiz.toMap())
                .await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get all quizzes
     */
    suspend fun getAllQuizzes(): Result<List<Quiz>> {
        return try {
            val snapshot = firestore.collection(QUIZZES_COLLECTION)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val quizzes = snapshot.documents.mapNotNull { doc ->
                doc.toQuiz()
            }

            Result.success(quizzes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get quiz by ID
     */
    suspend fun getQuiz(quizId: String): Result<Quiz?> {
        return try {
            val doc = firestore.collection(QUIZZES_COLLECTION)
                .document(quizId)
                .get()
                .await()

            val quiz = doc.toQuiz()
            Result.success(quiz)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Save quiz result
     */
    suspend fun saveQuizResult(result: QuizResult): Result<String> {
        return try {
            val docRef = firestore.collection(QUIZ_RESULTS_COLLECTION)
                .add(result.toMap())
                .await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== Flashcard Operations ====================

    /**
     * Save flashcard set to Firestore
     */
    suspend fun saveFlashcardSet(flashcardSet: FlashcardSet): Result<String> {
        return try {
            val docRef = firestore.collection(FLASHCARDS_COLLECTION)
                .add(flashcardSet.toMap())
                .await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get all flashcard sets
     */
    suspend fun getAllFlashcardSets(): Result<List<FlashcardSet>> {
        return try {
            val snapshot = firestore.collection(FLASHCARDS_COLLECTION)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val flashcardSets = snapshot.documents.mapNotNull { doc ->
                doc.toFlashcardSet()
            }

            Result.success(flashcardSets)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get flashcard sets by subject
     */
    suspend fun getFlashcardSetsBySubject(subject: String): Result<List<FlashcardSet>> {
        return try {
            val snapshot = firestore.collection(FLASHCARDS_COLLECTION)
                .whereEqualTo("subject", subject)
                .get()
                .await()

            val flashcardSets = snapshot.documents.mapNotNull { doc ->
                doc.toFlashcardSet()
            }

            Result.success(flashcardSets)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== Student Progress Operations ====================

    /**
     * Get student progress
     */
    suspend fun getStudentProgress(studentId: String): Result<StudentProgress?> {
        return try {
            val doc = firestore.collection(PROGRESS_COLLECTION)
                .document(studentId)
                .get()
                .await()

            val progress = doc.toStudentProgress()
            Result.success(progress)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update student progress
     */
    suspend fun updateStudentProgress(
        studentId: String,
        progress: StudentProgress
    ): Result<Unit> {
        return try {
            firestore.collection(PROGRESS_COLLECTION)
                .document(studentId)
                .set(progress.toMap())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Add XP to student
     */
    suspend fun addXP(studentId: String, xpAmount: Int): Result<StudentProgress?> {
        return try {
            val progressResult = getStudentProgress(studentId)
            val currentProgress = progressResult.getOrNull() ?: StudentProgress(
                userId = studentId,
                xp = 0,
                level = 1,
                streak = 0,
                badges = emptyList(),
                completedQuizzes = 0,
                quizResults = emptyList()
            )

            val newXP = currentProgress.xp + xpAmount
            val newLevel = (newXP / 100) + 1
            val updatedProgress = currentProgress.copy(
                xp = newXP,
                level = newLevel
            )

            updateStudentProgress(studentId, updatedProgress)
            Result.success(updatedProgress)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== Real-time Updates ====================

    /**
     * Observe materials in real-time
     */
    fun observeMaterials(): Flow<List<Material>> = callbackFlow {
        val listener = firestore.collection(MATERIALS_COLLECTION)
            .orderBy("uploadedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val materials = snapshot?.documents?.mapNotNull { doc ->
                    doc.toMaterial()
                } ?: emptyList()

                trySend(materials)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Observe student progress in real-time
     */
    fun observeStudentProgress(studentId: String): Flow<StudentProgress?> = callbackFlow {
        val listener = firestore.collection(PROGRESS_COLLECTION)
            .document(studentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val progress = snapshot?.toStudentProgress()
                trySend(progress)
            }

        awaitClose { listener.remove() }
    }
}

// ==================== Extension Functions ====================

/**
 * Extension function to convert Firestore document to Material
 */
private fun com.google.firebase.firestore.DocumentSnapshot.toMaterial(): Material? {
    return try {
        Material(
            id = id,
            title = getString("title") ?: "",
            description = getString("description") ?: "",
            fileUrl = getString("fileUrl") ?: "",
            fileType = getString("fileType") ?: "",
            uploadedBy = getString("uploadedBy") ?: "",
            uploadedByName = getString("uploadedByName") ?: "",
            uploadedAt = getLong("uploadedAt") ?: 0L,
            subject = getString("subject") ?: "",
            tags = (get("tags") as? List<String>) ?: emptyList(),
            downloadCount = (getLong("downloadCount") ?: 0).toInt(),
            isPublic = getBoolean("isPublic") ?: true
        )
    } catch (e: Exception) {
        null
    }
}

/**
 * Extension function to convert Firestore document to Quiz
 */
private fun com.google.firebase.firestore.DocumentSnapshot.toQuiz(): Quiz? {
    return try {
        val questionsMap = get("questions") as? List<Map<String, Any>> ?: emptyList()
        val questions = questionsMap.mapNotNull { questionMap ->
            try {
                QuizQuestion(
                    id = questionMap["id"] as? String ?: "",
                    question = questionMap["question"] as? String ?: "",
                    options = (questionMap["options"] as? List<String>) ?: emptyList(),
                    correctAnswer = (questionMap["correctAnswer"] as? Long)?.toInt() ?: 0,
                    explanation = questionMap["explanation"] as? String ?: "",
                    imageUrl = questionMap["imageUrl"] as? String ?: ""
                )
            } catch (e: Exception) {
                null
            }
        }

        Quiz(
            id = id,
            title = getString("title") ?: "",
            description = getString("description") ?: "",
            teacherId = getString("teacherId") ?: "",
            teacherName = getString("teacherName") ?: "",
            subject = getString("subject") ?: "",
            questions = questions,
            xpReward = (getLong("xpReward") ?: 0).toInt(),
            timeLimit = (getLong("timeLimit") ?: 0).toInt(),
            createdAt = getLong("createdAt") ?: 0L,
            isPublic = getBoolean("isPublic") ?: true
        )
    } catch (e: Exception) {
        null
    }
}


/**
 * Extension function to convert Firestore document to FlashcardSet
 */
private fun com.google.firebase.firestore.DocumentSnapshot.toFlashcardSet(): FlashcardSet? {
    return try {
        val cardsMap = get("cards") as? List<Map<String, Any>> ?: emptyList()
        val cards = cardsMap.mapNotNull { cardMap ->
            try {
                Flashcard(
                    id = cardMap["id"] as? String ?: "",
                    front = cardMap["front"] as? String ?: "",
                    back = cardMap["back"] as? String ?: "",
                    imageUrl = cardMap["imageUrl"] as? String ?: ""
                )
            } catch (e: Exception) {
                null
            }
        }

        FlashcardSet(
            id = id,
            title = getString("title") ?: "",
            description = getString("description") ?: "",
            teacherId = getString("teacherId") ?: "",
            teacherName = getString("teacherName") ?: "",
            subject = getString("subject") ?: "",
            cards = cards,
            createdAt = getLong("createdAt") ?: 0L,
            isPublic = getBoolean("isPublic") ?: true
        )
    } catch (e: Exception) {
        null
    }
}

/**
 * Extension function to convert Firestore document to StudentProgress
 */
private fun com.google.firebase.firestore.DocumentSnapshot.toStudentProgress(): StudentProgress? {
    return try {
        val badgesMap = get("badges") as? List<Map<String, Any>> ?: emptyList()
        val badges = badgesMap.mapNotNull { badgeMap ->
            try {
                Badge(
                    id = badgeMap["id"] as? String ?: "",
                    title = badgeMap["title"] as? String ?: "",
                    description = badgeMap["description"] as? String ?: "",
                    emoji = badgeMap["emoji"] as? String ?: "",
                    unlockedAt = (badgeMap["unlockedAt"] as? Long) ?: 0L
                )
            } catch (e: Exception) {
                null
            }
        }

        val quizResultsMap = get("quizResults") as? List<Map<String, Any>> ?: emptyList()
        val quizResults = quizResultsMap.mapNotNull { resultMap ->
            try {
                val answersMap = resultMap["answers"] as? List<Map<String, Any>> ?: emptyList()
                val answers = answersMap.mapNotNull { answerMap ->
                    try {
                        QuizAnswer(
                            questionId = answerMap["questionId"] as? String ?: "",
                            selectedAnswer = (answerMap["selectedAnswer"] as? Long)?.toInt() ?: -1,
                            correctAnswer = (answerMap["correctAnswer"] as? Long)?.toInt() ?: -1,
                            isCorrect = answerMap["isCorrect"] as? Boolean ?: false
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                QuizResult(
                    quizId = resultMap["quizId"] as? String ?: "",
                    quizTitle = resultMap["quizTitle"] as? String ?: "",
                    score = (resultMap["score"] as? Long)?.toInt() ?: 0,
                    totalQuestions = (resultMap["totalQuestions"] as? Long)?.toInt() ?: 0,
                    xpEarned = (resultMap["xpEarned"] as? Long)?.toInt() ?: 0,
                    completedAt = (resultMap["completedAt"] as? Long) ?: 0L,
                    answers = answers
                )
            } catch (e: Exception) {
                null
            }
        }

        val studiedSets = (get("studiedFlashcardSets") as? List<String>) ?: emptyList()

        StudentProgress(
            userId = id,
            xp = (getLong("xp") ?: 0).toInt(),
            level = (getLong("level") ?: 1).toInt(),
            streak = (getLong("streak") ?: 0).toInt(),
            lastActivityDate = getLong("lastActivityDate") ?: 0L,
            completedQuizzes = (getLong("completedQuizzes") ?: 0).toInt(),
            totalQuizScore = (getLong("totalQuizScore") ?: 0).toInt(),
            badges = badges,
            quizResults = quizResults,
            studiedFlashcardSets = studiedSets
        )
    } catch (e: Exception) {
        null
    }
}

// ==================== Data Class Extensions ====================

private fun Material.toMap(): Map<String, Any> = mapOf(
    "title" to title,
    "description" to description,
    "fileUrl" to fileUrl,
    "fileType" to fileType,
    "uploadedBy" to uploadedBy,
    "uploadedByName" to uploadedByName,
    "uploadedAt" to uploadedAt,
    "subject" to subject,
    "tags" to tags,
    "downloadCount" to downloadCount,
    "isPublic" to isPublic
)

private fun Quiz.toMap(): Map<String, Any> = mapOf(
    "title" to title,
    "description" to description,
    "teacherId" to teacherId,
    "teacherName" to teacherName,
    "subject" to subject,
    "questions" to questions.map { question ->
        mapOf(
            "id" to question.id,
            "question" to question.question,
            "options" to question.options,
            "correctAnswer" to question.correctAnswer,
            "explanation" to question.explanation,
            "imageUrl" to question.imageUrl
        )
    },
    "xpReward" to xpReward,
    "timeLimit" to timeLimit,
    "createdAt" to createdAt,
    "isPublic" to isPublic
)

private fun FlashcardSet.toMap(): Map<String, Any> = mapOf(
    "title" to title,
    "description" to description,
    "teacherId" to teacherId,
    "teacherName" to teacherName,
    "subject" to subject,
    "cards" to cards.map { card ->
        mapOf(
            "id" to card.id,
            "front" to card.front,
            "back" to card.back,
            "imageUrl" to card.imageUrl
        )
    },
    "createdAt" to createdAt,
    "isPublic" to isPublic
)

private fun QuizResult.toMap(): Map<String, Any> = mapOf(
    "quizId" to quizId,
    "quizTitle" to quizTitle,
    "score" to score,
    "totalQuestions" to totalQuestions,
    "xpEarned" to xpEarned,
    "completedAt" to completedAt,
    "answers" to answers.map { answer ->
        mapOf(
            "questionId" to answer.questionId,
            "selectedAnswer" to answer.selectedAnswer,
            "correctAnswer" to answer.correctAnswer,
            "isCorrect" to answer.isCorrect
        )
    }
)

private fun StudentProgress.toMap(): Map<String, Any> = mapOf(
    "xp" to xp,
    "level" to level,
    "streak" to streak,
    "lastActivityDate" to lastActivityDate,
    "completedQuizzes" to completedQuizzes,
    "totalQuizScore" to totalQuizScore,
    "badges" to badges.map { badge ->
        mapOf(
            "id" to badge.id,
            "title" to badge.title,
            "description" to badge.description,
            "emoji" to badge.emoji,
            "unlockedAt" to badge.unlockedAt
        )
    },
    "quizResults" to quizResults.map { result ->
        mapOf(
            "quizId" to result.quizId,
            "quizTitle" to result.quizTitle,
            "score" to result.score,
            "totalQuestions" to result.totalQuestions,
            "xpEarned" to result.xpEarned,
            "completedAt" to result.completedAt,
            "answers" to result.answers.map { answer ->
                mapOf(
                    "questionId" to answer.questionId,
                    "selectedAnswer" to answer.selectedAnswer,
                    "correctAnswer" to answer.correctAnswer,
                    "isCorrect" to answer.isCorrect
                )
            }
        )
    },
    "studiedFlashcardSets" to studiedFlashcardSets
)
