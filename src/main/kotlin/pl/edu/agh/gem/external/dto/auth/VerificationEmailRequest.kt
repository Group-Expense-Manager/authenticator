package pl.edu.agh.gem.external.dto.auth

import jakarta.validation.constraints.NotBlank
import pl.edu.agh.gem.external.dto.ValidationMessage.EMAIL_NOT_BLANK

data class VerificationEmailRequest(
    @field:NotBlank(message = EMAIL_NOT_BLANK)
    val email: String,
)
