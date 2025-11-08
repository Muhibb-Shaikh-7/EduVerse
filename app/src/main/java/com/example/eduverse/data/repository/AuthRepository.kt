package com.example.eduverse.data.repository

import com.example.eduverse.data.firebase.FirebaseAuthService
import com.example.eduverse.data.model.User
import com.example.eduverse.data.model.UserRole
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Authentication Repository
 *
 * PURPOSE: Central repository for all authentication operations
 * DATA STORAGE: 100% Firebase (NO local storage)
 *
 * This repository acts as a bridge between UI and Firebase:
 * - Delegates all auth operations to FirebaseAuthService
 * - NO SharedPreferences usage
 * - NO mock user data
 * - All data comes from Firebase Auth + Firestore
 *
 * Data Flow:
 * UI → AuthRepository → FirebaseAuthService → Firebase Cloud
 */
@Singleton
class AuthRepository @Inject constructor(
    private val authService: FirebaseAuthService
) {

    /**
     * Register new user with email and password
     *
     * Flow:
     * 1. Validate input (email, password, name)
     * 2. Call Firebase Auth to create account
     * 3. Store user profile in Firestore
     * 4. Return user object
     *
     * @param email User email (must be valid format)
     * @param password Password (minimum 6 characters)
     * @param name Full name
     * @param role User role (ADMIN, TEACHER, or STUDENT)
     * @return Result with User object or error
     */
    suspend fun register(
        email: String,
        password: String,
        name: String,
        role: UserRole
    ): Result<User> {
        // Input validation
        if (email.isBlank()) {
            return Result.failure(Exception("Email cannot be empty"))
        }
        if (password.length < 6) {
            return Result.failure(Exception("Password must be at least 6 characters"))
        }
        if (name.isBlank()) {
            return Result.failure(Exception("Name cannot be empty"))
        }

        // Delegate to Firebase Auth Service
        return authService.registerWithEmail(
            email = email,
            password = password,
            displayName = name,
            role = role
        )
    }

    /**
     * Login user with email and password
     *
     * Flow:
     * 1. Validate input
     * 2. Call Firebase Auth to sign in
     * 3. Fetch user data from Firestore
     * 4. Update last login timestamp
     * 5. Return user object
     *
     * @param email User email
     * @param password User password
     * @return Result with User object or error
     */
    suspend fun login(email: String, password: String): Result<User> {
        // Input validation
        if (email.isBlank()) {
            return Result.failure(Exception("Email cannot be empty"))
        }
        if (password.isBlank()) {
            return Result.failure(Exception("Password cannot be empty"))
        }

        // Delegate to Firebase Auth Service
        return authService.signInWithEmail(email, password)
    }

    /**
     * Sign in with Google
     *
     * Note: ID token must be obtained from Google Sign-In flow first
     *
     * @param idToken Google ID token
     * @return Result with User object or error
     */
    suspend fun signInWithGoogle(idToken: String): Result<User> {
        return authService.signInWithGoogle(idToken)
    }

    /**
     * Get currently logged-in user
     *
     * Returns: User object from Firestore or null if not logged in
     *
     * Note: This fetches LIVE data from Firestore every time
     * No caching or local storage involved
     */
    suspend fun getCurrentUser(): User? {
        return authService.getCurrentUser()
    }

    /**
     * Get Firebase User object
     *
     * Returns: FirebaseUser (contains uid, email, displayName, etc.)
     * This is the raw Firebase Auth user, not our custom User object
     */
    fun getCurrentFirebaseUser(): FirebaseUser? {
        return authService.getCurrentFirebaseUser()
    }

    /**
     * Observe authentication state changes in real-time
     *
     * Usage in ViewModel:
     * ```
     * authRepository.observeAuthState().collect { firebaseUser ->
     *     if (firebaseUser != null) {
     *         // User logged in
     *         val user = authRepository.getCurrentUser()
     *     } else {
     *         // User logged out
     *     }
     * }
     * ```
     */
    fun observeAuthState(): Flow<FirebaseUser?> {
        return authService.observeAuthState()
    }

    /**
     * Check if user is currently logged in
     *
     * Returns: true if Firebase Auth session exists, false otherwise
     */
    fun isUserLoggedIn(): Boolean {
        return authService.isUserLoggedIn()
    }

    /**
     * Get current user ID
     *
     * Returns: Firebase UID or null if not logged in
     */
    fun getCurrentUserId(): String? {
        return authService.getCurrentUserId()
    }

    /**
     * Update user profile in Firestore
     *
     * Example usage:
     * ```
     * val updates = mapOf(
     *     "displayName" to "New Name",
     *     "bio" to "Updated bio",
     *     "avatarUrl" to "https://..."
     * )
     * authRepository.updateUserProfile(userId, updates)
     * ```
     *
     * @param uid User ID
     * @param updates Map of field names to new values
     * @return Result indicating success or failure
     */
    suspend fun updateUserProfile(uid: String, updates: Map<String, Any>): Result<Unit> {
        return authService.updateUserProfile(uid, updates)
    }

    /**
     * Change current user's password
     *
     * @param newPassword New password (min 6 characters)
     * @return Result indicating success or failure
     */
    suspend fun changePassword(newPassword: String): Result<Unit> {
        if (newPassword.length < 6) {
            return Result.failure(Exception("Password must be at least 6 characters"))
        }
        return authService.changePassword(newPassword)
    }

    /**
     * Send password reset email
     *
     * @param email Email address to send reset link
     * @return Result indicating success or failure
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        if (email.isBlank()) {
            return Result.failure(Exception("Email cannot be empty"))
        }
        return authService.sendPasswordResetEmail(email)
    }

    /**
     * Logout current user
     *
     * This clears the Firebase Auth session
     * No local data to clear since we don't store anything locally
     */
    fun logout() {
        authService.signOut()
    }

    /**
     * Delete user account permanently
     *
     * WARNING: This cannot be undone!
     * Deletes:
     * - Firebase Auth account
     * - User document from Firestore
     * - User cannot login again with same credentials
     *
     * @return Result indicating success or failure
     */
    suspend fun deleteAccount(): Result<Unit> {
        return authService.deleteAccount()
    }
}
