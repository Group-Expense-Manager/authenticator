package pl.edu.agh.gem.external.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import pl.edu.agh.gem.internal.domain.User

data class RegistrationRequest(
    @field:NotBlank(message = "USERNAME CAN NOT BE BLANK")
    @field:Size(min = 4, message = "MIN USERNAME LENGTH IS 4")
    @field:Size(max = 20, message = "MAX USERNAME LENGTH IS 20")
    @field:Pattern(regexp = "^[a-zA-Z0-9]+$", message = "USERNAME MAY ONLY CONTAIN LETTERS, NUMBERS AND SPECIAL SIGNS: _-")
    val username: String,
    @field:NotBlank(message = "USERNAME CAN NOT BE BLANK")
    @field:Size(min = 8, message = "MIN PASSWORD LENGTH IS 8")
    @field:Size(max = 30, message = "MAX USERNAME LENGTH IS 20")
    @field:Pattern.List(
        Pattern(regexp = ".*[a-z].*", message = "PASSWORD MUST CONTAIN AT LEAST ONE LOWERCASE LETTER."),
        Pattern(regexp = ".*[A-Z].*", message = "PASSWORD MUST CONTAIN AT LEAST ONE UPPERCASE LETTER."),
        Pattern(regexp = ".*\\d.*", message = "PASSWORD MUST CONTAIN AT LEAST ONE DIGIT."),
        Pattern(regexp = ".*[@#$%^&+=!].*", message = "PASSWORD MUST CONTAIN AT LEAST ONE SPECIAL CHARACTER AMONG @#$%^&+=!"),
    )
    val password: String,
) {
    fun toDomain() =
        User(
            username = username,
            password = password,
        )
}
