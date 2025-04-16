package com.example.tiksid.data.repository

import android.content.Context
import android.util.Log
import com.example.tiksid.data.URLi
import com.example.tiksid.data.models.Genre
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

class GenreRepository(private val context: Context) {
    private val token: String? get() = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        .getString("token", null)
    private val baseUrl = URLi().url

    suspend fun getGenresForMovie(movieId: Int): List<Genre> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/Genre/searchByMovieId/$movieId")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Authorization", "Bearer $token")
            }

            val response = conn.inputStream.bufferedReader().readText()
            val jsonArray = JSONArray(response)

            return@withContext (0 until jsonArray.length()).map { i ->
                val obj = jsonArray.getJSONObject(i)
                Genre(
                    id = obj.getInt("id"),
                    name = obj.getString("name")
                )
            }
        } catch (e: Exception) {
            Log.e("GenreRepository", "Error fetching genres for movie $movieId", e)
            emptyList()
        }
    }

    suspend fun getFirstGenreNameForMovie(movieId: Int): String {
        return try {
            val genres = getGenresForMovie(movieId)
            genres.firstOrNull()?.name ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }
}