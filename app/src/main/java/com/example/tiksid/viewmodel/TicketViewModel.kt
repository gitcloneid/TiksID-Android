package com.example.tiksid.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tiksid.data.repository.MovieRepository
import com.example.tiksid.data.repository.ScheduleRepository
import com.example.tiksid.data.repository.TheaterRepository
import com.example.tiksid.data.repository.TransactionRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TicketViewModel(
    private val transactionRepository: TransactionRepository,
    private val scheduleRepository: ScheduleRepository,
    private val movieRepository: MovieRepository,
    private val theaterRepository: TheaterRepository,
    private val context: Context
) : ViewModel() {

    private val _tickets = mutableStateListOf<TicketUiModel>()
    val tickets: List<TicketUiModel> = _tickets

    init {
        fetchTickets()
    }

    private fun fetchTickets() {
        viewModelScope.launch {
            try {
                val user = transactionRepository.getUser() ?: return@launch
                val transactions = transactionRepository.getUserTransactions(user.id)
                val ticketList = mutableListOf<TicketUiModel>()
                for (transaction in transactions) {
                    val transactionDetails = transactionRepository.getTransactionDetails(transaction.id)
                    if (transactionDetails.isEmpty()) continue
                    val schedule = scheduleRepository.getSchedule(transaction.scheduleId) ?: continue
                    val movie = movieRepository.getMovie(schedule.movieId) ?: continue
                    val theater = theaterRepository.getTheater(schedule.theaterId) ?: continue

                    val inputDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val outputDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    val formattedDate = outputDateFormat.format(inputDateFormat.parse(schedule.date)!!)
                    val formattedTime = schedule.time.dropLast(3)

                    val seatsList = transactionDetails.map { it.seat }
                    val totalPrice = transactionDetails.sumOf { it.price }
                    val numberOfTickets = transactionDetails.size

                    ticketList.add(
                        TicketUiModel(
                            movieTitle = movie.title,
                            posterUrl = movie.poster,
                            date = formattedDate,
                            time = formattedTime,
                            seats = seatsList,
                            numberOfTickets = numberOfTickets,
                            price = totalPrice,
                            theaterName = theater.name,
                            genres = movie.genres, // Use the genres from the Movie object
                            duration = movie.duration
                        )
                    )
                }
                _tickets.clear()
                _tickets.addAll(ticketList)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

class TicketViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TicketViewModel::class.java)) {
            return TicketViewModel(
                transactionRepository = TransactionRepository(context),
                scheduleRepository = ScheduleRepository(context),
                movieRepository = MovieRepository(context),
                theaterRepository = TheaterRepository(context),
                context = context
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}