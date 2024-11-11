package pl.edu.agh.gem.internal.model.emailsender

data class PasswordEmailDetails(
    val userId: String,
    val email: String,
    val password: String,
)
