package lt.arnastamasiunas.coachbooking.model

data class Reservation(
    val id: String = "",
    val clientId: String = "",
    val trainerId: String = "",
    val date: String = "",     // "yyyy-MM-dd"
    val start: String = "",     // "HH:mm"
    val end: String = "",       // "HH:mm"
    val createdAt: Long = 0L
)