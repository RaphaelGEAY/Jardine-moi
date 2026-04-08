package com.example.jardinemoi.auth

import com.example.jardinemoi.SupabaseManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


object AuthRepository {

    suspend fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            withContext(Dispatchers.IO) {
                SupabaseManager.client.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
            }
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
            withContext(Dispatchers.IO) {
                SupabaseManager.client.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
            }
            onSuccess()
        } catch (e: Exception) {
            onError(e.message ?: "Erreur d'inscription")
        }
    }
}


