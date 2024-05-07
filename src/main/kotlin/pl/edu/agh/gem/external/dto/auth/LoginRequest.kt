package pl.edu.agh.gem.external.dto.auth

import jakarta.validation.constraints.NotBlank
import pl.edu.agh.gem.external.dto.ValidationMessage.Companion.EMAIL_NOT_BLANK
import pl.edu.agh.gem.external.dto.ValidationMessage.Companion.PASSWORD_NOT_BLANK

data class LoginRequest(
    @field:NotBlank(message = EMAIL_NOT_BLANK)
    val email: String,
    @field:NotBlank(message = PASSWORD_NOT_BLANK)
    val password: String,
)
