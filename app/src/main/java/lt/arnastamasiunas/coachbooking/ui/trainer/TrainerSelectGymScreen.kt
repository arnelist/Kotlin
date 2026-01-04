package lt.arnastamasiunas.coachbooking.ui.trainer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import lt.arnastamasiunas.coachbooking.data.AuthRepository
import lt.arnastamasiunas.coachbooking.data.GymRepository
import lt.arnastamasiunas.coachbooking.data.UserRepository
import lt.arnastamasiunas.coachbooking.model.Gym

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainerSelectGymScreen(
    authRepo: AuthRepository,
    gymRepo: GymRepository,
    userRepo: UserRepository,
    onBack: () -> Unit
) {
    var gyms by remember { mutableStateOf<List<Gym>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var selectedGymToSave by remember { mutableStateOf<Gym?>(null) }

    LaunchedEffect(Unit) {
        loading = true
        error = null
        try {
            gyms = gymRepo.getGyms()
        } catch (e: Exception) {
            error = e.message ?: "Nepavyko gauti gym'ų"
        } finally {
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pasirink gym") },
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
                else -> Column(Modifier.fillMaxSize()) {
                    message?.let {
                        Text(it, modifier = Modifier.padding(16.dp))
                    }

                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(gyms) { g ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !saving) {
                                        saving = true
                                        message = null
                                        error = null
                                        val uid = authRepo.currentUid()
                                        if (uid == null) {
                                            error = "Neprisijungęs treneris"
                                            saving = false
                                        } else {
                                            selectedGymToSave = g
                                        }
                                    }
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

    LaunchedEffect(selectedGymToSave) {
        val g = selectedGymToSave ?: return@LaunchedEffect
        try {
            val uid = authRepo.currentUid() ?: error("Neprisijungęs treneris")
            userRepo.setGym(uid, g.id)
            message = "Priskirta: ${g.name}"
            onBack()
        } catch (e: Exception) {
            error = e.message ?: "Nepavyko priskirti gym"
        } finally {
            saving = false
            selectedGymToSave = null
        }
    }
}