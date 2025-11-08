package com.example.eduverse.data.firebase

import com.example.eduverse.data.model.User
import com.example.eduverse.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Authentication Service
 *
 * Handles user authentication operations including:
 * - User registration with role assignment
 * - Login/logout
 * - Password management
 * - User profile updates
 */
@Singleton
class FirebaseAuthService @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    /**
     * Register a new user with Firebase Auth and store user data in Firestore
     */
    suspend fun register(
        email: String,
        password: String,
        displayName: String,
        role: UserRole
    ): Result<User> {
        return try {
            // Create Firebase Auth user
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("User creation failed")

            // Update display name
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()

            // Store user data in Firestore
            val userData = hashMapOf(
                "uid" to firebaseUser.uid,
                "email" to email,
                "displayName" to displayName,
                "role" to role.name,
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )

            firestore.collection("users")
                .document(firebaseUser.uid)
                .set(userData)
                .await()

            // Create user object
            val user = User(
                uid = firebaseUser.uid,
                email = email,
                displayName = displayName,
                role = role
            )

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Login user with email and password
     */
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            // Sign in with Firebase Auth
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("Login failed")

            // Fetch user data from Firestore
            val userDoc = firestore.collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()

            if (!userDoc.exists()) {
                throw Exception("User data not found")
            }

            val roleString = userDoc.getString("role") ?: throw Exception("User role not found")
            val role = UserRole.valueOf(roleString)

            val user = User(
                uid = firebaseUser.uid,
                email = firebaseUser.email ?: email,
                displayName = firebaseUser.displayName ?: userDoc.getString("displayName") ?: "",
                role = role
            )

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get current authenticated user
     */
    suspend fun getCurrentUser(): User? {
        return try {
            val firebaseUser = auth.currentUser ?: return null

            // Fetch user data from Firestore
            val userDoc = firestore.collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()

            if (!userDoc.exists()) return null

            val roleString = userDoc.getString("role") ?: return null
            val role = UserRole.valueOf(roleString)

            User(
                uid = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                displayName = firebaseUser.displayName ?: userDoc.getString("displayName") ?: "",
                role = role
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Logout current user
     */
    fun logout() {
        auth.signOut()
    }

    /**
     * Check if user is logged in
     */
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    /**
     * Send password reset email
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update user profile
     */
    suspend fun updateProfile(displayName: String? = null): Result<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("No user logged in")

            if (displayName != null) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
                user.updateProfile(profileUpdates).await()

                // Update Firestore
                firestore.collection("users")
                    .document(user.uid)
                    .update(
                        mapOf(
                            "displayName" to displayName,
                            "updatedAt" to System.currentTimeMillis()
                        )
                    )
                    .await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete user account
     */
    suspend fun deleteAccount(): Result<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("No user logged in")
            val uid = user.uid

            // Delete user data from Firestore
            firestore.collection("users")
                .document(uid)
                .delete()
                .await()

            // Delete Firebase Auth user
            user.delete().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
