package pl.edu.agh.gem.external.dto.auth

import jakarta.validation.constraints.NotBlank

data class VerificationEmailRequest(
    @field:NotBlank(message = "Email can not be blank")
    val email: String,
)
