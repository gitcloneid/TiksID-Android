// ui/components/MovieCard.kt
package com.example.tiksid.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tiksid.data.models.Movie

@Composable
fun MovieCard(
    movie: Movie,
    genre: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        // Image with 2:3 aspect ratio
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f) // Enforce 2:3 aspect ratio (width:height)
                .clip(RoundedCornerShape(12.dp))
        ) {
            NetworkImage(
                url = movie.poster,
                contentDescription = movie.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Movie title
        Text(
            text = movie.title,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 8.dp),
            maxLines = 1
        )

        // Genre
        Text(
            text = genre ?: "Loading...",
            color = Color.Gray,
            fontSize = 12.sp,
            maxLines = 1
        )

        // Duration
        Text(
            text = "${movie.duration} min",
            color = Color.Gray,
            fontSize = 12.sp,
            maxLines = 1
        )
    }
}