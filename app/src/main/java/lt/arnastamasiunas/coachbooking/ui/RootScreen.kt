package lt.arnastamasiunas.coachbooking.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.navigation.NavHostController
import lt.arnastamasiunas.coachbooking.data.AuthRepository
import lt.arnastamasiunas.coachbooking.data.UserRepository
import lt.arnastamasiunas.coachbooking.navigation.Routes

@Composable
fun RootScreen (
    navController: NavHostController,
    authRepo: AuthRepository,
    userRepo: UserRepository
) {
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val uid = authRepo.currentUid()
        if (uid == null) {
            navController.navigate(Routes.LOGIN) { popUpTo(0) }
        } else {
            val role = userRepo.getRole(uid).trim().lowercase()
            navController.navigate(
                if (role === "trainer") Routes.TRAINER_HOME else Routes.GYMS
            ) { popUpTo(0) }
        }
        loading = false
    }

    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}