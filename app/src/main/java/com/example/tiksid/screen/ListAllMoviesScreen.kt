// screen/ListAllMoviesScreen.kt
package com.example.tiksid.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tiksid.data.models.Movie
import com.example.tiksid.data.repository.GenreRepository
import com.example.tiksid.data.repository.MovieRepository
import com.example.tiksid.ui.components.MovieCard
import com.example.tiksid.viewmodel.MoviesViewModel

@Composable
fun ListAllMoviesScreen(
    onMovieClick: (Movie) -> Unit
) {
    val context = LocalContext.current
    val movieRepository = remember { MovieRepository(context) }
    val genreRepository = remember { GenreRepository(context) }
    val viewModel: MoviesViewModel = viewModel(
        factory = MoviesViewModel.Factory(movieRepository, genreRepository)
    )

    val movies by viewModel.movies.collectAsState()
    val genreCache by viewModel.genreCache.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFF0B0B15))
    ) {
        if (isLoading && movies.isEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFFFFC93C)
            )
        } else if (error != null && movies.isEmpty()) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Error: $error",
                    color = Color.Red
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "All Movies",
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(movies) { movie ->
                        MovieCard(
                            movie = movie,
                            genre = genreCache[movie.id],
                            onClick = { onMovieClick(movie) }
                        )
                    }
                }
            }
        }
    }
}