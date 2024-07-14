package pl.edu.agh.gem.external.dto.emailsender

import pl.edu.agh.gem.internal.model.emailsender.PasswordEmailDetails

data class PasswordEmailRequest(
    val email: String,
    val password: String,
) {
    companion object {
        fun from(passwordEmailDetails: PasswordEmailDetails) =
            PasswordEmailRequest(
                email = passwordEmailDetails.email,
                password = passwordEmailDetails.password,
            )
    }
}
