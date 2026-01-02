package lt.arnastamasiunas.coachbooking.data

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    fun currentUid(): String? {
        return auth.currentUser?.uid
    }

    suspend fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun register(email: String, password: String): String {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        return result.user?.uid
            ?: throw IllegalStateException("User UID is null after register")
    }

    fun logout() {
        auth.signOut()
    }
}