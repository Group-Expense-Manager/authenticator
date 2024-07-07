package pl.edu.agh.gem.external.dto.auth

data class LoginResponse(
    val userId: String,
    val token: String,
)
