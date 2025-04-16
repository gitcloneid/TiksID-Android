package com.example.tiksid.data.repository

import android.content.Context
import android.util.Log
import com.example.tiksid.data.URLi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class AuthRepository(private val context: Context) {
    private val baseUrl = URLi().url
    private val prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)

    suspend fun login(email: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("$baseUrl/api/Auth/login")
                val json = JSONObject().apply {
                    put("email", email)
                    put("password", password)
                }
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true
                conn.outputStream.write(json.toString().toByteArray())

                if (conn.responseCode in 200..299) {
                    val response = conn.inputStream.bufferedReader().readText()
                    val token = JSONObject(response).getString("token")
                    prefs.edit()
                        .putString("token", token)
                        .apply()
                    true
                } else false
            } catch (e: Exception) {
                Log.e("AuthRepository", "Login error", e)
                false
            }
        }
    }

    fun isLoggedIn(): Boolean {
        return prefs.getString("token", null) != null
    }

    fun logout() {
        prefs.edit().remove("token").apply()
    }
}