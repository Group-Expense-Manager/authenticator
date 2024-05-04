package pl.edu.agh.gem.external.dto.emailsender

import pl.edu.agh.gem.internal.model.emailsender.VerificationEmailDetails

data class VerificationEmailRequest(
    val email: String,
    val code: String,
) {
    companion object {
        fun from(verificationEmailDetails: VerificationEmailDetails) =
            VerificationEmailRequest(
                email = verificationEmailDetails.email,
                code = verificationEmailDetails.code,
            )
    }
}
