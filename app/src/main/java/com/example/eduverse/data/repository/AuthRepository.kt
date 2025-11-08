package com.example.eduverse.data.repository

import android.content.Context
import com.example.eduverse.data.model.User
import com.example.eduverse.data.model.UserRole
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    // Mock user storage
    private val users = mutableMapOf<String, MockUser>()

    data class MockUser(
        val uid: String,
        val email: String,
        val password: String,
        val displayName: String,
        val role: UserRole
    )

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            // Find user by email
            val mockUser = users.values.find { it.email == email }
                ?: throw Exception("User not found")

            // Check password
            if (mockUser.password != password) {
                throw Exception("Invalid password")
            }

            // Save current user
            prefs.edit()
                .putString("current_user_uid", mockUser.uid)
                .putString("current_user_email", mockUser.email)
                .putString("current_user_name", mockUser.displayName)
                .putString("current_user_role", mockUser.role.name)
                .apply()

            val user = User(
                uid = mockUser.uid,
                email = mockUser.email,
                displayName = mockUser.displayName,
                role = mockUser.role
            )

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(
        email: String,
        password: String,
        name: String,
        role: UserRole
    ): Result<User> {
        return try {
            // Check if user already exists
            if (users.values.any { it.email == email }) {
                throw Exception("Email already registered")
            }

            // Create new user
            val uid = UUID.randomUUID().toString()
            val mockUser = MockUser(
                uid = uid,
                email = email,
                password = password,
                displayName = name,
                role = role
            )

            // Store user
            users[uid] = mockUser

            // Save current user
            prefs.edit()
                .putString("current_user_uid", uid)
                .putString("current_user_email", email)
                .putString("current_user_name", name)
                .putString("current_user_role", role.name)
                .apply()

            val user = User(
                uid = uid,
                email = email,
                displayName = name,
                role = role
            )

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUser(): User? {
        val uid = prefs.getString("current_user_uid", null) ?: return null
        val email = prefs.getString("current_user_email", null) ?: return null
        val name = prefs.getString("current_user_name", null) ?: return null
        val roleString = prefs.getString("current_user_role", null) ?: return null
        val role = UserRole.valueOf(roleString)

        return User(
            uid = uid,
            email = email,
            displayName = name,
            role = role
        )
    }

    fun logout() {
        prefs.edit().clear().apply()
    }

    fun isUserLoggedIn(): Boolean {
        return prefs.getString("current_user_uid", null) != null
    }
}
