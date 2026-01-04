package lt.arnastamasiunas.coachbooking.model

data class TrainerUser(
    val uid: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = ""
) {
    val fullName: String get() = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ")
}