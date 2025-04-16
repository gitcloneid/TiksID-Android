package com.example.tiksid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tiksid.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _loginStatus = MutableStateFlow<LoginStatus>(LoginStatus.Idle)
    val loginStatus: StateFlow<LoginStatus> = _loginStatus.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(repository.isLoggedIn())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginStatus.value = LoginStatus.Loading

            val success = repository.login(email, password)

            if (success) {
                _loginStatus.value = LoginStatus.Success
                _isLoggedIn.value = true
            } else {
                _loginStatus.value = LoginStatus.Error("Username or Password Incorrect")
            }
        }
    }

    fun logout() {
        repository.logout()
        _isLoggedIn.value = false
    }

    fun resetLoginStatus() {
        _loginStatus.value = LoginStatus.Idle
    }

    sealed class LoginStatus {
        object Idle : LoginStatus()
        object Loading : LoginStatus()
        object Success : LoginStatus()
        data class Error(val message: String) : LoginStatus()
    }

    class Factory(private val repository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                return AuthViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}