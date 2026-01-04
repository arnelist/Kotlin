package lt.arnastamasiunas.coachbooking

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.navigation.compose.*
import lt.arnastamasiunas.coachbooking.data.AuthRepository
import lt.arnastamasiunas.coachbooking.data.UserRepository
import lt.arnastamasiunas.coachbooking.navigation.Routes
import lt.arnastamasiunas.coachbooking.ui.BookingScreen
import lt.arnastamasiunas.coachbooking.ui.RootScreen
import lt.arnastamasiunas.coachbooking.ui.TrainerHomeScreen
import lt.arnastamasiunas.coachbooking.ui.auth.LoginScreen
import lt.arnastamasiunas.coachbooking.ui.auth.RegisterScreen
import lt.arnastamasiunas.coachbooking.ui.theme.CoachBookingTheme
import android.net.Uri
import androidx.navigation.NavType
import androidx.navigation.navArgument
import lt.arnastamasiunas.coachbooking.data.ReservationRepository
import lt.arnastamasiunas.coachbooking.data.TimeslotRepository
import lt.arnastamasiunas.coachbooking.ui.trainer.TrainerTimeslotCreatorScreen
import lt.arnastamasiunas.coachbooking.data.GymRepository
import lt.arnastamasiunas.coachbooking.ui.client.ClientReservationsScreen
import lt.arnastamasiunas.coachbooking.ui.client.GymsScreen
import lt.arnastamasiunas.coachbooking.ui.client.GymTrainersScreen
import lt.arnastamasiunas.coachbooking.ui.trainer.TrainerReservationsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CoachBookingTheme {
                val navController = rememberNavController()
                val authRepo = remember { AuthRepository() }
                val userRepo = remember { UserRepository() }
                val timeslotRepo = remember { TimeslotRepository() }
                val reservationRepo = remember { ReservationRepository() }
                val gymRepo = remember { GymRepository() }

                NavHost(
                    navController = navController,
                    startDestination = Routes.ROOT
                ) {
                    composable(Routes.ROOT) {
                        RootScreen(navController, authRepo, userRepo)
                    }

                    composable(Routes.LOGIN) {
                        LoginScreen(
                            onLogin = {email, password ->
                                authRepo.login(email, password)
                                val uid = authRepo.currentUid()!!
                                val role = userRepo.getRole(uid)

                                navController.navigate(
                                    if (role == "trainer") Routes.TRAINER_HOME else Routes.GYMS
                                ) {
                                    popUpTo(Routes.LOGIN) { inclusive = true }
                                }
                            },
                            onGoRegister = {
                                navController.navigate(Routes.REGISTER)
                            }
                        )
                    }

                    composable(Routes.REGISTER) {
                        RegisterScreen(
                            onRegister = {email, password, role ->
                                val uid = authRepo.register(email, password)
                                userRepo.createUser(uid, email, role)

                                navController.navigate(
                                    if (role == "trainer") Routes.TRAINER_HOME else Routes.GYMS
                                ) {
                                    popUpTo(Routes.LOGIN) { inclusive = true }
                                }
                            },
                            onGoLogin = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable(Routes.GYMS) {
                        GymsScreen(
                            gymRepo = gymRepo,
                            onLogout = {
                                authRepo.logout()
                                navController.navigate(Routes.LOGIN) { popUpTo(0) }
                            },
                            onReservations = { navController.navigate(Routes.CLIENT_RESERVATIONS) },
                            onGymClick = { gymId, gymName ->
                                val nameEncoded = Uri.encode(gymName)
                                navController.navigate("${Routes.GYM_TRAINERS}?gymId=$gymId&gymName=$nameEncoded")
                            }
                        )
                    }

                    composable(
                        route = "${Routes.GYM_TRAINERS}?gymId={gymId}&gymName={gymName}",
                        arguments = listOf(
                            navArgument("gymId") { type = NavType.StringType; defaultValue = "" },
                            navArgument("gymName") { type = NavType.StringType; defaultValue = "" }
                        )
                    ) { backStackEntry ->
                        val gymId = backStackEntry.arguments?.getString("gymId") ?: ""
                        val gymName = backStackEntry.arguments?.getString("gymName") ?: ""

                        GymTrainersScreen(
                            gymId = gymId,
                            gymName = gymName,
                            userRepo = userRepo,
                            onBack = { navController.popBackStack() },
                            onTrainerClick = { trainerId, trainerEmail ->
                                val emailEncoded = Uri.encode(trainerEmail)
                                navController.navigate("${Routes.BOOKING}?trainerId=$trainerId&trainerEmail=$emailEncoded")
                            }
                        )
                    }
                    composable(Routes.TRAINER_HOME) {
                        TrainerHomeScreen(
                            onCreateTimeslot = { navController.navigate(Routes.TRAINER_CREATE_SLOT) },
                            onLogout = {
                                authRepo.logout()
                                navController.navigate(Routes.LOGIN) { popUpTo(0) }
                            },
                            onReservations = { navController.navigate(Routes.TRAINER_RESERVATIONS) }
                        )
                    }

                    composable(
                        route = "${Routes.BOOKING}?trainerId={trainerId}&trainerEmail={trainerEmail}",
                        arguments = listOf(
                            navArgument("trainerId") { type = NavType.StringType; defaultValue = "" },
                            navArgument("trainerEmail") { type = NavType.StringType; defaultValue = "" }
                        )
                    ) { backStackEntry ->
                        val trainerId = backStackEntry.arguments?.getString("trainerId") ?: ""
                        val trainerEmail = backStackEntry.arguments?.getString("trainerEmail") ?: ""

                        BookingScreen(
                            trainerId = trainerId,
                            trainerEmail = trainerEmail,
                            authRepo = authRepo,
                            timeslotRepo = timeslotRepo,
                            reservationRepo = reservationRepo,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(Routes.TRAINER_CREATE_SLOT) {
                        TrainerTimeslotCreatorScreen(
                            authRepo = authRepo,
                            timeslotRepo = timeslotRepo,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(Routes.CLIENT_RESERVATIONS) {
                        ClientReservationsScreen(
                            authRepo = authRepo,
                            reservationRepo = reservationRepo,
                            userRepo = userRepo,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(Routes.TRAINER_RESERVATIONS) {
                        TrainerReservationsScreen(
                            authRepo = authRepo,
                            reservationRepo = reservationRepo,
                            userRepo = userRepo,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}