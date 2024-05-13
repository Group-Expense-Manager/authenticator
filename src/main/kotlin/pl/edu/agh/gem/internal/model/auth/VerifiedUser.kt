package pl.edu.agh.gem.internal.model.auth

data class VerifiedUser(
    val id: String,
    val email: String,
    val password: String,
)
