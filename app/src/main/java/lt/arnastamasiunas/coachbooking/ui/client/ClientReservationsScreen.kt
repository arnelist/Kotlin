package lt.arnastamasiunas.coachbooking.ui.client

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import lt.arnastamasiunas.coachbooking.data.AuthRepository
import lt.arnastamasiunas.coachbooking.data.ReservationRepository
import lt.arnastamasiunas.coachbooking.data.UserRepository
import lt.arnastamasiunas.coachbooking.model.Reservation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientReservationsScreen(
    authRepo: AuthRepository,
    reservationRepo: ReservationRepository,
    userRepo: UserRepository,
    onBack: () -> Unit
) {
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var reservations by remember { mutableStateOf<List<Reservation>>(emptyList()) }
    var trainerEmails by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    LaunchedEffect(Unit) {
        loading = true; error = null
        try {
            val clientId = authRepo.currentUid() ?: error("Neprisijungęs vartotojas")
            val res = reservationRepo.getClientReservations(clientId)
            reservations = res

            val uniqueTrainerIds = res.map { it.trainerId }.distinct().filter { it.isNotBlank() }
            val map = mutableMapOf<String, String>()
            for (tid in uniqueTrainerIds) {
                map[tid] = userRepo.getDisplayName(tid)
            }
            trainerEmails = map
        } catch (e: Exception) {
            error = e.message ?: "Nepavyko gauti rezervacijų"
        } finally {
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mano rezervacijos") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                }
                reservations.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Rezervacijų nėra")
                }
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(reservations) { r ->
                        val trainerEmail = trainerEmails[r.trainerId] ?: r.trainerId
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(14.dp)) {
                                Text("$trainerEmail", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(4.dp))
                                Text("${r.date}  ${r.start}-${r.end}")
                            }
                        }
                    }
                }
            }
        }
    }
}