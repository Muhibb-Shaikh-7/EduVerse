package com.example.eduverse.data.repository

import com.example.eduverse.data.model.User
import com.example.eduverse.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("User ID not found")
            val user = getUserFromFirestore(uid)
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
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("User ID not found")

            val user = User(
                uid = uid,
                email = email,
                name = name,
                role = role
            )

            // Save user to Firestore
            firestore.collection("users")
                .document(uid)
                .set(user)
                .await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserFromFirestore(uid: String): User {
        val document = firestore.collection("users")
            .document(uid)
            .get()
            .await()

        return document.toObject(User::class.java) ?: throw Exception("User not found")
    }

    fun logout() {
        auth.signOut()
    }

    fun isUserLoggedIn(): Boolean {
        return currentUser != null
    }
}
