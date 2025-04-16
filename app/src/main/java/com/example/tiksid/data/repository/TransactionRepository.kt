package com.example.tiksid.data.repository

import android.content.Context
import android.util.Log
import com.example.tiksid.data.URLi
import com.example.tiksid.data.models.Transaction
import com.example.tiksid.data.models.TransactionDetail
import com.example.tiksid.data.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class TransactionRepository(private val context: Context) {
    private val token: String? get() = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        .getString("token", null)
    private val baseUrl = URLi().url

    suspend fun getUser(): User? = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/Auth/me")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Authorization", "Bearer $token")
            }

            val response = conn.inputStream.bufferedReader().readText()
            val obj = JSONObject(response)
            User(
                id = obj.getInt("id"),
                fullname = obj.getString("fullname"),
                email = obj.getString("email")
            )
        } catch (e: Exception) {
            Log.e("TransactionRepository", "Error fetching user", e)
            null
        }
    }

    suspend fun createTransaction(userId: Int, scheduleId: Int, transactionDate: String): Int? = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/Transaction")
            val json = JSONObject().apply {
                put("id", 0)
                put("userId", userId)
                put("scheduleId", scheduleId)
                put("transactionDate", transactionDate)
            }

            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
            }

            conn.outputStream.write(json.toString().toByteArray())
            if (conn.responseCode in 200..299) {
                val response = conn.inputStream.bufferedReader().readText()
                JSONObject(response).getInt("id")
            } else null
        } catch (e: Exception) {
            Log.e("TransactionRepository", "Error creating transaction", e)
            null
        }
    }

    suspend fun createTransactionDetail(transactionId: Int, seat: String, price: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/TransactionDetails")
            val json = JSONObject().apply {
                put("id", 0)
                put("transactionId", transactionId)
                put("seat", seat)
                put("price", price)
            }

            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
            }

            conn.outputStream.write(json.toString().toByteArray())
            conn.responseCode in 200..299
        } catch (e: Exception) {
            Log.e("TransactionRepository", "Error creating transaction detail", e)
            false
        }
    }

    suspend fun getBookedSeatsForSchedule(scheduleId: Int): List<String> = withContext(Dispatchers.IO) {
        try {
            val transactionsUrl = URL("$baseUrl/api/Transaction")
            val transactionsConn = (transactionsUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("Accept", "*/*")
            }

            val transactionsResponse = transactionsConn.inputStream.bufferedReader().readText()
            val transactionsArray = JSONArray(transactionsResponse)
            val transactionIds = mutableListOf<Int>()
            for (i in 0 until transactionsArray.length()) {
                val transaction = transactionsArray.getJSONObject(i)
                if (transaction.getInt("scheduleId") == scheduleId) {
                    transactionIds.add(transaction.getInt("id"))
                }
            }

            val detailsUrl = URL("$baseUrl/api/TransactionDetails")
            val detailsConn = (detailsUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("Accept", "*/*")
            }

            val detailsResponse = detailsConn.inputStream.bufferedReader().readText()
            val detailsArray = JSONArray(detailsResponse)
            val bookedSeats = mutableListOf<String>()
            for (i in 0 until detailsArray.length()) {
                val detail = detailsArray.getJSONObject(i)
                if (transactionIds.contains(detail.getInt("transactionId"))) {
                    bookedSeats.add(detail.getString("seat"))
                }
            }

            bookedSeats
        } catch (e: Exception) {
            Log.e("TransactionRepository", "Error fetching booked seats", e)
            emptyList()
        }
    }

    // mengambil semua transaksi
    suspend fun getUserTransactions(userId: Int): List<Transaction> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/Transaction")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("Accept", "*/*")
            }

            val responseCode = conn.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception("Failed to fetch transactions: HTTP $responseCode")
            }

            val response = conn.inputStream.bufferedReader().readText()
            val jsonArray = JSONArray(response)

            val transactions = mutableListOf<Transaction>()
            for (i in 0 until jsonArray.length()) {
                val transactionJson = jsonArray.getJSONObject(i)
                if (transactionJson.getInt("userId") == userId) {
                    transactions.add(
                        Transaction(
                            id = transactionJson.getInt("id"),
                            userId = transactionJson.getInt("userId"),
                            scheduleId = transactionJson.getInt("scheduleId"),
                            transactionDate = transactionJson.getString("transactionDate")
                        )
                    )
                }
            }
            transactions
        } catch (e: Exception) {
            Log.e("TransactionRepository", "Error fetching user transactions", e)
            emptyList()
        }
    }

    // detail transaksi
    suspend fun getTransactionDetails(transactionId: Int): List<TransactionDetail> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/TransactionDetails")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("Accept", "*/*")
            }

            val responseCode = conn.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception("Failed to fetch transaction details: HTTP $responseCode")
            }

            val response = conn.inputStream.bufferedReader().readText()
            val jsonArray = JSONArray(response)

            val details = mutableListOf<TransactionDetail>()
            for (i in 0 until jsonArray.length()) {
                val detailJson = jsonArray.getJSONObject(i)
                if (detailJson.getInt("transactionId") == transactionId) {
                    details.add(
                        TransactionDetail(
                            id = detailJson.getInt("id"),
                            transactionId = detailJson.getInt("transactionId"),
                            seat = detailJson.getString("seat"),
                            price = detailJson.getInt("price")
                        )
                    )
                }
            }
            details
        } catch (e: Exception) {
            Log.e("TransactionRepository", "Error fetching transaction details", e)
            emptyList()
        }
    }
}