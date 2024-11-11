package pl.edu.agh.gem.external.dto.emailsender

data class PasswordRecoveryEmailRequest(
    val userId: String,
    val email: String,
    val link: String,
)
