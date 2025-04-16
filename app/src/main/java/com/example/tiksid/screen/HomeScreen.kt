// screen/HomeScreen.kt
package com.example.tiksid.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tiksid.data.models.Movie
import com.example.tiksid.data.repository.GenreRepository
import com.example.tiksid.data.repository.MovieRepository
import com.example.tiksid.ui.components.NetworkImage
import com.example.tiksid.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onMovieClick: (Movie) -> Unit
) {
    val context = LocalContext.current
    val movieRepository = remember { MovieRepository(context) }
    val genreRepository = remember { GenreRepository(context) }
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.Factory(movieRepository, genreRepository)
    )

    val popularMovie by viewModel.popularMovie.collectAsState()
    val newTitles by viewModel.newTitles.collectAsState()
    val genreCache by viewModel.genreCache.collectAsState()
    val isPopularMovieFallback by viewModel.isPopularMovieFallback.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0B15))
    ) {
        if (isLoading && popularMovie == null && newTitles.isEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFFFFC93C)
            )
        } else if (error != null && popularMovie == null && newTitles.isEmpty()) {
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
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // populer movie
                Text(
                    text = "Popular this week",
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Spacer(modifier = Modifier.padding(5.dp))

                if (popularMovie == null) {
                    Text(
                        text = "No movies available",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                } else {
                    popularMovie?.let { movie ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .padding(12.dp)
                                .clickable { onMovieClick(movie) }
                        ) {
                            NetworkImage(
                                url = movie.poster,
                                contentDescription = movie.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(2f / 3f),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = movie.title,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    text = genreCache[movie.id] ?: "Loading...",
                                    color = Color.LightGray,
                                    fontSize = 14.sp
                                )

                                Text(
                                    text = " • ${movie.duration} minutes",
                                    color = Color.LightGray,
                                    fontSize = 14.sp
                                )

                                if (isPopularMovieFallback) {
                                    Text(
                                        text = " • Released: ${movie.releaseDate}",
                                        color = Color.LightGray,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // new title
                Text(
                    text = "New Titles",
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (newTitles.isEmpty()) {
                    Text(
                        text = "No new titles available",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(newTitles) { movie ->
                            Column(
                                modifier = Modifier
                                    .width(120.dp)
                                    .clickable { onMovieClick(movie) }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .height(160.dp)
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                ) {
                                    NetworkImage(
                                        url = movie.poster,
                                        contentDescription = movie.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                Text(
                                    text = movie.title,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(top = 8.dp),
                                    maxLines = 1
                                )

                                Text(
                                    text = genreCache[movie.id] ?: "Loading...",
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )

                                Text(
                                    text = "${movie.duration} min",
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}