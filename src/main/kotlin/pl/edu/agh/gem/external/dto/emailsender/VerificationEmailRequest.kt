package pl.edu.agh.gem.external.dto.emailsender

import pl.edu.agh.gem.internal.model.emailsender.VerificationEmailDetails

data class VerificationEmailRequest(
    val username: String,
    val email: String,
    val code: String,
) {
    companion object {
        fun from(verificationEmailDetails: VerificationEmailDetails) =
            VerificationEmailRequest(
                username = verificationEmailDetails.username,
                email = verificationEmailDetails.email,
                code = verificationEmailDetails.code,
            )
    }
}
