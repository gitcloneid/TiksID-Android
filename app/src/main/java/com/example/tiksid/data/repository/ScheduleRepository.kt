package com.example.tiksid.data.repository

import android.content.Context
import android.util.Log
import com.example.tiksid.data.URLi
import com.example.tiksid.data.models.Schedule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

class ScheduleRepository(private val context: Context) {
    private val token: String? get() = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        .getString("token", null)
    private val baseUrl = URLi().url

    suspend fun getSchedulesByMovie(movieId: Int): List<Schedule> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/Schedule/byMovie/$movieId")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("Accept", "*/*")
            }

            val responseCode = conn.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception("Failed to fetch schedules: HTTP $responseCode")
            }

            val response = conn.inputStream.bufferedReader().readText()
            val jsonArray = JSONArray(response)

            (0 until jsonArray.length()).map { i ->
                val obj = jsonArray.getJSONObject(i)
                Schedule(
                    id = obj.getInt("id"),
                    movieId = obj.getInt("movieId"),
                    theaterId = obj.getInt("theaterId"),
                    date = obj.getString("date"),
                    time = obj.getString("time"),
                    price = obj.getInt("price")
                )
            }
        } catch (e: Exception) {
            Log.e("ScheduleRepository", "Error fetching schedules", e)
            emptyList()
        }
    }

    suspend fun getSchedule(scheduleId: Int): Schedule? = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/Schedule")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("Accept", "*/*")
            }

            val responseCode = conn.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception("Failed to fetch schedules: HTTP $responseCode")
            }

            val response = conn.inputStream.bufferedReader().readText()
            val jsonArray = JSONArray(response)

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                if (obj.getInt("id") == scheduleId) {
                    return@withContext Schedule(
                        id = obj.getInt("id"),
                        movieId = obj.getInt("movieId"),
                        theaterId = obj.getInt("theaterId"),
                        date = obj.getString("date"),
                        time = obj.getString("time"),
                        price = obj.getInt("price")
                    )
                }
            }
            null
        } catch (e: Exception) {
            Log.e("ScheduleRepository", "Error fetching schedule", e)
            null
        }
    }
}