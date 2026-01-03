package lt.arnastamasiunas.coachbooking.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainerHomeScreen(
    onCreateTimeslot: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trainer") },
                actions = { TextButton(onClick = onLogout) { Text("Logout") } }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("TRAINER HOME")
            Button(onClick = onCreateTimeslot, modifier = Modifier.fillMaxWidth()) {
                Text("Create Timeslot")
            }
        }
    }
}