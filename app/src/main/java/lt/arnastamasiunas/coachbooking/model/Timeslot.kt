package lt.arnastamasiunas.coachbooking.model

data class Timeslot(
    val id: String = "",
    val trainerId: String = "",
    val date: String = "",     // "yyyy-MM-dd"
    val start: String = "",    // "HH:mm"
    val end: String = "",      // "HH:mm"
    val status: String = "",
    val order: Long = 0L
) {
    val isBooked: Boolean get() = status.lowercase() == "booked"
}