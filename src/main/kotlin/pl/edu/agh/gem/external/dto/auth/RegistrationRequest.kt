package pl.edu.agh.gem.external.dto.auth

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import pl.edu.agh.gem.external.dto.ValidationMessage.EMAIL_NOT_BLANK
import pl.edu.agh.gem.external.dto.ValidationMessage.MAX_PASSWORD_LENGTH
import pl.edu.agh.gem.external.dto.ValidationMessage.MIN_PASSWORD_LENGTH
import pl.edu.agh.gem.external.dto.ValidationMessage.PASSWORD_DIGIT
import pl.edu.agh.gem.external.dto.ValidationMessage.PASSWORD_LOWERCASE
import pl.edu.agh.gem.external.dto.ValidationMessage.PASSWORD_NOT_BLANK
import pl.edu.agh.gem.external.dto.ValidationMessage.PASSWORD_SPECIAL_CHARACTER
import pl.edu.agh.gem.external.dto.ValidationMessage.PASSWORD_UPPERCASE
import pl.edu.agh.gem.external.dto.ValidationMessage.USERNAME_PATTERN_MESSAGE
import pl.edu.agh.gem.external.dto.ValidationMessage.WRONG_EMAIL_FORMAT

data class RegistrationRequest(
    @field:Pattern(message = USERNAME_PATTERN_MESSAGE, regexp = "^[a-zA-Z0-9_.+-]{3,20}$")
    val username: String,
    @field:NotBlank(message = EMAIL_NOT_BLANK)
    @field:Pattern(
        regexp = "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$",
        message = WRONG_EMAIL_FORMAT,
    )
    val email: String,
    @field:NotBlank(message = PASSWORD_NOT_BLANK)
    @field:Size(min = 8, message = MIN_PASSWORD_LENGTH)
    @field:Size(max = 30, message = MAX_PASSWORD_LENGTH)
    @field:Pattern.List(
        Pattern(regexp = ".*[a-z].*", message = PASSWORD_LOWERCASE),
        Pattern(regexp = ".*[A-Z].*", message = PASSWORD_UPPERCASE),
        Pattern(regexp = ".*\\d.*", message = PASSWORD_DIGIT),
        Pattern(regexp = ".*[@#$%^&+=!].*", message = PASSWORD_SPECIAL_CHARACTER),
    )
    val password: String,
)
