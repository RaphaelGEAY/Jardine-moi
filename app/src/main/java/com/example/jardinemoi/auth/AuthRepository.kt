package com.example.jardinemoi.auth

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

object AuthRepository {

    private val auth = FirebaseAuth.getInstance()

    suspend fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            auth.signInWithEmailAndPassword(email, password).await()
            onSuccess()
        } catch (e: Exception) {
            onError(e.message ?: "Erreur de connexion")
        }
    }

    suspend fun register(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            auth.createUserWithEmailAndPassword(email, password).await()
            onSuccess()
        } catch (e: Exception) {
            onError(e.message ?: "Erreur d'inscription")
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}
