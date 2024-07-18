package pl.edu.agh.gem.internal.model.emailsender

data class VerificationEmailDetails(
    val username: String,
    val email: String,
    val code: String,
)
