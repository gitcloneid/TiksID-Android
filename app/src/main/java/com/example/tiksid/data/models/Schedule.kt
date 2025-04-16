package com.example.tiksid.data.models

data class Schedule(
    val id: Int,
    val movieId: Int,
    val theaterId: Int,
    val date: String,
    val time: String,
    val price: Int
)