package pl.edu.agh.gem.external.dto.auth

import jakarta.validation.constraints.NotBlank

data class PasswordRecoveryRequest(
    @field:NotBlank(message = "Email can not be blank")
    val email: String,
)
