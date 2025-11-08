package com.example.eduverse.data.firebase

import com.example.eduverse.data.model.User
import com.example.eduverse.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Authentication Service
 * 
 * PURPOSE: Handle ALL authentication using Firebase Auth
 * DATA STORAGE: All user data stored in Firestore (NOT locally)
 * 
 * Key Features:
 * 1. Email/Password authentication via Firebase
 * 2. Google Sign-In integration
 * 3. User profile stored in Firestore under "users" collection
 * 4. Real-time auth state observation
 * 5. NO local storage - everything syncs to cloud
 */
@Singleton
class FirebaseAuthService @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    // Firestore collection name for user data
    companion object {
        private const val USERS_COLLECTION = "users"
    }

    /**
     * Get currently authenticated Firebase user
     * Returns: FirebaseUser object or null if not logged in
     */
    fun getCurrentFirebaseUser(): FirebaseUser? {
        return auth.currentUser
    }

    /**
     * Get current user's UID
     * Returns: User ID string or null
     */
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    /**
     * Check if user is currently logged in
     * Returns: true if authenticated, false otherwise
     */
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    /**
     * Observe authentication state changes in real-time
     * 
     * Usage:
     * authService.observeAuthState().collect { user ->
     *     if (user != null) {
     *         // User is logged in
     *     } else {
     *         // User is logged out
     *     }
     * }
     */
    fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(authStateListener)
        awaitClose { auth.removeAuthStateListener(authStateListener) }
    }

    /**
     * Register new user with email and password
     * 
     * Steps:
     * 1. Create Firebase Auth account
     * 2. Create user document in Firestore
     * 3. Return complete user object
     * 
     * @param email User's email address
     * @param password User's password (min 6 characters)
     * @param displayName User's full name
     * @param role User role (ADMIN, TEACHER, or STUDENT)
     * @return Result with User object or error
     */
    suspend fun registerWithEmail(
        email: String,
        password: String,
        displayName: String,
        role: UserRole = UserRole.STUDENT
    ): Result<User> {
        return try {
            android.util.Log.d("FirebaseAuth", "Starting registration for: $email")

            // Step 1: Create Firebase Authentication account
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
                ?: throw Exception("Failed to create user account")

            // Step 2: Create user data object
            val user = User(
                uid = firebaseUser.uid,
                email = email,
                displayName = displayName,
                role = role,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                lastLoginAt = System.currentTimeMillis(),
                isActive = true
            )

            // Step 3: Save user data to Firestore
            // Path: /users/{uid}
            firestore.collection(USERS_COLLECTION)
                .document(user.uid)
                .set(user.toMap())
                .await()

            // Step 4: Update last login timestamp
            updateLastLogin(user.uid)

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sign in with email and password
     * 
     * Steps:
     * 1. Authenticate with Firebase Auth
     * 2. Fetch user data from Firestore
     * 3. Update last login timestamp
     * 4. Return user object
     * 
     * @param email User's email
     * @param password User's password
     * @return Result with User object or error
     */
    suspend fun signInWithEmail(
        email: String,
        password: String
    ): Result<User> {
        return try {
            // Step 1: Authenticate with Firebase
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
                ?: throw Exception("Authentication failed")

            // Step 2: Fetch user data from Firestore
            val user = getUserFromFirestore(firebaseUser.uid)
                ?: throw Exception("User data not found in database")

            // Step 3: Update last login timestamp
            updateLastLogin(firebaseUser.uid)

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sign in with Google
     * 
     * Steps:
     * 1. User selects Google account (handled by Google Sign-In UI)
     * 2. Get ID token from Google
     * 3. Authenticate with Firebase using token
     * 4. Check if user exists in Firestore
     * 5. If new user, create Firestore document
     * 6. Return user object
     * 
     * @param idToken Google ID token from GoogleSignInAccount
     * @return Result with User object or error
     */
    suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            // Step 1: Create Firebase credential from Google token
            val credential = GoogleAuthProvider.getCredential(idToken, null)

            // Step 2: Authenticate with Firebase
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user
                ?: throw Exception("Google sign-in failed")

            // Step 3: Check if user exists in Firestore
            var user = getUserFromFirestore(firebaseUser.uid)

            // Step 4: If new user, create Firestore document
            if (user == null) {
                user = User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = firebaseUser.displayName ?: "",
                    avatarUrl = firebaseUser.photoUrl?.toString() ?: "",
                    role = UserRole.STUDENT, // Default role for Google sign-in
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    lastLoginAt = System.currentTimeMillis(),
                    isActive = true
                )

                // Save to Firestore
                firestore.collection(USERS_COLLECTION)
                    .document(user.uid)
                    .set(user.toMap())
                    .await()
            } else {
                // Update last login for existing user
                updateLastLogin(user.uid)
            }

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch user data from Firestore
     * 
     * @param uid User ID
     * @return User object or null if not found
     */
    suspend fun getUserFromFirestore(uid: String): User? {
        return try {
            val document = firestore.collection(USERS_COLLECTION)
                .document(uid)
                .get()
                .await()

            if (document.exists()) {
                document.toUser()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get current logged-in user's complete data from Firestore
     * 
     * @return User object or null
     */
    suspend fun getCurrentUser(): User? {
        val uid = getCurrentUserId() ?: return null
        return getUserFromFirestore(uid)
    }

    /**
     * Update user profile in Firestore
     * 
     * @param uid User ID
     * @param updates Map of field names to new values
     * @return Result indicating success or failure
     */
    suspend fun updateUserProfile(uid: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            val updatesWithTimestamp = updates + mapOf(
                "updatedAt" to System.currentTimeMillis()
            )

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
     * Update last login timestamp
     * Called automatically after successful login
     * 
     * @param uid User ID
     */
    private suspend fun updateLastLogin(uid: String) {
        try {
            firestore.collection(USERS_COLLECTION)
                .document(uid)
                .update("lastLoginAt", System.currentTimeMillis())
                .await()
        } catch (e: Exception) {
            // Non-critical operation, fail silently
        }
    }

    /**
     * Change user password
     * 
     * @param newPassword New password (min 6 characters)
     * @return Result indicating success or failure
     */
    suspend fun changePassword(newPassword: String): Result<Unit> {
        return try {
            val user = auth.currentUser
                ?: throw Exception("No user logged in")

            user.updatePassword(newPassword).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Send password reset email
     * 
     * @param email Email address
     * @return Result indicating success or failure
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
     * Sign out current user
     * Clears Firebase Auth session
     */
    fun signOut() {
        auth.signOut()
    }

    /**
     * Delete user account
     * WARNING: Deletes both Firebase Auth account and Firestore data
     * 
     * @return Result indicating success or failure
     */
    suspend fun deleteAccount(): Result<Unit> {
        return try {
            val user = auth.currentUser
                ?: throw Exception("No user logged in")

            val uid = user.uid

            // Step 1: Delete Firestore user document
            firestore.collection(USERS_COLLECTION)
                .document(uid)
                .delete()
                .await()

            // Step 2: Delete Firebase Auth account
            user.delete().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Extension function: Convert Firestore document to User object
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
            createdAt = getLong("createdAt") ?: System.currentTimeMillis(),
            updatedAt = getLong("updatedAt") ?: System.currentTimeMillis(),
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
 * Extension function: Convert User object to Firestore map
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
