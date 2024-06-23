package pl.edu.agh.gem.external.dto.auth

data class VerificationResponse(
    val userId: String,
    val token: String,
)
