package com.example.tiksid.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tiksid.ui.components.NetworkImage
import com.example.tiksid.viewmodel.TicketUiModel
import com.example.tiksid.viewmodel.TicketViewModel
import com.example.tiksid.viewmodel.TicketViewModelFactory
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TicketScreen(
    modifier: Modifier = Modifier,
    viewModel: TicketViewModel = viewModel(factory = TicketViewModelFactory(LocalContext.current))
) {
    var selectedTicket by remember { mutableStateOf<TicketUiModel?>(null) }

    if (selectedTicket == null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF0B0B15))
                .padding(16.dp)
        ) {
            Text(
                text = "Your Ticket",
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(viewModel.tickets) { ticket ->
                    TicketItem(
                        ticket = ticket,
                        onClick = { selectedTicket = ticket } // Set selected ticket on click
                    )
                }
            }
        }
    } else {
        TicketDetailScreen(
            ticket = selectedTicket!!,
            onBackClick = { selectedTicket = null } // Clear selected ticket to go back
        )
    }
}

@Composable
fun TicketItem(
    ticket: TicketUiModel,
    onClick: () -> Unit // Add click handler
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0B0B15), shape = RoundedCornerShape(8.dp))
            .padding(12.dp)
            .clickable { onClick() } // Make the item clickable
    ) {
        NetworkImage(
            url = ticket.posterUrl,
            contentDescription = "${ticket.movieTitle} poster",
            modifier = Modifier
                .size(width = 180.dp, height = 270.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp)
        ) {
            Text(
                text = ticket.movieTitle,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${ticket.numberOfTickets} ticket${if (ticket.numberOfTickets > 1) "s" else ""} • ${ticket.date} ${ticket.time}",
                color = Color(0xFFBBBBBB),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Seat:",
                color = Color(0xFFE8E2E2),
                fontSize = 14.sp
            )
            Text(
                text = ticket.seats.joinToString(" & "),
                color = Color(0xFFE8E2E2),
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Rp${NumberFormat.getNumberInstance(Locale("id", "ID")).format(ticket.price)}.-",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}