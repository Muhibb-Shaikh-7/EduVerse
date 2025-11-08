package com.example.eduverse.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eduverse.data.model.User
import com.example.eduverse.data.model.UserRole
import com.example.eduverse.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            val firebaseUser = authRepository.currentUser
            if (firebaseUser != null) {
                try {
                    val user = authRepository.getUserFromFirestore(firebaseUser.uid)
                    _authState.value = AuthState(user = user)
                } catch (e: Exception) {
                    _authState.value = AuthState(error = e.message)
                }
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)

            val result = authRepository.login(email, password)

            _authState.value = if (result.isSuccess) {
                AuthState(user = result.getOrNull())
            } else {
                AuthState(error = result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    fun register(email: String, password: String, name: String, role: UserRole) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)

            val result = authRepository.register(email, password, name, role)

            _authState.value = if (result.isSuccess) {
                AuthState(user = result.getOrNull())
            } else {
                AuthState(error = result.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _authState.value = AuthState()
    }

    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }
}
