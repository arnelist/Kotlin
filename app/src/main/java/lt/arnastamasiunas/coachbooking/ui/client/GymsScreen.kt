package lt.arnastamasiunas.coachbooking.ui.client

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import lt.arnastamasiunas.coachbooking.data.GymRepository
import lt.arnastamasiunas.coachbooking.model.Gym
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymsScreen(
    greeting: String?,
    gymRepo: GymRepository,
    onLogout: () -> Unit,
    onGymClick: (gymId: String, gymName: String) -> Unit,
    onReservations: () -> Unit
) {
    var gyms by remember { mutableStateOf<List<Gym>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun refresh() {
        scope.launch {
            loading = true
            error = null
            try {
                gyms = gymRepo.getGyms()
            } catch (e: Exception) {
                error = e.message ?: "Nepavyko gauti gymų"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { refresh() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sporto salės") },
                actions = {
                    TextButton(onClick = onReservations) { Text("Rezervacijos") }
                    TextButton(onClick = onLogout) { Text("Atsijungti") } }
            )
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
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

                gyms.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Kol kas gym'ų nėra")
                }

                else -> {
                    Column(
                        Modifier.fillMaxSize()
                    ) {

                        if (!greeting.isNullOrBlank()) {
                            Text(
                                text = "Sveiki, $greeting",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(gyms) { g ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onGymClick(g.id, g.name) }
                                ) {
                                    Column(Modifier.padding(14.dp)) {
                                        Text(g.name, style = MaterialTheme.typography.titleMedium)
                                        if (g.address.isNotBlank()) {
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                g.address,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}