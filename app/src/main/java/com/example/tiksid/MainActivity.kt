package com.example.tiksid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tiksid.screen.DashboardScreen
import com.example.tiksid.screen.LoginScreen
import com.example.tiksid.screen.TicketScreen
import com.example.tiksid.ui.theme.TiksIDTheme
import com.example.tiksid.viewmodel.DashboardViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TiksIDTheme {
                AppEntry()
            }
        }
    }
}

@Composable
fun AppEntry() {
    val viewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModel.Factory()
    )

    val selectedTab by viewModel.selectedTab.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val selectedTicketId by viewModel.selectedTicketId.collectAsState()

    if (!isLoggedIn) {
        LoginScreen(navToDashboard = { viewModel.setLoggedIn(true) })
    } else {
        DashboardScreen(
            selectedIndex = selectedTab,
            onTabSelected = { viewModel.selectTab(it) },
            onViewTicketDetail = { transactionId ->
                viewModel.selectTicket(transactionId)
            }
        )
    }
}