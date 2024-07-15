package pl.edu.agh.gem.internal.model.emailsender

data class PasswordRecoveryEmailDetails(
    val email: String,
    val link: String,
)
