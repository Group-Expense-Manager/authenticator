package pl.edu.agh.gem.external.dto.auth

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import pl.edu.agh.gem.external.dto.ValidationMessage.MAX_PASSWORD_LENGTH
import pl.edu.agh.gem.external.dto.ValidationMessage.MIN_PASSWORD_LENGTH
import pl.edu.agh.gem.external.dto.ValidationMessage.PASSWORD_DIGIT
import pl.edu.agh.gem.external.dto.ValidationMessage.PASSWORD_LOWERCASE
import pl.edu.agh.gem.external.dto.ValidationMessage.PASSWORD_NOT_BLANK
import pl.edu.agh.gem.external.dto.ValidationMessage.PASSWORD_SPECIAL_CHARACTER
import pl.edu.agh.gem.external.dto.ValidationMessage.PASSWORD_UPPERCASE

data class PasswordChangeRequest(
    @field:NotBlank(message = PASSWORD_NOT_BLANK)
    val oldPassword: String,
    @field:NotBlank(message = PASSWORD_NOT_BLANK)
    @field:Size(min = 8, message = MIN_PASSWORD_LENGTH)
    @field:Size(max = 30, message = MAX_PASSWORD_LENGTH)
    @field:Pattern.List(
        Pattern(regexp = ".*\\p{Ll}.*", message = PASSWORD_LOWERCASE),
        Pattern(regexp = ".*\\p{Lu}.*", message = PASSWORD_UPPERCASE),
        Pattern(regexp = ".*\\d.*", message = PASSWORD_DIGIT),
        Pattern(regexp = ".*[@#$%^&+=!].*", message = PASSWORD_SPECIAL_CHARACTER),
    )
    val newPassword: String,
)
