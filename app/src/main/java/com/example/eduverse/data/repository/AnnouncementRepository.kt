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

@Singleton
class AnnouncementRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val ANNOUNCEMENTS_COLLECTION = "announcements"
    }

    suspend fun createAnnouncement(announcement: Announcement): Result<String> {
        return try {
            val docRef = firestore.collection(ANNOUNCEMENTS_COLLECTION).document()
            val announcementWithId = announcement.copy(id = docRef.id)
            docRef.set(announcementWithId.toMap()).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAnnouncement(id: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            val updatesWithTimestamp = updates + mapOf("updatedAt" to System.currentTimeMillis())
            firestore.collection(ANNOUNCEMENTS_COLLECTION).document(id).update(updatesWithTimestamp)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAnnouncement(id: String): Result<Unit> {
        return try {
            firestore.collection(ANNOUNCEMENTS_COLLECTION).document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAnnouncement(id: String): Result<Announcement?> {
        return try {
            val doc = firestore.collection(ANNOUNCEMENTS_COLLECTION).document(id).get().await()
            Result.success(doc.toAnnouncement())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllAnnouncements(): Result<List<Announcement>> {
        return try {
            val snapshot = firestore.collection(ANNOUNCEMENTS_COLLECTION)
                .orderBy("createdAt", Query.Direction.DESCENDING).get().await()
            Result.success(snapshot.documents.mapNotNull { it.toAnnouncement() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPublishedAnnouncements(): Result<List<Announcement>> {
        return try {
            val snapshot = firestore.collection(ANNOUNCEMENTS_COLLECTION)
                .whereEqualTo("published", true)
                .orderBy("publishedAt", Query.Direction.DESCENDING).get().await()
            Result.success(snapshot.documents.mapNotNull { it.toAnnouncement() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeAnnouncements(): Flow<List<Announcement>> = callbackFlow {
        val listener = firestore.collection(ANNOUNCEMENTS_COLLECTION)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.documents?.mapNotNull { it.toAnnouncement() } ?: emptyList())
            }
        awaitClose { listener.remove() }
    }
}

private fun com.google.firebase.firestore.DocumentSnapshot.toAnnouncement(): Announcement? {
    return try {
        Announcement(
            id = id,
            scope = AnnouncementScope.fromString(getString("scope") ?: "GLOBAL"),
            courseId = getString("courseId") ?: "",
            courseTitle = getString("courseTitle") ?: "",
            title = getString("title") ?: "",
            body = getString("body") ?: "",
            priority = AnnouncementPriority.fromString(getString("priority") ?: "NORMAL"),
            scheduledAt = getLong("scheduledAt") ?: 0L,
            publishedAt = getLong("publishedAt") ?: 0L,
            createdBy = getString("createdBy") ?: "",
            createdByName = getString("createdByName") ?: "",
            createdAt = getLong("createdAt") ?: 0L,
            updatedAt = getLong("updatedAt") ?: 0L,
            published = getBoolean("published") ?: false,
            imageUrl = getString("imageUrl") ?: "",
            targetRoles = (get("targetRoles") as? List<String>)?.map { UserRole.fromString(it) }
                ?: listOf(UserRole.STUDENT, UserRole.TEACHER)
        )
    } catch (e: Exception) {
        null
    }
}

private fun Announcement.toMap(): Map<String, Any> = mapOf(
    "id" to id,
    "scope" to scope.name,
    "courseId" to courseId,
    "courseTitle" to courseTitle,
    "title" to title,
    "body" to body,
    "priority" to priority.name,
    "scheduledAt" to scheduledAt,
    "publishedAt" to publishedAt,
    "createdBy" to createdBy,
    "createdByName" to createdByName,
    "createdAt" to createdAt,
    "updatedAt" to updatedAt,
    "published" to published,
    "imageUrl" to imageUrl,
    "targetRoles" to targetRoles.map { it.name }
)
