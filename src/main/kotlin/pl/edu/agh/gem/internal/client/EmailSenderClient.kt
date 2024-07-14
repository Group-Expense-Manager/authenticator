package pl.edu.agh.gem.internal.client

import pl.edu.agh.gem.internal.model.emailsender.VerificationEmailDetails

interface EmailSenderClient {
    fun sendVerificationEmail(verificationEmailDetails: VerificationEmailDetails)
    fun sendPasswordRecoveryEmail(email: String, code: String)
}

class EmailSenderClientException(override val message: String?) : RuntimeException()

class RetryableEmailSenderClientException(override val message: String?) : RuntimeException()
