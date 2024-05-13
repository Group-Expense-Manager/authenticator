package pl.edu.agh.gem.external.dto.auth

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class RegistrationRequest(
    @field:NotBlank(message = "Email can not be blank")
    @field:Pattern(
        regexp = "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$",
        message = "Wrong email format",
    )
    val email: String,
    @field:NotBlank(message = "Password can not be blank")
    @field:Size(min = 8, message = "Minimum password length is 8")
    @field:Size(max = 30, message = "Maximum password length is 30")
    @field:Pattern.List(
        Pattern(regexp = ".*[a-z].*", message = "Password must contain at least one lowercase letter"),
        Pattern(regexp = ".*[A-Z].*", message = "Password must contain at least one uppercase letter"),
        Pattern(regexp = ".*\\d.*", message = "Password must contain at least one digit"),
        Pattern(regexp = ".*[@#$%^&+=!].*", message = "Password must contain at least one special character among @#$%^&+=!"),
    )
    val password: String,
)
