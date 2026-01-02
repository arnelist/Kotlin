package lt.arnastamasiunas.coachbooking.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun createUser(
        uid: String,
        email: String,
        role: String
    ) {
        val userData = hashMapOf(
            "uid" to uid,
            "email" to email,
            "role" to role,
            "createdAt" to System.currentTimeMillis()
        )

        db.collection("users")
            .document(uid)
            .set(userData)
            .await()
    }

    suspend fun getRole(uid: String):String {
        val snapshot = db.collection("users")
            .document(uid)
            .get()
            .await()

        return snapshot.getString("role") ?: "client"
    }
}