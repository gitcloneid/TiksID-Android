package com.example.tiksid.data.models

data class Transaction(
    val id: Int,
    val userId: Int,
    val scheduleId: Int,
    val transactionDate: String
)