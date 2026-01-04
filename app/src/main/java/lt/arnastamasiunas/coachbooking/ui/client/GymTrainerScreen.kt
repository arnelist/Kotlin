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
import lt.arnastamasiunas.coachbooking.data.UserRepository
import lt.arnastamasiunas.coachbooking.model.TrainerUser
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import lt.arnastamasiunas.coachbooking.ui.components.pressScaleClickable

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
    val scope = rememberCoroutineScope()

    fun refresh() {
        scope.launch {
            loading = true
            error = null
            try {
                trainers = userRepo.getTrainersByGym(gymId)
            } catch (e: Exception) {
                error = e.message ?: "Nepavyko gauti trenerių"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(gymId) { refresh() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(gymName) },
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

                trainers.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Šitam gym’e trenerių nėra")
                }

                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(trainers) { t ->

                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(220)) + slideInVertically(tween(220)) { it/6 }
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .pressScaleClickable { onTrainerClick(t.uid, t.email) },
                                shape = MaterialTheme.shapes.large
                            ) {
                                Column(Modifier.padding(14.dp)) {
                                    val name = t.fullName.ifBlank { t.email }
                                    Text(name, style = MaterialTheme.typography.titleMedium)
                                    if (t.fullName.isNotBlank()) {
                                        Spacer(Modifier.height(4.dp))
                                        Text(t.email, style = MaterialTheme.typography.bodySmall)
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