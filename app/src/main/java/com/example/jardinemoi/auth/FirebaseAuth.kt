package com.example.jardinemoi.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _isAuthenticated = MutableStateFlow(auth.currentUser != null)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        _isAuthenticated.value = firebaseAuth.currentUser != null
    }

    init {
        auth.addAuthStateListener(authListener)
    }

    fun login(email: String, password: String) {
        _uiState.value = AuthUiState(isLoading = true)

        viewModelScope.launch {
            when (val result = AuthRepository.login(email, password)) {
                is AuthResult.Success -> _uiState.value = AuthUiState(isSuccess = true)
                is AuthResult.Error -> _uiState.value = AuthUiState(error = result.message)
            }
        }
    }

    fun register(email: String, password: String, onSuccess: (FirebaseUser) -> Unit, onError: (String) -> Unit) {
        _uiState.value = AuthUiState(isLoading = true)
        viewModelScope.launch {
            when (val result = AuthRepository.register(email, password)) {
                is AuthResult.Success -> {
                    _uiState.value = AuthUiState(isSuccess = true)
                    onSuccess(result.user)
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

    fun currentUser() = auth.currentUser

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authListener)
    }
}
