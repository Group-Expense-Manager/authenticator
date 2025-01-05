package pl.edu.agh.gem.external.dto.auth

import jakarta.validation.constraints.NotBlank
import pl.edu.agh.gem.external.dto.ValidationMessage.CODE_NOT_BLANK
import pl.edu.agh.gem.external.dto.ValidationMessage.EMAIL_NOT_BLANK
import pl.edu.agh.gem.internal.model.auth.Verification

data class VerificationRequest(
    @field:NotBlank(message = EMAIL_NOT_BLANK)
    val email: String,
    @field:NotBlank(message = CODE_NOT_BLANK)
    val code: String,
) {
    fun toDomain() =
        Verification(
            email = email.lowercase(),
            code = code,
        )
}
