package pl.edu.agh.gem.external.dto.emailsender

import pl.edu.agh.gem.internal.model.emailsender.PasswordRecoveryEmailDetails

data class PasswordRecoveryEmailRequest(
    val email: String,
    val link: String,
) {
    companion object {
        fun from(passwordRecoveryEmailDetails: PasswordRecoveryEmailDetails) =
            PasswordRecoveryEmailRequest(
                email = passwordRecoveryEmailDetails.email,
                link = passwordRecoveryEmailDetails.link,
            )
    }
}
