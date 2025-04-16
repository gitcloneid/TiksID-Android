// viewmodel/HomeViewModel.kt
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HomeViewModel(
    private val movieRepository: MovieRepository,
    private val genreRepository: GenreRepository
) : ViewModel() {

    private val _popularMovie = MutableStateFlow<Movie?>(null)
    val popularMovie: StateFlow<Movie?> = _popularMovie.asStateFlow()

    private val _newTitles = MutableStateFlow<List<Movie>>(emptyList())
    val newTitles: StateFlow<List<Movie>> = _newTitles.asStateFlow()

    private val _genreCache = MutableStateFlow<Map<Int, String>>(emptyMap())
    val genreCache: StateFlow<Map<Int, String>> = _genreCache.asStateFlow()

    private val _isPopularMovieFallback = MutableStateFlow(false)
    val isPopularMovieFallback: StateFlow<Boolean> = _isPopularMovieFallback.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // load popular
                val popular = movieRepository.getPopularMovie()

                // Load new mov
                val newlyReleasedMovies = movieRepository.getNewlyReleasedMovies()

                // load many transaction
                if (popular == null && newlyReleasedMovies.isNotEmpty()) {
                    _popularMovie.value = newlyReleasedMovies.maxByOrNull { movie ->
                        LocalDate.parse(movie.releaseDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    }
                    _isPopularMovieFallback.value = true
                } else {
                    _popularMovie.value = popular
                    _isPopularMovieFallback.value = false
                }

                // sort by ascending
                val sortedMovies = newlyReleasedMovies.sortedBy { movie ->
                    movie.title
                }
                _newTitles.value = sortedMovies

                // load genre all mov
                val allMovies = (popular?.let { listOf(it) } ?: emptyList()) + sortedMovies
                val genreMap = mutableMapOf<Int, String>()
                allMovies.distinctBy { it.id }.forEach { movie ->
                    val genres = genreRepository.getGenresForMovie(movie.id)
                    genreMap[movie.id] = genres.joinToString(", ") { it.name }
                }
                _genreCache.value = genreMap

            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error loading data", e)
                _error.value = "Failed to load data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    class Factory(
        private val movieRepository: MovieRepository,
        private val genreRepository: GenreRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel(movieRepository, genreRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}