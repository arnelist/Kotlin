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
import lt.arnastamasiunas.coachbooking.data.GymRepository
import lt.arnastamasiunas.coachbooking.model.Gym

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymsScreen(
    gymRepo: GymRepository,
    onLogout: () -> Unit,
    onGymClick: (gymId: String, gymName: String) -> Unit,
    onReservations: () -> Unit
) {
    var gyms by remember { mutableStateOf<List<Gym>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        loading = true; error = null
        try { gyms = gymRepo.getGyms() }
        catch (e: Exception) { error = e.message ?: "Nepavyko gauti gym'ų" }
        finally { loading = false }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gym'ai") },
                actions = {
                    TextButton(onClick = onReservations) { Text("Rezervacijos") }
                    TextButton(onClick = onLogout) { Text("Logout") } }
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
                gyms.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Kol kas gym'ų nėra")
                }
                else -> LazyColumn(
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
                                    Text(g.address, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}