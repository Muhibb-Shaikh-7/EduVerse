package com.example.eduverse.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eduverse.data.model.User
import com.example.eduverse.data.model.UserRole
import com.example.eduverse.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Authentication ViewModel
 *
 * PURPOSE: Manage authentication UI state and operations
 * DATA SOURCE: 100% Firebase (via AuthRepository)
 *
 * Features:
 * 1. Login/Register with email & password
 * 2. Google Sign-In support
 * 3. Real-time auth state observation
 * 4. Auto-fetch user data from Firestore
 * 5. NO local storage - all data from Firebase
 *
 * State Management:
 * - authState: Current authentication state (user, loading, error)
 * - Observes Firebase Auth state changes automatically
 * - Fetches user data from Firestore when auth state changes
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // UI State for authentication
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        // Automatically observe Firebase Auth state changes
        observeAuthStateChanges()

        // Check if user is already logged in
        checkCurrentUser()
    }

    /**
     * Observe Firebase Authentication state in real-time
     *
     * When auth state changes:
     * 1. Firebase notifies us via Flow
     * 2. If user logged in, fetch full user data from Firestore
     * 3. Update UI state
     */
    private fun observeAuthStateChanges() {
        viewModelScope.launch {
            authRepository.observeAuthState().collect { firebaseUser ->
                if (firebaseUser != null) {
                    // User is logged in, fetch complete data from Firestore
                    val user = authRepository.getCurrentUser()
                    _authState.update { it.copy(user = user) }
                } else {
                    // User is logged out
                    _authState.update { it.copy(user = null) }
                }
            }
        }
    }

    /**
     * Check if user is currently logged in
     * Fetches user data from Firestore if logged in
     */
    private fun checkCurrentUser() {
        viewModelScope.launch {
            if (authRepository.isUserLoggedIn()) {
                val user = authRepository.getCurrentUser()
                _authState.update { it.copy(user = user) }
            }
        }
    }

    /**
     * Login with email and password
     *
     * Flow:
     * 1. Validate input
     * 2. Show loading state
     * 3. Call Firebase Auth via repository
     * 4. Fetch user data from Firestore
     * 5. Update UI state (success or error)
     *
     * @param email User email
     * @param password User password
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            // Show loading state
            _authState.update { it.copy(isLoading = true, error = null) }

            // Call Firebase Auth
            val result = authRepository.login(email, password)

            // Handle result
            result
                .onSuccess { user ->
                    // Login successful
                    _authState.update {
                        it.copy(
                            user = user,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
                    // Login failed
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Login failed"
                        )
                    }
                }
        }
    }

    /**
     * Register new user
     *
     * Flow:
     * 1. Validate input
     * 2. Show loading state
     * 3. Create Firebase Auth account
     * 4. Create user document in Firestore
     * 5. Auto-login and update UI state
     *
     * @param email User email
     * @param password User password
     * @param name Full name
     * @param role User role (ADMIN, TEACHER, or STUDENT)
     */
    fun register(
        email: String,
        password: String,
        name: String,
        role: UserRole
    ) {
        viewModelScope.launch {
            // Show loading state
            _authState.update { it.copy(isLoading = true, error = null) }

            // Call Firebase Auth
            val result = authRepository.register(email, password, name, role)

            // Handle result
            result
                .onSuccess { user ->
                    // Registration successful, user is auto-logged in
                    _authState.update {
                        it.copy(
                            user = user,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
                    // Registration failed
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Registration failed"
                        )
                    }
                }
        }
    }

    /**
     * Sign in with Google
     *
     * Note: You need to handle Google Sign-In UI flow first
     * to get the ID token, then call this function
     *
     * @param idToken Google ID token from GoogleSignInAccount
     */
    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }

            val result = authRepository.signInWithGoogle(idToken)

            result
                .onSuccess { user ->
                    _authState.update {
                        it.copy(
                            user = user,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Google sign-in failed"
                        )
                    }
                }
        }
    }

    /**
     * Logout current user
     *
     * This:
     * 1. Clears Firebase Auth session
     * 2. Removes user data from UI state
     * 3. No local data to clear (everything is in Firebase)
     */
    fun logout() {
        authRepository.logout()
        _authState.update { it.copy(user = null, error = null) }
    }

    /**
     * Clear error message
     * Call this after showing error to user
     */
    fun clearError() {
        _authState.update { it.copy(error = null) }
    }

    /**
     * Send password reset email
     *
     * @param email Email address to send reset link
     */
    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }

            val result = authRepository.sendPasswordResetEmail(email)

            result
                .onSuccess {
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            error = "Password reset email sent! Check your inbox."
                        )
                    }
                }
                .onFailure { exception ->
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to send reset email"
                        )
                    }
                }
        }
    }

    /**
     * Update user profile
     *
     * Example: Update display name or bio
     *
     * @param updates Map of fields to update
     */
    fun updateProfile(updates: Map<String, Any>) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch

            _authState.update { it.copy(isLoading = true, error = null) }

            val result = authRepository.updateUserProfile(userId, updates)

            result
                .onSuccess {
                    // Fetch updated user data from Firestore
                    val updatedUser = authRepository.getCurrentUser()
                    _authState.update {
                        it.copy(
                            user = updatedUser,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to update profile"
                        )
                    }
                }
        }
    }
}

/**
 * Authentication UI State
 *
 * @param user Current logged-in user (from Firestore) or null
 * @param isLoading True when operation in progress
 * @param error Error message to display or null
 */
data class AuthState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
