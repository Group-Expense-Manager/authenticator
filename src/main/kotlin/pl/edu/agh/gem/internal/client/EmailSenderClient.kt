package pl.edu.agh.gem.internal.client

import pl.edu.agh.gem.internal.model.emailsender.PasswordEmailDetails
import pl.edu.agh.gem.internal.model.emailsender.PasswordRecoveryEmailDetails
import pl.edu.agh.gem.internal.model.emailsender.VerificationEmailDetails

interface EmailSenderClient {
    fun sendVerificationEmail(verificationEmailDetails: VerificationEmailDetails)

    fun sendPasswordRecoveryEmail(passwordRecoveryEmailDetails: PasswordRecoveryEmailDetails)

    fun sendPassword(passwordEmailDetails: PasswordEmailDetails)
}

class EmailSenderClientException(override val message: String?) : RuntimeException()

class RetryableEmailSenderClientException(override val message: String?) : RuntimeException()
