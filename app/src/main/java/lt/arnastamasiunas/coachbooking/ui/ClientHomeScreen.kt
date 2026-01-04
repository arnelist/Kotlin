package lt.arnastamasiunas.coachbooking.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import lt.arnastamasiunas.coachbooking.data.UserRepository
import lt.arnastamasiunas.coachbooking.model.TrainerUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientHomeScreen(
    userRepo: UserRepository,
    onLogout: () -> Unit,
    onTrainerClick: (trainerId: String, trainerEmail: String) -> Unit = {_, _ -> }
) {
    var trainers by remember { mutableStateOf<List<TrainerUser>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableIntStateOf(0) }

    suspend fun fetch() {
        loading = true
        error = null
        try {
            trainers = userRepo.getTrainers()
        } catch (e: Exception) {
            error = e.message ?: "Nepavyko gauti trenerių"
        } finally {
            loading = false
        }
    }

    LaunchedEffect(refreshKey) { fetch() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Treneriai") },
                actions = {
                    TextButton(onClick = { refreshKey++ }) { Text("Perkrauti") }
                    TextButton(onClick = onLogout) { Text("Atsijungti") }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                error != null -> Column(Modifier.padding(16.dp)) {
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { refreshKey++ }) { Text("Bandyt dar kartą") }
                }
                trainers.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Kol kas trenerių nėra")
                }
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(trainers) { t ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onTrainerClick(t.uid, t.email) }
                        ) {
                            Column(Modifier.padding(14.dp)) {
                                Text(
                                    t.email.ifBlank { "Treneris" },
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(Modifier.height(4.dp))
                                Text("ID: ${t.uid}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}