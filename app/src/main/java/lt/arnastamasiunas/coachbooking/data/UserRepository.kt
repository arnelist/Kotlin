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
        role: String,
        firstName: String,
        lastName: String
    ) {
        val userData = hashMapOf(
            "uid" to uid,
            "email" to email,
            "role" to role.trim().lowercase(),
            "firstName" to firstName.trim(),
            "lastName" to lastName.trim(),
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
                uid = doc.id,
                email = doc.getString("email") ?: "",
                firstName = doc.getString("firstName") ?: "",
                lastName = doc.getString("lastName") ?: ""
            )
        }.sortedBy { it.fullName.ifBlank { it.email }.lowercase() }
    }

    suspend fun getEmailByUid(uid: String): String {
        val doc = db.collection("users").document(uid).get().await()
        return doc.getString("email") ?: uid
    }

    suspend fun getGymId(uid: String): String? {
        val doc = db.collection("users").document(uid).get().await()
        return doc.getString("gymId")
    }

    data class UserProfile(
        val firstName: String = "",
        val lastName: String = "",
        val email: String = ""
    ) {
        val fullName: String get() = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ")
    }

    suspend fun getProfile(uid: String): UserProfile {
        val doc = db.collection("users").document(uid).get().await()
        return UserProfile(
            firstName = doc.getString("firstName") ?: "",
            lastName = doc.getString("lastName") ?: "",
            email = doc.getString("email") ?: ""
        )
    }

    suspend fun getDisplayName(uid: String): String {
        val doc = db.collection("users").document(uid).get().await()
        val first = doc.getString("firstName") ?: ""
        val last = doc.getString("lastName") ?: ""
        val email = doc.getString("email") ?: uid
        val full = listOf(first, last).filter { it.isNotBlank() }.joinToString(" ")
        return if (full.isNotBlank()) "$full ($email)" else email
    }
}