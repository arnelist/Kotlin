package lt.arnastamasiunas.coachbooking.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import lt.arnastamasiunas.coachbooking.data.AuthRepository
import lt.arnastamasiunas.coachbooking.data.ReservationRepository
import lt.arnastamasiunas.coachbooking.data.TimeslotRepository
import lt.arnastamasiunas.coachbooking.model.Timeslot
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    trainerId: String,
    trainerEmail: String,
    authRepo: AuthRepository,
    timeslotRepo: TimeslotRepository,
    reservationRepo: ReservationRepository,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    val today = remember { LocalDate.now() }
    val days = remember { (0..6).map { today.plusDays(it.toLong()) } }

    var selectedDay by remember { mutableStateOf(days.first()) }
    var timeslots by remember { mutableStateOf<List<Timeslot>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var slotToBook by remember { mutableStateOf<Timeslot?>(null) }
    var bookingSlot by remember { mutableStateOf<Timeslot?>(null) }
    var bookingLoading by remember { mutableStateOf(false) }
    var toast by remember { mutableStateOf<String?>(null) }

    fun selectedDateStr() = selectedDay.format(formatter)

    suspend fun loadTimeslots() {
        loading = true
        error = null
        try {
            timeslots = timeslotRepo.getTimeslots(trainerId, selectedDateStr())
        } catch (e: Exception) {
            error = e.message ?: "Nepavyko gauti laikų"
        } finally {
            loading = false
        }
    }

    LaunchedEffect(trainerId, selectedDay) { loadTimeslots() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(trainerEmail.ifBlank { "Booking" }) },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                days.forEach { day ->
                    val selected = day == selectedDay
                    FilterChip(
                        selected = selected,
                        onClick = { selectedDay = day },
                        label = { Text(day.dayOfWeek.name.take(3) + " " + day.dayOfMonth) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("Data: ${selectedDateStr()}", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(12.dp))

            when {
                loading -> Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                error != null -> Column {
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { /* reload */ }) { Text("Bandyt dar kartą") }
                }
                timeslots.isEmpty() -> Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text("Nėra laisvų laikų šiai dienai")
                }
                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        timeslots.forEach { slot ->
                            val disabled = slot.isBooked || bookingLoading

                            Text("${slot.start} - ${slot.end}", style = MaterialTheme.typography.titleMedium)

                            if (slot.isBooked) Text("Užimta") else Text("Rezervuoti")

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !disabled) { bookingSlot = slot }
                            ) {
                                Row(
                                    Modifier.fillMaxWidth().padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${slot.start} - ${slot.end}", style = MaterialTheme.typography.titleMedium)
                                    Text(if (slot.isBooked) "Užimta" else "Rezervuoti")
                                }
                            }

                            LaunchedEffect(slotToBook) {
                                val slot = slotToBook ?: return@LaunchedEffect
                                bookingLoading = true
                                error = null
                                try {
                                    val clientId = authRepo.currentUid() ?: error("Neprisijungęs vartotojas")
                                    reservationRepo.createReservationTransactional(
                                        clientId = clientId,
                                        trainerId = trainerId,
                                        date = selectedDateStr(),
                                        start = slot.start,
                                        end = slot.end,
                                        timeslotId = slot.id
                                    )
                                    loadTimeslots()
                                } catch (e: Exception) {
                                    error = e.message ?: "Rezervacija nepavyko"
                                } finally {
                                    bookingLoading = false
                                    slotToBook = null
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (bookingSlot != null) {
        val s = bookingSlot!!

        AlertDialog(
            onDismissRequest = {
                if (!bookingLoading) bookingSlot = null
            },
            title = { Text("Patvirtinti rezervaciją?") },
            text = { Text("${selectedDateStr()}  ${s.start} - ${s.end}") },
            confirmButton = {
                TextButton(
                    enabled = !bookingLoading,
                    onClick = {
                        bookingLoading = true
                        scope.launch {
                            try {
                                val clientId = authRepo.currentUid()
                                    ?: error("Neprisijungęs vartotojas")

                                reservationRepo.createReservationTransactional(
                                    clientId = clientId,
                                    trainerId = trainerId,
                                    date = selectedDateStr(),
                                    start = s.start,
                                    end = s.end,
                                    timeslotId = s.id
                                )

                                loadTimeslots()
                            } finally {
                                bookingLoading = false
                                bookingSlot = null
                            }
                        }
                    }
                ) {
                    Text(if (bookingLoading) "Rezervuojama..." else "Patvirtinti")
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !bookingLoading,
                    onClick = { bookingSlot = null }
                ) {
                    Text("Atšaukti")
                }
            }
        )
    }
}