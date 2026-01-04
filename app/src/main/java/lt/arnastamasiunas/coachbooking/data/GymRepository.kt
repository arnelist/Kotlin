package lt.arnastamasiunas.coachbooking.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import lt.arnastamasiunas.coachbooking.model.Gym

class GymRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun getGyms(): List<Gym> {
        val snap = db.collection("gyms").get().await()
        return snap.documents.map { d ->
            Gym(
                id = d.id,
                name = d.getString("name") ?: "Gym",
                address = d.getString("address") ?: ""
            )
        }.sortedBy { it.name.lowercase() }
    }

    suspend fun getGymNameById(gymId: String): String {
        val doc = db.collection("gyms").document(gymId).get().await()
        return doc.getString("name") ?: "Gym"
    }
}