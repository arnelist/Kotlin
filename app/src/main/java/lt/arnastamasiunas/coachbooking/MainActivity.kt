package lt.arnastamasiunas.coachbooking

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.navigation.compose.*
import lt.arnastamasiunas.coachbooking.data.AuthRepository
import lt.arnastamasiunas.coachbooking.data.UserRepository
import lt.arnastamasiunas.coachbooking.ui.auth.LoginScreen
import lt.arnastamasiunas.coachbooking.ui.auth.RegisterScreen
import lt.arnastamasiunas.coachbooking.ui.theme.CoachBookingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CoachBookingTheme {
                val navController = rememberNavController()
                val authRepo = remember { AuthRepository() }
                val userRepo = remember { UserRepository() }

                NavHost(
                    navController = navController,
                    startDestination = "login"
                ) {
                    composable("login") {
                        LoginScreen(
                            onLogin = {email, password ->
                                authRepo.login(email, password)
                                val uid = authRepo.currentUid()!!
                                val role = userRepo.getRole(uid)

                                navController.navigate(
                                    if (role === "trainer") "trainer_home" else "client_home"
                                ) {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onGoRegister = {
                                navController.navigate("register")
                            }
                        )
                    }

                    composable("register") {
                        RegisterScreen(
                            onRegister = {email, password, role ->
                                val uid = authRepo.register(email, password)
                                userRepo.createUser(uid, email, role)

                                navController.navigate(
                                    if (role === "trainer") "trainer_home" else "client_home"
                                ) {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onGoLogin = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable("client_home") {
                        //Temp stub
                        androidx.compose.material3.Surface(
                            modifier = androidx.compose.ui.Modifier.fillMaxSize()
                        ) {
                            androidx.compose.material3.Text("CLIENT_HOME")
                        }
                    }

                    composable("trainer_home") {
                        //Temp stub
                        androidx.compose.material3.Surface(
                            modifier = androidx.compose.ui.Modifier.fillMaxSize()
                        ) {
                            androidx.compose.material3.Text("TRAINER_HOME")
                        }
                    }
                }
            }
        }
    }
}