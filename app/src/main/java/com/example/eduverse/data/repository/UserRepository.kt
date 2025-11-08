package com.example.eduverse.data.repository

import com.example.eduverse.data.model.User
import com.example.eduverse.data.model.UserRole
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User Repository
 *
 * Handles all user-related Firestore operations
 */
@Singleton
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val USERS_COLLECTION = "users"
    }

    /**
     * Create or update user
     */
    suspend fun saveUser(user: User): Result<Unit> {
        return try {
            firestore.collection(USERS_COLLECTION)
                .document(user.uid)
                .set(user.toMap())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get user by UID
     */
    suspend fun getUser(uid: String): Result<User?> {
        return try {
            val doc = firestore.collection(USERS_COLLECTION)
                .document(uid)
                .get()
                .await()

            val user = doc.toUser()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update user
     */
    suspend fun updateUser(uid: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            val updatesWithTimestamp = updates + mapOf("updatedAt" to System.currentTimeMillis())
            firestore.collection(USERS_COLLECTION)
                .document(uid)
                .update(updatesWithTimestamp)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update user role
     */
    suspend fun updateUserRole(uid: String, role: UserRole): Result<Unit> {
        return try {
            firestore.collection(USERS_COLLECTION)
                .document(uid)
                .update(
                    mapOf(
                        "role" to role.name,
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
     * Get all users
     */
    suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val snapshot = firestore.collection(USERS_COLLECTION)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val users = snapshot.documents.mapNotNull { it.toUser() }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get users by role
     */
    suspend fun getUsersByRole(role: UserRole): Result<List<User>> {
        return try {
            val snapshot = firestore.collection(USERS_COLLECTION)
                .whereEqualTo("role", role.name)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val users = snapshot.documents.mapNotNull { it.toUser() }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Search users by name or email
     */
    suspend fun searchUsers(query: String): Result<List<User>> {
        return try {
            // Note: For better search, consider using Algolia or Elasticsearch
            // This is a basic implementation using Firestore
            val snapshot = firestore.collection(USERS_COLLECTION)
                .orderBy("displayName")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .limit(50)
                .get()
                .await()

            val users = snapshot.documents.mapNotNull { it.toUser() }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Observe user in real-time
     */
    fun observeUser(uid: String): Flow<User?> = callbackFlow {
        val listener = firestore.collection(USERS_COLLECTION)
            .document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val user = snapshot?.toUser()
                trySend(user)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Observe all users in real-time
     */
    fun observeAllUsers(): Flow<List<User>> = callbackFlow {
        val listener = firestore.collection(USERS_COLLECTION)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val users = snapshot?.documents?.mapNotNull { it.toUser() } ?: emptyList()
                trySend(users)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Get user count
     */
    suspend fun getUserCount(): Result<Int> {
        return try {
            val snapshot = firestore.collection(USERS_COLLECTION)
                .get()
                .await()
            Result.success(snapshot.size())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update last login time
     */
    suspend fun updateLastLogin(uid: String): Result<Unit> {
        return try {
            firestore.collection(USERS_COLLECTION)
                .document(uid)
                .update("lastLoginAt", System.currentTimeMillis())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Extension function to convert Firestore document to User
 */
private fun com.google.firebase.firestore.DocumentSnapshot.toUser(): User? {
    return try {
        User(
            uid = id,
            email = getString("email") ?: "",
            displayName = getString("displayName") ?: "",
            role = UserRole.fromString(getString("role") ?: "STUDENT"),
            avatarUrl = getString("avatarUrl") ?: "",
            bio = getString("bio") ?: "",
            createdAt = getLong("createdAt") ?: 0L,
            updatedAt = getLong("updatedAt") ?: 0L,
            lastLoginAt = getLong("lastLoginAt") ?: 0L,
            isActive = getBoolean("isActive") ?: true,
            enrolledCourses = (get("enrolledCourses") as? List<String>) ?: emptyList(),
            createdCourses = (get("createdCourses") as? List<String>) ?: emptyList()
        )
    } catch (e: Exception) {
        null
    }
}

/**
 * Extension function to convert User to Map
 */
private fun User.toMap(): Map<String, Any> = mapOf(
    "uid" to uid,
    "email" to email,
    "displayName" to displayName,
    "role" to role.name,
    "avatarUrl" to avatarUrl,
    "bio" to bio,
    "createdAt" to createdAt,
    "updatedAt" to updatedAt,
    "lastLoginAt" to lastLoginAt,
    "isActive" to isActive,
    "enrolledCourses" to enrolledCourses,
    "createdCourses" to createdCourses
)
