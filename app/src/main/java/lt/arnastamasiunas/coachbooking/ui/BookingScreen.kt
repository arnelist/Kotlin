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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.PaddingValues
import java.time.format.TextStyle
import java.util.Locale

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
    val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    val today = remember { LocalDate.now() }
    val days = remember { (0..6).map { today.plusDays(it.toLong()) } }

    var selectedDay by remember { mutableStateOf(days.first()) }
    var timeslots by remember { mutableStateOf<List<Timeslot>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    var bookingSlot by remember { mutableStateOf<Timeslot?>(null) }
    var bookingLoading by remember { mutableStateOf(false) }

    var showBooked by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun selectedDateStr() = selectedDay.format(formatter)

    val ltLocale = remember { Locale("lt", "LT") }
    val weekday = selectedDay.dayOfWeek
        .getDisplayName(TextStyle.SHORT, ltLocale)
        .replaceFirstChar { it.uppercaseChar() }

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

    val visibleSlots = remember(timeslots, showBooked, bookingLoading) {
        val list = if (showBooked) timeslots else timeslots.filter { it.status != "booked" }
        list
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(trainerEmail.ifBlank { "Rezervacija" }) },
                navigationIcon = { TextButton(onClick = onBack) { Text("Atgal") } }
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
                        label = {
                            Text(
                                day.dayOfWeek
                                    .getDisplayName(TextStyle.SHORT, ltLocale)
                                    .replaceFirstChar { it.uppercaseChar() } + " " + day.dayOfMonth
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("Data: ${selectedDateStr()} (${weekday})")
            Spacer(Modifier.height(8.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Rodyti užimtus", style = MaterialTheme.typography.bodyMedium)
                Switch(
                    checked = showBooked,
                    onCheckedChange = { showBooked = it }
                )
            }

            Spacer(Modifier.height(12.dp))

            when {
                loading -> {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                error != null -> {
                    Column(
                        Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(error!!, color = MaterialTheme.colorScheme.error)
                        Button(
                            onClick = { scope.launch { loadTimeslots() } }
                        ) {
                            Text("Bandyt dar kartą")
                        }
                    }
                }

                visibleSlots.isEmpty() -> {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Šiai dienai laikų nėra")
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(items = visibleSlots) { slot: Timeslot ->
                            val isBooked = slot.status == "booked"
                            val disabled = isBooked || bookingLoading

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !disabled) {
                                        bookingSlot = slot
                                    }
                            ) {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "${slot.start} - ${slot.end}",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(if (isBooked) "Užimta" else "Rezervuoti")
                                }
                            }
                        }
                    }
                }
            }

            if (bookingSlot != null) {
                val s = bookingSlot!!

                AlertDialog(
                    onDismissRequest = { if (!bookingLoading) bookingSlot = null },
                    title = { Text("Patvirtinti rezervaciją?") },
                    text = { Text("${selectedDateStr()}  ${s.start}-${s.end}") },
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

                                        snackbarHostState.showSnackbar("Rezervuota ✅")
                                        loadTimeslots()
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar(
                                            e.message ?: "Rezervacija nepavyko"
                                        )
                                    } finally {
                                        bookingLoading = false
                                        bookingSlot = null
                                    }
                                }
                            }
                        ) { Text(if (bookingLoading) "Rezervuojama..." else "Taip") }
                    },
                    dismissButton = {
                        TextButton(
                            enabled = !bookingLoading,
                            onClick = { bookingSlot = null }
                        ) { Text("Ne") }
                    }
                )
            }
        }
    }
}