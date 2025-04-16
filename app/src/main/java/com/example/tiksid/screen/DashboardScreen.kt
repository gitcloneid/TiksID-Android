package com.example.tiksid.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.tiksid.R
import com.example.tiksid.data.models.Movie

@Composable
fun DashboardScreen(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    onViewTicketDetail: (Int) -> Unit
) {
    var selectedMovie by remember { mutableStateOf<Movie?>(null) }
    var showTickets by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            BottomAppBar(
                containerColor = Color(0xFF0B0B15),
                tonalElevation = 4.dp
            ) {
                NavigationBarItem(
                    icon = {
                        Image(
                            painter = painterResource(id = R.drawable.home),
                            contentDescription = "Home",
                            modifier = Modifier.size(32.dp)
                        )
                    },
                    selected = selectedIndex == 0,
                    onClick = {
                        selectedMovie = null
                        onTabSelected(0)
                    }
                )
                NavigationBarItem(
                    icon = {
                        Image(
                            painter = painterResource(id = R.drawable.category),
                            contentDescription = "Dashboard",
                            modifier = Modifier.size(32.dp)
                        )
                    },
                    selected = selectedIndex == 1,
                    onClick = {
                        selectedMovie = null
                        onTabSelected(1)
                    }
                )
                NavigationBarItem(
                    icon = {
                        Image(
                            painter = painterResource(id = R.drawable.ticket),
                            contentDescription = "Ticket",
                            modifier = Modifier.size(32.dp)
                        )
                    },
                    selected = selectedIndex == 2,
                    onClick = {
                        selectedMovie = null
                        onTabSelected(2)
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when {
                selectedMovie != null -> {
                    MovieDetailScreen(
                        movie = selectedMovie!!,
                        onBack = { selectedMovie = null },
                        onBuyTicketSuccess = {
                            selectedMovie = null
                            onTabSelected(2)
                        }
                    )
                }
                selectedIndex == 0 -> HomeScreen(
                    onMovieClick = { movie ->
                        selectedMovie = movie
                    }
                )
                selectedIndex == 1 -> ListAllMoviesScreen(
                    onMovieClick = { movie ->
                        selectedMovie = movie
                    }
                )
                selectedIndex == 2 -> TicketScreen()
            }
        }
    }
}