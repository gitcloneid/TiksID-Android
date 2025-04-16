package com.example.tiksid.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tiksid.data.models.Movie
import com.example.tiksid.data.repository.GenreRepository
import com.example.tiksid.data.repository.ScheduleRepository
import com.example.tiksid.data.repository.TheaterRepository
import com.example.tiksid.data.repository.TransactionRepository
import com.example.tiksid.ui.components.NetworkImage
import com.example.tiksid.viewmodel.MovieDetailViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    movie: Movie,
    onBack: () -> Unit,
    onBuyTicketSuccess: () -> Unit
) {
    val context = LocalContext.current
    val genreRepository = remember { GenreRepository(context) }
    val theaterRepository = remember { TheaterRepository(context) }
    val scheduleRepository = remember { ScheduleRepository(context) }
    val transactionRepository = remember { TransactionRepository(context) }

    val viewModel: MovieDetailViewModel = viewModel(
        factory = MovieDetailViewModel.Factory(
            movie,
            genreRepository,
            theaterRepository,
            scheduleRepository,
            transactionRepository
        )
    )

    val genres by viewModel.genres.collectAsState()
    val theaters by viewModel.theaters.collectAsState()
    val schedules by viewModel.schedules.collectAsState()
    val selectedTheater by viewModel.selectedTheater.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedTime by viewModel.selectedTime.collectAsState()
    val selectedSeats by viewModel.selectedSeats.collectAsState()
    val bookedSeats by viewModel.bookedSeats.collectAsState()
    val totalPrice by viewModel.totalPrice.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // format price
    val formattedPrice = NumberFormat.getNumberInstance(Locale("id", "ID")).format(totalPrice).let {
        "Rp$it.-"
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {}
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0B15))
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFFFFC93C)
            )
        } else if (error != null) {
            Text(
                text = error ?: "Unknown error",
                color = Color.Red,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .align(Alignment.Start)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                NetworkImage(
                    url = movie.poster,
                    contentDescription = movie.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2f / 3f)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = movie.title,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${movie.duration} minutes • ${movie.releaseDate}",
                    color = Color.LightGray,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Genres
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(genres) { genre ->
                        Text(
                            text = genre.name,
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .background(
                                    Color(0xFF1A1A2E),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = movie.description,
                    color = Color.White,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                //theater
                if (theaters.isEmpty()) {
                    Text(
                        text = "No available theaters for this movie",
                        color = Color.Red,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1A1A2E), RoundedCornerShape(8.dp))
                            .padding(16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                } else {
                    Text(
                        text = "THEATER",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextField(
                            value = selectedTheater?.name ?: "Select Theater",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = TextFieldDefaults.textFieldColors(
                                containerColor = Color(0xFF1A1A2E),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledTextColor = Color.White,
                                focusedTextColor = Color.White
                            ),
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            }
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            theaters.forEach { theater ->
                                DropdownMenuItem(
                                    text = { Text(theater.name, color = Color.White) },
                                    onClick = {
                                        viewModel.selectTheater(theater)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (schedules.isEmpty()) {
                        Text(
                            text = "Stay tuned for the movie schedule update",
                            color = Color.Red,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1A1A2E), RoundedCornerShape(8.dp))
                                .padding(16.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    } else {
                        Text(
                            text = "Date",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val availableDates = schedules.map { it.date }.distinct()
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(availableDates) { date ->
                                val isSelected = date == selectedDate
                                Text(
                                    text = LocalDate.parse(date).format(DateTimeFormatter.ofPattern("dd MMM")),
                                    color = if (isSelected) Color.Black else Color.White,
                                    modifier = Modifier
                                        .background(
                                            if (isSelected) Color(0xFFFFC93C) else Color(0xFF1A1A2E),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                        .clickable { viewModel.selectDate(date) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Available Time",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val availableTimes = schedules.filter { it.date == selectedDate }.map { it.time }.distinct()
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(availableTimes) { time ->
                                val isSelected = time == selectedTime
                                Text(
                                    text = time.dropLast(3),
                                    color = if (isSelected) Color.Black else Color.White,
                                    modifier = Modifier
                                        .background(
                                            if (isSelected) Color(0xFFFFC93C) else Color(0xFF1A1A2E),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                        .clickable { viewModel.selectTime(time) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // seat
                        selectedTheater?.let { theater ->
                            Text(
                                text = "Choose Seat",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .nestedScroll(nestedScrollConnection)
                            ) {
                                items(theater.section) { sectionIndex ->
                                    // untuk setiap bagian, hitung huruf kolom awal
                                    // karena kolom = 12, setiap bagian akan memiliki 12 kolom (A hingga L)
                                    // jika bagian > 1, kita lanjutkan hurufnya (mis., bagian 1: A hingga L, bagian 2: M hingga X, dst.)
                                    val columnsPerSection = theater.column
                                    val startColumnIndex = sectionIndex * columnsPerSection
                                    val endColumnIndex = minOf(startColumnIndex + columnsPerSection, 26) //limit ke huruf z

                                    if (startColumnIndex >= 26) return@items

                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        for (col in startColumnIndex until endColumnIndex) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                for (row in 1..theater.row) {
                                                    val seat = "${('A' + col)}$row"
                                                    val isSelected = selectedSeats.contains(seat)
                                                    val isUnavailable = bookedSeats.contains(seat)

                                                    Box(
                                                        modifier = Modifier
                                                            .size(40.dp)
                                                            .border(1.dp, Color.Transparent, RoundedCornerShape(4.dp))
                                                            .background(
                                                                when {
                                                                    isUnavailable -> Color.Transparent
                                                                    isSelected -> Color(0xFFFFC93C)
                                                                    else -> Color(0xFF1A2A44)
                                                                },
                                                                RoundedCornerShape(4.dp)
                                                            )
                                                            .clickable(enabled = !isUnavailable) {
                                                                viewModel.toggleSeat(seat)
                                                            },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = seat,
                                                            color = if (isSelected) Color.Black else if (isUnavailable) Color.Gray else Color.White,
                                                            fontSize = 12.sp
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Check date if expired
                        val selectedSchedule = schedules.find {
                            it.date == selectedDate && it.time == selectedTime
                        }
                        val isPastDate = selectedSchedule?.let {
                            LocalDate.parse(it.date).isBefore(LocalDate.now())
                        } ?: true

                        Button(
                            onClick = { viewModel.buyTickets(onBuyTicketSuccess) },
                            enabled = selectedSeats.isNotEmpty() && !isPastDate,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPastDate) Color.Gray else Color(0xFFFFC93C)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (isPastDate) "Schedule Expired" else "Buy Ticket $formattedPrice",
                                color = if (isPastDate) Color.White else Color.Black,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}