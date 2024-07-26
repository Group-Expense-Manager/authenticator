package pl.edu.agh.gem.internal.model.emailsender

data class PasswordRecoveryEmailDetails(
    val username: String,
    val email: String,
    val code: String,
)
