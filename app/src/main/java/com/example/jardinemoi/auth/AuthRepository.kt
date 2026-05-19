package com.example.jardinemoi.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed class AuthResult {
    data class Success(val user: FirebaseUser) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

object AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun login(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                AuthResult.Success(user)
            } else {
                AuthResult.Error("Utilisateur non trouvé")
            }
        } catch (e: com.google.firebase.auth.FirebaseAuthException) {
            val message = when (e.errorCode) {
                "ERROR_INVALID_EMAIL" -> "L'adresse e-mail n'est pas valide."
                "ERROR_WRONG_PASSWORD" -> "Le mot de passe est incorrect."
                "ERROR_USER_NOT_FOUND" -> "Aucun compte ne correspond à cet e-mail."
                "ERROR_USER_DISABLED" -> "Ce compte a été désactivé."
                "ERROR_TOO_MANY_REQUESTS" -> "Trop de tentatives échouées. Veuillez réessayer plus tard."
                "ERROR_INVALID_CREDENTIAL" -> "L'e-mail ou le mot de passe est incorrect."
                else -> "Erreur d'authentification : ${e.localizedMessage}"
            }
            AuthResult.Error(message)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Erreur de connexion")
        }
    }

    suspend fun register(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                AuthResult.Success(user)
            } else {
                AuthResult.Error("Erreur lors de la création de l'utilisateur")
            }
        } catch (e: com.google.firebase.auth.FirebaseAuthException) {
            val message = when (e.errorCode) {
                "ERROR_INVALID_EMAIL" -> "L'adresse e-mail n'est pas valide."
                "ERROR_EMAIL_ALREADY_IN_USE" -> "Cet e-mail est déjà utilisé par un autre compte."
                "ERROR_WEAK_PASSWORD" -> "Le mot de passe est trop faible."
                else -> "Erreur d'inscription : ${e.localizedMessage}"
            }
            AuthResult.Error(message)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Erreur d'inscription")
        }
    }

    suspend fun createUserProfile(uid: String, name: String, email: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val userData = mapOf(
                "name" to name,
                "email" to email,
                "createdAt" to System.currentTimeMillis()
            )
            firestore.collection("users").document(uid).set(userData).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun currentUser() = auth.currentUser

    fun isLoggedIn() = auth.currentUser != null
}
