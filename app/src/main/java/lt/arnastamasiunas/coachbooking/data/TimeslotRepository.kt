package lt.arnastamasiunas.coachbooking.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import lt.arnastamasiunas.coachbooking.model.Timeslot

class TimeslotRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun getTimeslots(trainerId: String, date: String): List<Timeslot> {
        val snap = db.collection("timeslots")
            .whereEqualTo("trainerId", trainerId)
            .whereEqualTo("date", date)
            .get()
            .await()

        return snap.documents.map { doc ->
            Timeslot(
                id = doc.id,
                trainerId = doc.getString("trainerId") ?: "",
                date = doc.getString("date") ?: "",
                start = doc.getString("start") ?: "",
                end = doc.getString("end") ?: "",
                status = doc.getString("status") ?: "",
                order = (doc.getLong("order") ?: 0L)
            )
        }.sortedBy { it.order }
    }

    suspend fun createTimeslot(
        trainerId: String,
        date: String,
        start: String,
        end: String,
        order: Long
    ) {
        val docId = "${trainerId}_${date}_${start}".replace(":", "-")
        val ref = db.collection("timeslots").document(docId)

        db.runTransaction { tx ->
            val snap = tx.get(ref)
            if (snap.exists()) {
                throw IllegalStateException("Timeslot $date $start jau egzistuoja.")
            }

            val data = hashMapOf(
                "trainerId" to trainerId,
                "date" to date,
                "start" to start,
                "end" to end,
                "status" to "free",
                "order" to order,
                "createdAt" to System.currentTimeMillis()
            )

            tx.set(ref, data)
            null
        }.await()
    }
}