package lt.arnastamasiunas.coachbooking.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainerHomeScreen(
    greeting: String?,
    currentGymName: String?,
    canCreateTimeslots: Boolean,
    onSelectGym: () -> Unit,
    onCreateTimeslot: () -> Unit,
    onReservations: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Treneris") },
                actions = { TextButton(onClick = onLogout) { Text("Atsijungti") } }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            if (!greeting.isNullOrBlank()) {
                Text(
                    text = "Sveiki, $greeting",
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            Text(
                text = if (currentGymName.isNullOrBlank())
                    "Priskirta sporto salė: (nepriskirta)"
                else
                    "Current gym: $currentGymName",
                style = MaterialTheme.typography.bodyMedium
            )

            Button(onClick = onSelectGym, modifier = Modifier.fillMaxWidth()) {
                Text("Prisiskirti salę")
            }

            Button(
                onClick = onCreateTimeslot,
                enabled = canCreateTimeslots,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sukurti laiką")
            }

            Button(onClick = onReservations, modifier = Modifier.fillMaxWidth()) {
                Text("Mano rezervacijos")
            }

            if (!canCreateTimeslots) {
                Text(
                    "Pirma priskirk gym, kad galėtum kurti timeslotus.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}