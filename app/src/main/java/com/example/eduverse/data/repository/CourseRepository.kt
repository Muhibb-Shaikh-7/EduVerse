package com.example.eduverse.data.repository

import com.example.eduverse.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Course Repository
 *
 * Handles all course-related Firestore operations
 */
@Singleton
class CourseRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val COURSES_COLLECTION = "courses"
        private const val LESSONS_SUBCOLLECTION = "lessons"
    }

    /**
     * Create a new course
     */
    suspend fun createCourse(course: Course): Result<String> {
        return try {
            val docRef = firestore.collection(COURSES_COLLECTION).document()
            val courseWithId = course.copy(courseId = docRef.id)
            docRef.set(courseWithId.toMap()).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update an existing course
     */
    suspend fun updateCourse(courseId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            val updatesWithTimestamp = updates + mapOf("updatedAt" to System.currentTimeMillis())
            firestore.collection(COURSES_COLLECTION)
                .document(courseId)
                .update(updatesWithTimestamp)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a course
     */
    suspend fun deleteCourse(courseId: String): Result<Unit> {
        return try {
            // Delete all lessons first
            val lessonsSnapshot = firestore.collection(COURSES_COLLECTION)
                .document(courseId)
                .collection(LESSONS_SUBCOLLECTION)
                .get()
                .await()

            // Batch delete lessons
            val batch = firestore.batch()
            lessonsSnapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }

            // Delete the course
            val courseRef = firestore.collection(COURSES_COLLECTION).document(courseId)
            batch.delete(courseRef)

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get course by ID
     */
    suspend fun getCourse(courseId: String): Result<Course?> {
        return try {
            val doc = firestore.collection(COURSES_COLLECTION)
                .document(courseId)
                .get()
                .await()

            val course = doc.toCourse()
            Result.success(course)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get all published courses
     */
    suspend fun getPublishedCourses(): Result<List<Course>> {
        return try {
            val snapshot = firestore.collection(COURSES_COLLECTION)
                .whereEqualTo("published", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val courses = snapshot.documents.mapNotNull { it.toCourse() }
            Result.success(courses)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get courses by instructor
     */
    suspend fun getCoursesByInstructor(userId: String): Result<List<Course>> {
        return try {
            val snapshot = firestore.collection(COURSES_COLLECTION)
                .whereEqualTo("createdBy", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val courses = snapshot.documents.mapNotNull { it.toCourse() }
            Result.success(courses)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get courses by category
     */
    suspend fun getCoursesByCategory(categoryId: String): Result<List<Course>> {
        return try {
            val snapshot = firestore.collection(COURSES_COLLECTION)
                .whereEqualTo("categoryId", categoryId)
                .whereEqualTo("published", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val courses = snapshot.documents.mapNotNull { it.toCourse() }
            Result.success(courses)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Search courses by title
     */
    suspend fun searchCourses(query: String): Result<List<Course>> {
        return try {
            val snapshot = firestore.collection(COURSES_COLLECTION)
                .whereEqualTo("published", true)
                .orderBy("title")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .await()

            val courses = snapshot.documents.mapNotNull { it.toCourse() }
            Result.success(courses)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get all courses (for admin)
     */
    suspend fun getAllCourses(): Result<List<Course>> {
        return try {
            val snapshot = firestore.collection(COURSES_COLLECTION)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val courses = snapshot.documents.mapNotNull { it.toCourse() }
            Result.success(courses)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Toggle course publish status
     */
    suspend fun togglePublishStatus(courseId: String, published: Boolean): Result<Unit> {
        return try {
            firestore.collection(COURSES_COLLECTION)
                .document(courseId)
                .update(
                    mapOf(
                        "published" to published,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Increment enrollment count
     */
    suspend fun incrementEnrollmentCount(courseId: String): Result<Unit> {
        return try {
            firestore.collection(COURSES_COLLECTION)
                .document(courseId)
                .update("enrollmentCount", FieldValue.increment(1))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Observe courses in real-time
     */
    fun observeCourses(published: Boolean? = null): Flow<List<Course>> = callbackFlow {
        val query = if (published != null) {
            firestore.collection(COURSES_COLLECTION)
                .whereEqualTo("published", published)
                .orderBy("createdAt", Query.Direction.DESCENDING)
        } else {
            firestore.collection(COURSES_COLLECTION)
                .orderBy("createdAt", Query.Direction.DESCENDING)
        }

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val courses = snapshot?.documents?.mapNotNull { it.toCourse() } ?: emptyList()
            trySend(courses)
        }

        awaitClose { listener.remove() }
    }

    /**
     * Get featured courses
     */
    suspend fun getFeaturedCourses(): Result<List<Course>> {
        return try {
            val snapshot = firestore.collection(COURSES_COLLECTION)
                .whereEqualTo("published", true)
                .whereEqualTo("featured", true)
                .orderBy("avgRating", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .await()

            val courses = snapshot.documents.mapNotNull { it.toCourse() }
            Result.success(courses)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Extension function to convert Firestore document to Course
 */
private fun com.google.firebase.firestore.DocumentSnapshot.toCourse(): Course? {
    return try {
        Course(
            courseId = id,
            title = getString("title") ?: "",
            description = getString("description") ?: "",
            thumbnailUrl = getString("thumbnailUrl") ?: "",
            tags = (get("tags") as? List<String>) ?: emptyList(),
            categoryId = getString("categoryId") ?: "",
            categoryName = getString("categoryName") ?: "",
            published = getBoolean("published") ?: false,
            createdBy = getString("createdBy") ?: "",
            createdByName = getString("createdByName") ?: "",
            createdAt = getLong("createdAt") ?: 0L,
            updatedAt = getLong("updatedAt") ?: 0L,
            price = getDouble("price") ?: 0.0,
            difficulty = CourseDifficulty.fromString(getString("difficulty") ?: "BEGINNER"),
            duration = (getLong("duration") ?: 0).toInt(),
            enrollmentCount = (getLong("enrollmentCount") ?: 0).toInt(),
            ratingCount = (getLong("ratingCount") ?: 0).toInt(),
            avgRating = getDouble("avgRating") ?: 0.0,
            lessonCount = (getLong("lessonCount") ?: 0).toInt(),
            language = getString("language") ?: "en",
            featured = getBoolean("featured") ?: false
        )
    } catch (e: Exception) {
        null
    }
}

/**
 * Extension function to convert Course to Map
 */
private fun Course.toMap(): Map<String, Any> = mapOf(
    "courseId" to courseId,
    "title" to title,
    "description" to description,
    "thumbnailUrl" to thumbnailUrl,
    "tags" to tags,
    "categoryId" to categoryId,
    "categoryName" to categoryName,
    "published" to published,
    "createdBy" to createdBy,
    "createdByName" to createdByName,
    "createdAt" to createdAt,
    "updatedAt" to updatedAt,
    "price" to price,
    "difficulty" to difficulty.name,
    "duration" to duration,
    "enrollmentCount" to enrollmentCount,
    "ratingCount" to ratingCount,
    "avgRating" to avgRating,
    "lessonCount" to lessonCount,
    "language" to language,
    "featured" to featured
)
