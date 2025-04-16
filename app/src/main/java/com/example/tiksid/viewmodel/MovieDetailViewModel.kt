package com.example.tiksid.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tiksid.data.models.*
import com.example.tiksid.data.repository.GenreRepository
import com.example.tiksid.data.repository.ScheduleRepository
import com.example.tiksid.data.repository.TheaterRepository
import com.example.tiksid.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MovieDetailViewModel(
    private val movie: Movie,
    private val genreRepository: GenreRepository,
    private val theaterRepository: TheaterRepository,
    private val scheduleRepository: ScheduleRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _genres = MutableStateFlow<List<Genre>>(emptyList())
    val genres: StateFlow<List<Genre>> = _genres.asStateFlow()

    private val _theaters = MutableStateFlow<List<Theater>>(emptyList())
    val theaters: StateFlow<List<Theater>> = _theaters.asStateFlow()

    private val _schedules = MutableStateFlow<List<Schedule>>(emptyList())
    val schedules: StateFlow<List<Schedule>> = _schedules.asStateFlow()

    private val _selectedTheater = MutableStateFlow<Theater?>(null)
    val selectedTheater: StateFlow<Theater?> = _selectedTheater.asStateFlow()

    private val _selectedDate = MutableStateFlow<String?>(null)
    val selectedDate: StateFlow<String?> = _selectedDate.asStateFlow()

    private val _selectedTime = MutableStateFlow<String?>(null)
    val selectedTime: StateFlow<String?> = _selectedTime.asStateFlow()

    private val _selectedSeats = MutableStateFlow<List<String>>(emptyList())
    val selectedSeats: StateFlow<List<String>> = _selectedSeats.asStateFlow()

    private val _bookedSeats = MutableStateFlow<List<String>>(emptyList())
    val bookedSeats: StateFlow<List<String>> = _bookedSeats.asStateFlow()

    private val _totalPrice = MutableStateFlow(0)
    val totalPrice: StateFlow<Int> = _totalPrice.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Load genres
                val movieGenres = genreRepository.getGenresForMovie(movie.id).sortedBy { it.name }
                _genres.value = movieGenres

                // Load theaters for the movie
                val theaters = theaterRepository.getTheatersByMovie(movie.id)
                _theaters.value = theaters

                // Load schedules for the movie
                val schedules = scheduleRepository.getSchedulesByMovie(movie.id)
                _schedules.value = schedules

                // Set default selections
                _selectedTheater.value = theaters.firstOrNull()
                _selectedDate.value = schedules.firstOrNull()?.date
                _selectedTime.value = schedules.firstOrNull()?.time

                // Load booked seats for the default schedule
                updateBookedSeats()
            } catch (e: Exception) {
                Log.e("MovieDetailViewModel", "Error loading data", e)
                _error.value = "Failed to load data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun updateBookedSeats() {
        viewModelScope.launch {
            val schedule = _schedules.value.find {
                it.date == _selectedDate.value && it.time == _selectedTime.value
            }
            if (schedule != null) {
                val bookedSeats = transactionRepository.getBookedSeatsForSchedule(schedule.id)
                _bookedSeats.value = bookedSeats
            } else {
                _bookedSeats.value = emptyList()
            }
        }
    }

    fun selectTheater(theater: Theater) {
        _selectedTheater.value = theater
        _selectedSeats.value = emptyList() // Reset seats when theater changes
        calculateTotalPrice()
    }

    fun selectDate(date: String) {
        _selectedDate.value = date
        _selectedSeats.value = emptyList() // Reset seats when date changes
        updateBookedSeats() // Update booked seats for new schedule
        calculateTotalPrice()
    }

    fun selectTime(time: String) {
        _selectedTime.value = time
        _selectedSeats.value = emptyList() // Reset seats when time changes
        updateBookedSeats() // Update booked seats for new schedule
        calculateTotalPrice()
    }

    fun toggleSeat(seat: String) {
        if (_bookedSeats.value.contains(seat)) return // Prevent selecting booked seats
        val currentSeats = _selectedSeats.value.toMutableList()
        if (currentSeats.contains(seat)) {
            currentSeats.remove(seat)
        } else {
            currentSeats.add(seat)
        }
        _selectedSeats.value = currentSeats
        calculateTotalPrice()
    }

    private fun calculateTotalPrice() {
        val schedule = _schedules.value.find {
            it.date == _selectedDate.value && it.time == _selectedTime.value
        }
        val pricePerSeat = schedule?.price ?: 0
        _totalPrice.value = pricePerSeat * _selectedSeats.value.size
    }

    fun buyTickets(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val user = transactionRepository.getUser() ?: return@launch
                val schedule = _schedules.value.find {
                    it.date == _selectedDate.value && it.time == _selectedTime.value
                } ?: return@launch

                // Create transaction
                val transactionId = transactionRepository.createTransaction(
                    userId = user.id,
                    scheduleId = schedule.id,
                    transactionDate = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
                ) ?: return@launch

                // Create transaction details for each seat
                _selectedSeats.value.forEach { seat ->
                    transactionRepository.createTransactionDetail(
                        transactionId = transactionId,
                        seat = seat,
                        price = schedule.price
                    )
                }

                onSuccess()
            } catch (e: Exception) {
                Log.e("MovieDetailViewModel", "Error buying tickets", e)
                _error.value = "Failed to buy tickets: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    class Factory(
        private val movie: Movie,
        private val genreRepository: GenreRepository,
        private val theaterRepository: TheaterRepository,
        private val scheduleRepository: ScheduleRepository,
        private val transactionRepository: TransactionRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MovieDetailViewModel::class.java)) {
                return MovieDetailViewModel(movie, genreRepository, theaterRepository, scheduleRepository, transactionRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}