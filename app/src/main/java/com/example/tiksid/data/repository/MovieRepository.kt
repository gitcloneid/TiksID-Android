package com.example.tiksid.data.repository

import android.content.Context
import android.util.Log
import com.example.tiksid.data.URLi
import com.example.tiksid.data.models.Movie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class MovieRepository(private val context: Context) {
    private val token: String? get() = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        .getString("token", null)
    private val baseUrl = URLi().url
    private val genreRepository = GenreRepository(context)

    suspend fun getMovie(movieId: Int): Movie? = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/Movie")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("Accept", "*/*")
            }

            val responseCode = conn.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception("Failed to fetch movies: HTTP $responseCode")
            }

            val response = conn.inputStream.bufferedReader().readText()
            val jsonArray = JSONArray(response)

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                if (obj.getInt("id") == movieId) {
                    val genres = genreRepository.getGenresForMovie(movieId).map { it.name }
                    return@withContext Movie(
                        id = obj.getInt("id"),
                        title = obj.getString("title"),
                        description = obj.getString("description"),
                        duration = obj.getInt("duration"),
                        releaseDate = obj.getString("releaseDate"),
                        poster = obj.getString("poster"),
                        genres = genres
                    )
                }
            }
            null
        } catch (e: Exception) {
            Log.e("MovieRepository", "Error fetching movie with ID $movieId", e)
            null
        }
    }

    // fetch popular and new release  mov
    private data class PopularMoviesResponse(
        val popularMovie: Movie?,
        val newlyReleasedMovies: List<Movie>
    )

    private suspend fun fetchPopularMovies(): PopularMoviesResponse = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/Movie/popular")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("Accept", "*/*")
            }

            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_NOT_FOUND || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                return@withContext PopularMoviesResponse(null, emptyList())
            }
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception("Failed to fetch popular movies: HTTP $responseCode")
            }

            val response = conn.inputStream.bufferedReader().readText()
            if (response.isEmpty()) {
                return@withContext PopularMoviesResponse(null, emptyList())
            }

            val jsonObj = JSONObject(response)

            val popularMovieJson = jsonObj.optJSONObject("mostPopularMovie")
            val popularMovie = if (popularMovieJson != null) {
                val genres = genreRepository.getGenresForMovie(popularMovieJson.getInt("id")).map { it.name }
                Movie(
                    id = popularMovieJson.getInt("id"),
                    title = popularMovieJson.getString("title"),
                    description = popularMovieJson.getString("description"),
                    duration = popularMovieJson.getInt("duration"),
                    releaseDate = popularMovieJson.getString("releaseDate"),
                    poster = popularMovieJson.getString("poster"),
                    genres = genres
                )
            } else {
                null
            }

            val newlyReleasedMoviesJson = jsonObj.optJSONArray("newlyReleasedMovies") ?: JSONArray()
            val newlyReleasedMovies = (0 until newlyReleasedMoviesJson.length()).map { i ->
                val movieJson = newlyReleasedMoviesJson.getJSONObject(i)
                val genres = genreRepository.getGenresForMovie(movieJson.getInt("id")).map { it.name }
                Movie(
                    id = movieJson.getInt("id"),
                    title = movieJson.getString("title"),
                    description = movieJson.getString("description"),
                    duration = movieJson.getInt("duration"),
                    releaseDate = movieJson.getString("releaseDate"),
                    poster = movieJson.getString("poster"),
                    genres = genres
                )
            }

            PopularMoviesResponse(popularMovie, newlyReleasedMovies)
        } catch (e: Exception) {
            Log.e("MovieRepository", "Error fetching popular movies", e)
            PopularMoviesResponse(null, emptyList())
        }
    }

    suspend fun getPopularMovie(): Movie? = withContext(Dispatchers.IO) {
        fetchPopularMovies().popularMovie
    }

    suspend fun getNewlyReleasedMovies(): List<Movie> = withContext(Dispatchers.IO) {
        fetchPopularMovies().newlyReleasedMovies
    }

    suspend fun getAllMovies(): List<Movie> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/Movie")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("Accept", "*/*")
            }

            val responseCode = conn.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception("Failed to fetch movies: HTTP $responseCode")
            }

            val response = conn.inputStream.bufferedReader().readText()
            val jsonArray = JSONArray(response)

            (0 until jsonArray.length()).map { i ->
                val obj = jsonArray.getJSONObject(i)
                val genres = genreRepository.getGenresForMovie(obj.getInt("id")).map { it.name }
                Movie(
                    id = obj.getInt("id"),
                    title = obj.getString("title"),
                    description = obj.getString("description"),
                    duration = obj.getInt("duration"),
                    releaseDate = obj.getString("releaseDate"),
                    poster = obj.getString("poster"),
                    genres = genres
                )
            }
        } catch (e: Exception) {
            Log.e("MovieRepository", "Error fetching movies", e)
            emptyList()
        }
    }
}