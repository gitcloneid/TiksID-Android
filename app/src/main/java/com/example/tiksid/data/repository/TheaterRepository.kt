package com.example.tiksid.data.repository

import android.content.Context
import android.util.Log
import com.example.tiksid.data.URLi
import com.example.tiksid.data.models.Theater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

class TheaterRepository(private val context: Context) {
    private val token: String? get() = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        .getString("token", null)
    private val baseUrl = URLi().url

    suspend fun getTheatersByMovie(movieId: Int): List<Theater> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/Theater/byMovie/$movieId")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("Accept", "*/*")
            }

            val responseCode = conn.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception("Failed to fetch theaters: HTTP $responseCode")
            }

            val response = conn.inputStream.bufferedReader().readText()
            val jsonArray = JSONArray(response)

            (0 until jsonArray.length()).map { i ->
                val obj = jsonArray.getJSONObject(i)
                Theater(
                    id = obj.getInt("id"),
                    name = obj.getString("name"),
                    section = obj.getInt("section"),
                    column = obj.getInt("column"),
                    row = obj.getInt("row")
                )
            }
        } catch (e: Exception) {
            Log.e("TheaterRepository", "Error fetching theaters", e)
            emptyList()
        }
    }

    suspend fun getTheater(theaterId: Int): Theater? = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/Theater/$theaterId")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("Accept", "*/*")
            }

            val responseCode = conn.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception("Failed to fetch theater: HTTP $responseCode")
            }

            val response = conn.inputStream.bufferedReader().readText()
            val obj = org.json.JSONObject(response)
            Theater(
                id = obj.getInt("id"),
                name = obj.getString("name"),
                section = obj.getInt("section"),
                column = obj.getInt("column"),
                row = obj.getInt("row")
            )
        } catch (e: Exception) {
            Log.e("TheaterRepository", "Error fetching theater", e)
            null
        }
    }
}