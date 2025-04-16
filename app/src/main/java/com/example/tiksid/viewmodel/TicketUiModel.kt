package com.example.tiksid.viewmodel

data class TicketUiModel(
    val movieTitle: String,
    val posterUrl: String,
    val date: String,
    val time: String,
    val seats: List<String>,
    val numberOfTickets: Int,
    val price: Int,
    val theaterName: String,
    val genres: List<String>,
    val duration: Int
) {
    fun getFormattedGenres(): String {
        return genres.joinToString(" • ") // Format genres for display, e.g., "Drama • Adventure"
    }
}