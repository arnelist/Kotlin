package lt.arnastamasiunas.coachbooking.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

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

            // 1) pažymim timeslot kaip booked
            tx.update(timeslotRef, "status", "booked")

            // 2) sukuriam reservation
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
}