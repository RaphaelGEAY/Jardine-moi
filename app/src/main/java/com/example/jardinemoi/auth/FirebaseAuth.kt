package com.example.jardinemoi.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val auth by lazy { FirebaseAuth.getInstance() }

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        _isAuthenticated.value = firebaseAuth.currentUser != null
    }

    init {
        // Initialiser l'état d'authentification de manière sécurisée
        _isAuthenticated.value = auth.currentUser != null
        auth.addAuthStateListener(authListener)
    }

    fun login(email: String, password: String) {
        if (_uiState.value.isLoading) return
        
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState(error = "Veuillez remplir tous les champs.")
            return
        }

        _uiState.value = AuthUiState(isLoading = true)

        viewModelScope.launch {
            val result = AuthRepository.login(email, password)
            when (result) {
                is AuthResult.Success -> {
                    _uiState.value = AuthUiState(isSuccess = true)
                }
                is AuthResult.Error -> {
                    _uiState.value = AuthUiState(error = result.message)
                }
            }
        }
    }

    fun register(name: String, email: String, password: String, onSuccess: (FirebaseUser) -> Unit, onError: (String) -> Unit) {
        if (_uiState.value.isLoading) return

        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState(error = "Veuillez remplir tous les champs.")
            return
        }

        _uiState.value = AuthUiState(isLoading = true)
        viewModelScope.launch {
            val result = AuthRepository.register(email, password)
            when (result) {
                is AuthResult.Success -> {
                    val profileCreated = AuthRepository.createUserProfile(result.user.uid, name, email)
                    if (profileCreated) {
                        _uiState.value = AuthUiState(isSuccess = true)
                        onSuccess(result.user)
                    } else {
                        _uiState.value = AuthUiState(error = "Erreur lors de la création du profil.")
                        onError("Erreur lors de la création du profil.")
                    }
                }
                is AuthResult.Error -> {
                    _uiState.value = AuthUiState(error = result.message)
                    onError(result.message)
                }
            }
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun setError(message: String?) {
        _uiState.value = _uiState.value.copy(error = message)
    }

    fun currentUser() = auth.currentUser

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authListener)
    }
}
