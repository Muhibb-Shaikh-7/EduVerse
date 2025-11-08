package com.example.eduverse.data.repository

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
 * Lesson Repository
 *
 * Handles all lesson-related Firestore operations
 */
@Singleton
class LessonRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val COURSES_COLLECTION = "courses"
        private const val LESSONS_SUBCOLLECTION = "lessons"
    }

    /**
     * Create a new lesson
     */
    suspend fun createLesson(courseId: String, lesson: Lesson): Result<String> {
        return try {
            val docRef = firestore.collection(COURSES_COLLECTION)
                .document(courseId)
                .collection(LESSONS_SUBCOLLECTION)
                .document()

            val lessonWithId = lesson.copy(lessonId = docRef.id, courseId = courseId)
            docRef.set(lessonWithId.toMap()).await()

            // Update course lesson count
            updateCourseLessonCount(courseId)

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update a lesson
     */
    suspend fun updateLesson(
        courseId: String,
        lessonId: String,
        updates: Map<String, Any>
    ): Result<Unit> {
        return try {
            val updatesWithTimestamp = updates + mapOf("updatedAt" to System.currentTimeMillis())
            firestore.collection(COURSES_COLLECTION)
                .document(courseId)
                .collection(LESSONS_SUBCOLLECTION)
                .document(lessonId)
                .update(updatesWithTimestamp)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a lesson
     */
    suspend fun deleteLesson(courseId: String, lessonId: String): Result<Unit> {
        return try {
            firestore.collection(COURSES_COLLECTION)
                .document(courseId)
                .collection(LESSONS_SUBCOLLECTION)
                .document(lessonId)
                .delete()
                .await()

            // Update course lesson count
            updateCourseLessonCount(courseId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get lesson by ID
     */
    suspend fun getLesson(courseId: String, lessonId: String): Result<Lesson?> {
        return try {
            val doc = firestore.collection(COURSES_COLLECTION)
                .document(courseId)
                .collection(LESSONS_SUBCOLLECTION)
                .document(lessonId)
                .get()
                .await()

            val lesson = doc.toLesson()
            Result.success(lesson)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get all lessons for a course
     */
    suspend fun getLessons(courseId: String): Result<List<Lesson>> {
        return try {
            val snapshot = firestore.collection(COURSES_COLLECTION)
                .document(courseId)
                .collection(LESSONS_SUBCOLLECTION)
                .orderBy("order", Query.Direction.ASCENDING)
                .get()
                .await()

            val lessons = snapshot.documents.mapNotNull { it.toLesson() }
            Result.success(lessons)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Reorder lessons (batch update)
     */
    suspend fun reorderLessons(courseId: String, lessonOrders: Map<String, Int>): Result<Unit> {
        return try {
            val batch = firestore.batch()

            lessonOrders.forEach { (lessonId, order) ->
                val lessonRef = firestore.collection(COURSES_COLLECTION)
                    .document(courseId)
                    .collection(LESSONS_SUBCOLLECTION)
                    .document(lessonId)

                batch.update(lessonRef, "order", order)
                batch.update(lessonRef, "updatedAt", System.currentTimeMillis())
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Observe lessons in real-time
     */
    fun observeLessons(courseId: String): Flow<List<Lesson>> = callbackFlow {
        val listener = firestore.collection(COURSES_COLLECTION)
            .document(courseId)
            .collection(LESSONS_SUBCOLLECTION)
            .orderBy("order", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val lessons = snapshot?.documents?.mapNotNull { it.toLesson() } ?: emptyList()
                trySend(lessons)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Update course lesson count
     */
    private suspend fun updateCourseLessonCount(courseId: String) {
        try {
            val lessonsSnapshot = firestore.collection(COURSES_COLLECTION)
                .document(courseId)
                .collection(LESSONS_SUBCOLLECTION)
                .get()
                .await()

            val count = lessonsSnapshot.size()

            firestore.collection(COURSES_COLLECTION)
                .document(courseId)
                .update("lessonCount", count)
                .await()
        } catch (e: Exception) {
            // Silently fail - this is a non-critical operation
        }
    }
}

/**
 * Extension function to convert Firestore document to Lesson
 */
private fun com.google.firebase.firestore.DocumentSnapshot.toLesson(): Lesson? {
    return try {
        val quizSpecMap = get("quizSpec") as? Map<String, Any>
        val quizSpec = quizSpecMap?.let {
            val questionsMap = it["questions"] as? List<Map<String, Any>> ?: emptyList()
            val questions = questionsMap.mapNotNull { qMap ->
                try {
                    QuizQuestion(
                        id = qMap["id"] as? String ?: "",
                        question = qMap["question"] as? String ?: "",
                        options = (qMap["options"] as? List<String>) ?: emptyList(),
                        correctAnswer = (qMap["correctAnswer"] as? Long)?.toInt() ?: 0,
                        explanation = qMap["explanation"] as? String ?: "",
                        imageUrl = qMap["imageUrl"] as? String ?: ""
                    )
                } catch (e: Exception) {
                    null
                }
            }

            QuizSpec(
                questions = questions,
                passingScore = (it["passingScore"] as? Long)?.toInt() ?: 70,
                allowRetry = it["allowRetry"] as? Boolean ?: true,
                showAnswers = it["showAnswers"] as? Boolean ?: true
            )
        }

        Lesson(
            lessonId = id,
            courseId = getString("courseId") ?: "",
            title = getString("title") ?: "",
            description = getString("description") ?: "",
            order = (getLong("order") ?: 0).toInt(),
            contentType = LessonContentType.fromString(getString("contentType") ?: "ARTICLE"),
            durationSec = (getLong("durationSec") ?: 0).toInt(),
            storagePath = getString("storagePath") ?: "",
            videoUrl = getString("videoUrl") ?: "",
            body = getString("body") ?: "",
            quizSpec = quizSpec,
            thumbnailUrl = getString("thumbnailUrl") ?: "",
            completed = getBoolean("completed") ?: false,
            locked = getBoolean("locked") ?: false,
            createdAt = getLong("createdAt") ?: 0L,
            updatedAt = getLong("updatedAt") ?: 0L
        )
    } catch (e: Exception) {
        null
    }
}

/**
 * Extension function to convert Lesson to Map
 */
private fun Lesson.toMap(): Map<String, Any> {
    val map = mutableMapOf<String, Any>(
        "lessonId" to lessonId,
        "courseId" to courseId,
        "title" to title,
        "description" to description,
        "order" to order,
        "contentType" to contentType.name,
        "durationSec" to durationSec,
        "storagePath" to storagePath,
        "videoUrl" to videoUrl,
        "body" to body,
        "thumbnailUrl" to thumbnailUrl,
        "completed" to completed,
        "locked" to locked,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )

    quizSpec?.let {
        map["quizSpec"] = mapOf(
            "questions" to it.questions.map { question ->
                mapOf(
                    "id" to question.id,
                    "question" to question.question,
                    "options" to question.options,
                    "correctAnswer" to question.correctAnswer,
                    "explanation" to question.explanation,
                    "imageUrl" to question.imageUrl
                )
            },
            "passingScore" to it.passingScore,
            "allowRetry" to it.allowRetry,
            "showAnswers" to it.showAnswers
        )
    }

    return map
}
