package pl.edu.agh.gem.external.dto.auth

import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:NotBlank(message = "Email can not be blank")
    val email: String,
    @field:NotBlank(message = "Password can not be blank")
    val password: String,
)
