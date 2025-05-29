package com.naulify.agent.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naulify.agent.repository.AuthRepository
import com.naulify.agent.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState

    private val _isEmailVerified = MutableStateFlow(false)
    val isEmailVerified: StateFlow<Boolean> = _isEmailVerified

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser()
            if (currentUser != null) {
                _isEmailVerified.value = authRepository.isEmailVerified()
                _authState.value = AuthState.Authenticated(currentUser.uid)
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val user = authRepository.signInWithEmail(email, password)
                user?.let {
                    _isEmailVerified.value = it.isEmailVerified
                    _authState.value = AuthState.Authenticated(it.uid)
                } ?: run {
                    _authState.value = AuthState.Error("Authentication failed")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Authentication failed")
            }
        }
    }

    fun signUpWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val user = authRepository.signUpWithEmail(email, password)
                user?.let {
                    authRepository.sendEmailVerification()
                    _authState.value = AuthState.VerificationRequired(it.uid)
                } ?: run {
                    _authState.value = AuthState.Error("Sign up failed")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Sign up failed")
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val user = authRepository.signInWithGoogle(idToken)
                user?.let {
                    _authState.value = AuthState.Authenticated(it.uid)
                } ?: run {
                    _authState.value = AuthState.Error("Google sign in failed")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Google sign in failed")
            }
        }
    }

    fun sendVerificationEmail() {
        viewModelScope.launch {
            try {
                authRepository.sendEmailVerification()
                _authState.value = AuthState.VerificationEmailSent
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Failed to send verification email")
            }
        }
    }

    fun checkEmailVerification() {
        viewModelScope.launch {
            _isEmailVerified.value = authRepository.isEmailVerified()
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _authState.value = AuthState.Unauthenticated
        }
    }
}

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    object VerificationEmailSent : AuthState()
    data class Authenticated(val userId: String) : AuthState()
    data class VerificationRequired(val userId: String) : AuthState()
    data class Error(val message: String) : AuthState()
}
