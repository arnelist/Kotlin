package lt.arnastamasiunas.coachbooking.ui.trainer

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import lt.arnastamasiunas.coachbooking.data.AuthRepository
import lt.arnastamasiunas.coachbooking.data.TimeslotRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainerTimeslotCreatorScreen(
    authRepo: AuthRepository,
    timeslotRepo: TimeslotRepository,
    onBack: () -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    val today = remember { LocalDate.now() }
    val days = remember { (0..6).map { today.plusDays(it.toLong()) } }

    var selectedDay by remember { mutableStateOf(days.first()) }
    val dateStr = selectedDay.format(formatter)

    val times = remember { generateHalfHourTimes("06:00", "22:00") }
    var start by remember { mutableStateOf("09:00") }
    var end by remember { mutableStateOf("10:00") }

    var expandedStart by remember { mutableStateOf(false) }
    var expandedEnd by remember { mutableStateOf(false) }

    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    fun computeOrder(time: String): Long {
        // "HH:mm" -> minutes from 00:00
        val parts = time.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return (h * 60 + m).toLong()
    }

    fun isValidRange(s: String, e: String): Boolean {
        return computeOrder(e) > computeOrder(s)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Laikų kūrimas") },
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
                    FilterChip(
                        selected = day == selectedDay,
                        onClick = { selectedDay = day },
                        label = { Text(day.dayOfWeek.name.take(3) + " " + day.dayOfMonth) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("Data: $dateStr")

            Spacer(Modifier.height(16.dp))
            Text("Laikas", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                // START dropdown
                Box(Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { expandedStart = true },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Pradžia: $start") }

                    DropdownMenu(expanded = expandedStart, onDismissRequest = { expandedStart = false }) {
                        times.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t) },
                                onClick = {
                                    start = t
                                    expandedStart = false
                                    if (!isValidRange(start, end)) {
                                        end = nextTime(times, start) ?: end
                                    }
                                }
                            )
                        }
                    }
                }

                // END dropdown
                Box(Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { expandedEnd = true },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Pabaiga: $end") }

                    DropdownMenu(expanded = expandedEnd, onDismissRequest = { expandedEnd = false }) {
                        times.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t) },
                                onClick = {
                                    end = t
                                    expandedEnd = false
                                }
                            )
                        }
                    }
                }
            }

            val valid = isValidRange(start, end)
            if (!valid) {
                Spacer(Modifier.height(8.dp))
                Text("End turi būti vėliau nei Start.", color = MaterialTheme.colorScheme.error)
            }

            message?.let {
                Spacer(Modifier.height(10.dp))
                Text(it)
            }
            error?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { loading = true },
                enabled = !loading && valid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (loading) "Kuriama..." else "Sukurti timeslot")
            }

            LaunchedEffect(loading) {
                if (!loading) return@LaunchedEffect
                message = null
                error = null
                try {
                    val trainerId = authRepo.currentUid() ?: error("Neprisijungęs treneris")
                    val order = computeOrder(start)

                    timeslotRepo.createTimeslot(
                        trainerId = trainerId,
                        date = dateStr,
                        start = start,
                        end = end,
                        order = order
                    )
                    message = "Sukurta: $dateStr $start-$end"
                } catch (e: Exception) {
                    error = e.message ?: "Nepavyko sukurti laiko"
                } finally {
                    loading = false
                }
            }
        }
    }
}

private fun generateHalfHourTimes(from: String, to: String): List<String> {
    fun toMinutes(s: String): Int {
        val p = s.split(":")
        return (p[0].toInt() * 60) + p[1].toInt()
    }
    fun fromMinutes(m: Int): String {
        val h = (m / 60).toString().padStart(2, '0')
        val mm = (m % 60).toString().padStart(2, '0')
        return "$h:$mm"
    }

    val start = toMinutes(from)
    val end = toMinutes(to)
    val out = mutableListOf<String>()
    var cur = start
    while (cur <= end) {
        out.add(fromMinutes(cur))
        cur += 30
    }
    return out
}

private fun nextTime(times: List<String>, after: String): String? {
    val idx = times.indexOf(after)
    return if (idx >= 0 && idx + 1 < times.size) times[idx + 1] else null
}