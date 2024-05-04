package pl.edu.agh.gem.external.dto.auth

import jakarta.validation.constraints.NotBlank
import pl.edu.agh.gem.internal.model.auth.Verification

data class VerificationRequest(
    @field:NotBlank(message = "Email can not be blank")
    val email: String,
    @field:NotBlank(message = "Code can not be blank")
    val code: String,
) {
    fun toDomain() = Verification(
        email = email,
        code = code,
    )
}
