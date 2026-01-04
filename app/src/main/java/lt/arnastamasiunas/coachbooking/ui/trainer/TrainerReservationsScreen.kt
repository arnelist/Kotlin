package lt.arnastamasiunas.coachbooking.ui.trainer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import lt.arnastamasiunas.coachbooking.data.AuthRepository
import lt.arnastamasiunas.coachbooking.data.ReservationRepository
import lt.arnastamasiunas.coachbooking.data.UserRepository
import lt.arnastamasiunas.coachbooking.model.Reservation
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainerReservationsScreen(
    authRepo: AuthRepository,
    reservationRepo: ReservationRepository,
    userRepo: UserRepository,
    onBack: () -> Unit
) {
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var reservations by remember { mutableStateOf<List<Reservation>>(emptyList()) }
    var clientEmails by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    val scope = rememberCoroutineScope()

    fun refresh() {
        scope.launch {
            loading = true
            error = null
            try {
                val trainerId = authRepo.currentUid() ?: error("Neprisijungęs treneris")
                val res = reservationRepo.getTrainerReservations(trainerId)
                reservations = res

                val uniqueClientIds = res.map { it.clientId }.distinct()
                val map = mutableMapOf<String, String>()
                for (cid in uniqueClientIds) {
                    map[cid] = userRepo.getDisplayName(cid)
                }
                clientEmails = map

            } catch (e: Exception) {
                error = e.message ?: "Nepavyko gauti rezervacijų"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { refresh() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mano rezervacijos") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Atgal") } }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(error!!, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { refresh() }) { Text("Bandyt dar kartą") }
                    }
                }

                reservations.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Rezervacijų nėra")
                }

                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(reservations) { r ->
                        val clientEmail = clientEmails[r.clientId] ?: r.clientId

                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(14.dp)) {
                                Text(clientEmail, style = MaterialTheme.typography.titleMedium)
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