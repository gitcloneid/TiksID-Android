package com.example.tiksid.data.models

import android.os.Parcelable

data class Movie(
    val id: Int,
    val title: String,
    val description: String,
    val duration: Int,
    val releaseDate: String,
    val poster: String,
    val genres: List<String>
)