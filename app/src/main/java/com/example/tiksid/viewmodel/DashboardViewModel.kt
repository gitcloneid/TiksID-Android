package com.example.tiksid.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tiksid.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _selectedTicketId = MutableStateFlow<Int?>(null)
    val selectedTicketId: StateFlow<Int?> = _selectedTicketId

    fun selectTab(index: Int) {
        _selectedTab.value = index
        // Clear selected ticket when changing tabs
        if (_selectedTicketId.value != null) {
            _selectedTicketId.value = null
        }
    }

    fun setLoggedIn(loggedIn: Boolean) {
        _isLoggedIn.value = loggedIn
    }

    fun selectTicket(transactionId: Int) {
        _selectedTicketId.value = transactionId
    }

    fun clearSelectedTicket() {
        _selectedTicketId.value = null
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                return DashboardViewModel() as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}