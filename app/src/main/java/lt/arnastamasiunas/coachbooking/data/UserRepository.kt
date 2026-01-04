package lt.arnastamasiunas.coachbooking.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import lt.arnastamasiunas.coachbooking.model.TrainerUser

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

    suspend fun getRole(uid: String): String {
        val doc = db.collection("users").document(uid).get().await()
        return (doc.getString("role") ?: "client").trim().lowercase()
    }

    suspend fun getTrainers(): List<TrainerUser> {
        val snapshot = db.collection("users")
            .whereEqualTo("role", "trainer")
            .get()
            .await()

        return snapshot.documents.map { doc ->
            TrainerUser(
                uid = doc.getString("uid") ?: doc.id,
                email = doc.getString("email") ?: ""
            )
        }.sortedBy { it.email.lowercase() }
    }

    suspend fun createTrainerDoc(uid: String, email: String) {
        createUser(uid = uid, email = email, role = "trainer")
    }

    suspend fun setGym(uid: String, gymId: String) {
        db.collection("users").document(uid)
            .update("gymId", gymId)
            .await()
    }

    suspend fun getTrainersByGym(gymId: String): List<TrainerUser> {
        val snap = db.collection("users")
            .whereEqualTo("role", "trainer")
            .whereEqualTo("gymId", gymId)
            .get()
            .await()

        return snap.documents.map { doc ->
            TrainerUser(
                uid = doc.getString("uid") ?: doc.id,
                email = doc.getString("email") ?: ""
            )
        }.sortedBy { it.email.lowercase() }
    }

    suspend fun getEmailByUid(uid: String): String {
        val doc = db.collection("users").document(uid).get().await()
        return doc.getString("email") ?: uid
    }
}