package pl.edu.agh.gem.external.dto.emailsender

import pl.edu.agh.gem.internal.model.emailsender.PasswordEmailDetails

data class PasswordEmailRequest(
    val username: String,
    val email: String,
    val password: String,
) {
    companion object {
        fun from(passwordEmailDetails: PasswordEmailDetails) =
            PasswordEmailRequest(
                username = passwordEmailDetails.username,
                email = passwordEmailDetails.email,
                password = passwordEmailDetails.password,
            )
    }
}
