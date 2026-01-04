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
import lt.arnastamasiunas.coachbooking.data.UserRepository
import lt.arnastamasiunas.coachbooking.model.TrainerUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymTrainersScreen(
    gymName: String,
    gymId: String,
    userRepo: UserRepository,
    onBack: () -> Unit,
    onTrainerClick: (trainerId: String, trainerEmail: String) -> Unit
) {
    var trainers by remember { mutableStateOf<List<TrainerUser>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(gymId) {
        loading = true; error = null
        try { trainers = userRepo.getTrainersByGym(gymId) }
        catch (e: Exception) { error = e.message ?: "Nepavyko gauti trenerių" }
        finally { loading = false }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(gymName) },
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
                trainers.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Šitam gym'e trenerių nėra")
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
                                Text(t.email, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}