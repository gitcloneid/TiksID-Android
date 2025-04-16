// viewmodel/MoviesViewModel.kt
package com.example.tiksid.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tiksid.data.models.Movie
import com.example.tiksid.data.repository.GenreRepository
import com.example.tiksid.data.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MoviesViewModel(
    private val movieRepository: MovieRepository,
    private val genreRepository: GenreRepository
) : ViewModel() {

    private val _movies = MutableStateFlow<List<Movie>>(emptyList())
    val movies: StateFlow<List<Movie>> = _movies.asStateFlow()

    private val _genreCache = MutableStateFlow<Map<Int, String>>(emptyMap())
    val genreCache: StateFlow<Map<Int, String>> = _genreCache.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadMovies()
    }

    private fun loadMovies() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val fetchedMovies = movieRepository.getAllMovies()
                _movies.value = fetchedMovies

                // Fetch genres for all movies in bulk
                val genreMap = mutableMapOf<Int, String>()
                fetchedMovies.forEach { movie ->
                    try {
                        val genre = genreRepository.getFirstGenreNameForMovie(movie.id)
                        genreMap[movie.id] = genre
                    } catch (e: Exception) {
                        Log.e("MoviesViewModel", "Error fetching genre for movie ${movie.id}", e)
                        genreMap[movie.id] = "Unknown"
                    }
                }
                _genreCache.value = genreMap

            } catch (e: Exception) {
                Log.e("MoviesViewModel", "Error loading movies", e)
                _error.value = "Failed to load movies: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        loadMovies()
    }

    class Factory(
        private val movieRepository: MovieRepository,
        private val genreRepository: GenreRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MoviesViewModel::class.java)) {
                return MoviesViewModel(movieRepository, genreRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}