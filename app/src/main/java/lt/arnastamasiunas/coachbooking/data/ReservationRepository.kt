package lt.arnastamasiunas.coachbooking.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import lt.arnastamasiunas.coachbooking.model.Reservation

class ReservationRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun createReservationTransactional(
        clientId: String,
        trainerId: String,
        date: String,
        start: String,
        end: String,
        timeslotId: String
    ) {
        val timeslotRef = db.collection("timeslots").document(timeslotId)
        val reservationsRef = db.collection("reservations")

        db.runTransaction { tx ->
            val snap = tx.get(timeslotRef)
            val status = snap.getString("status") ?: "free"
            if (status.lowercase() == "booked") {
                throw IllegalStateException("Šis laikas jau užimtas.")
            }

            tx.update(timeslotRef, "status", "booked")

            val reservationData = hashMapOf(
                "clientId" to clientId,
                "trainerId" to trainerId,
                "date" to date,
                "start" to start,
                "end" to end,
                "createdAt" to System.currentTimeMillis(),
                "timeslotId" to timeslotId
            )
            tx.set(reservationsRef.document(), reservationData)

            null
        }.await()
    }

    suspend fun getClientReservations(clientId: String): List<Reservation> {
        val snap = db.collection("reservations")
            .whereEqualTo("clientId", clientId)
            .get()
            .await()

        return snap.documents.map { d ->
            Reservation(
                id = d.id,
                clientId = d.getString("clientId") ?: "",
                trainerId = d.getString("trainerId") ?: "",
                date = d.getString("date") ?: "",
                start = d.getString("start") ?: "",
                end = d.getString("end") ?: "",
                timeslotId = d.getString("timeslotId") ?: "",
                createdAt = d.getLong("createdAt") ?: 0L
            )
        }.sortedByDescending { it.createdAt }
    }

    suspend fun getTrainerReservations(trainerId: String): List<Reservation> {
        val snap = db.collection("reservations")
            .whereEqualTo("trainerId", trainerId)
            .get()
            .await()

        return snap.documents.map { d ->
            Reservation(
                id = d.id,
                clientId = d.getString("clientId") ?: "",
                trainerId = d.getString("trainerId") ?: "",
                date = d.getString("date") ?: "",
                start = d.getString("start") ?: "",
                end = d.getString("end") ?: "",
                timeslotId = d.getString("timeslotId") ?: "",
                createdAt = d.getLong("createdAt") ?: 0L
            )
        }.sortedByDescending { it.createdAt }
    }

    suspend fun cancelReservation(reservationId: String, timeslotId: String) {
        val resRef = db.collection("reservations").document(reservationId)
        val slotRef = db.collection("timeslots").document(timeslotId)

        db.runTransaction { tx ->
            val resSnap = tx.get(resRef)
            if (!resSnap.exists()) {
                throw IllegalStateException("Rezervacija jau pašalinta.")
            }

            val slotSnap = tx.get(slotRef)
            if (slotSnap.exists()) {
                tx.update(slotRef, "status", "free")
            }

            tx.delete(resRef)
            null
        }.await()
    }
}