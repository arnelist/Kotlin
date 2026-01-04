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
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import lt.arnastamasiunas.coachbooking.ui.components.pressScaleClickable

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
    var cancelTarget by remember { mutableStateOf<Reservation?>(null) }
    var cancelLoading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun refresh() {
        scope.launch {
            loading = true
            error = null
            try {
                val clientId = authRepo.currentUid() ?: error("Neprisijungęs vartotojas")
                val res = reservationRepo.getClientReservations(clientId)
                reservations = res

                val uniqueTrainerIds = res.map { it.trainerId }.distinct()
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
    }

    LaunchedEffect(Unit) { refresh() }

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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(error!!, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { refresh() }) { Text("Bandyti dar kartą") }
                    }
                }

                reservations.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Rezervacijų nėra")
                }

                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {

                        LazyColumn(
                            contentPadding = PaddingValues(all = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(reservations) { r ->

                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn(tween(220)) + slideInVertically(tween(220)) { it/6 }
                                ) {

                                    Card(
                                        Modifier.fillMaxWidth(),
                                        shape = MaterialTheme.shapes.large
                                    ) {
                                        Column(Modifier.padding(14.dp)) {
                                            val trainerEmail = trainerEmails[r.trainerId] ?: r.trainerId
                                            Text(
                                                trainerEmail,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            Text("${r.date}  ${r.start}-${r.end}")

                                            Spacer(Modifier.height(10.dp))
                                            OutlinedButton(
                                                onClick = { cancelTarget = r },
                                                enabled = !cancelLoading && r.timeslotId.isNotBlank()
                                            ) {
                                                Text("Atšaukti")
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (cancelTarget != null) {
                            val r = cancelTarget!!

                            AlertDialog(
                                onDismissRequest = { if (!cancelLoading) cancelTarget = null },
                                title = { Text("Atšaukti rezervaciją?") },
                                text = { Text("${r.date} ${r.start}-${r.end}") },
                                confirmButton = {
                                    TextButton(
                                        enabled = !cancelLoading,
                                        onClick = {
                                            cancelLoading = true
                                            scope.launch {
                                                try {
                                                    reservationRepo.cancelReservation(
                                                        reservationId = r.id,
                                                        timeslotId = r.timeslotId
                                                    )

                                                    val clientId = authRepo.currentUid()
                                                        ?: error("Neprisijungęs vartotojas")

                                                    refresh()
                                                } catch (e: Exception) {
                                                    scope.launch { snackbarHostState.showSnackbar(e.message ?: "Nepavyko atšaukti") }
                                                } finally {
                                                    cancelLoading = false
                                                    cancelTarget = null
                                                }
                                            }
                                        }
                                    ) { Text(if (cancelLoading) "Atšaukiama..." else "Taip") }
                                },
                                dismissButton = {
                                    TextButton(
                                        enabled = !cancelLoading,
                                        onClick = { cancelTarget = null }
                                    ) { Text("Ne") }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}