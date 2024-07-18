package pl.edu.agh.gem.external.client

import io.github.resilience4j.retry.annotation.Retry
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod.POST
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate
import pl.edu.agh.gem.config.EmailSenderClientProperties
import pl.edu.agh.gem.external.dto.emailsender.PasswordEmailRequest
import pl.edu.agh.gem.external.dto.emailsender.PasswordRecoveryEmailRequest
import pl.edu.agh.gem.external.dto.emailsender.VerificationEmailRequest
import pl.edu.agh.gem.headers.HeadersUtils.withAppContentType
import pl.edu.agh.gem.internal.client.EmailSenderClient
import pl.edu.agh.gem.internal.client.EmailSenderClientException
import pl.edu.agh.gem.internal.client.RetryableEmailSenderClientException
import pl.edu.agh.gem.internal.model.emailsender.PasswordEmailDetails
import pl.edu.agh.gem.internal.model.emailsender.PasswordRecoveryEmailDetails
import pl.edu.agh.gem.internal.model.emailsender.VerificationEmailDetails
import pl.edu.agh.gem.paths.Paths.INTERNAL
import pl.edu.agh.gem.paths.Paths.OPEN

@Component
class RestEmailSenderClient(
    @Qualifier("EmailSenderClientRestTemplate") private val restTemplate: RestTemplate,
    private val emailSenderClientProperties: EmailSenderClientProperties,
    private val urlProperties: UrlProperties,

) : EmailSenderClient {

    @Retry(name = "emailSender")
    override fun sendVerificationEmail(verificationEmailDetails: VerificationEmailDetails) {
        try {
            restTemplate.exchange(
                resolveVerificationAddress(),
                POST,
                HttpEntity(VerificationEmailRequest.from(verificationEmailDetails), HttpHeaders().withAppContentType()),
                Any::class.java,
            )
        } catch (ex: HttpClientErrorException) {
            logger.warn(ex) { "Client side exception while trying to send verification email request: $verificationEmailDetails" }
            throw EmailSenderClientException(ex.message)
        } catch (ex: HttpServerErrorException) {
            logger.warn(ex) { "Server side exception while trying to send verification email request: $verificationEmailDetails" }
            throw RetryableEmailSenderClientException(ex.message)
        } catch (ex: Exception) {
            logger.warn(ex) { "Unexpected exception while trying to send verification email request: $verificationEmailDetails" }
            throw EmailSenderClientException(ex.message)
        }
    }

    override fun sendPasswordRecoveryEmail(email: String, code: String) {
        val passwordRecoveryEmailDetails = PasswordRecoveryEmailDetails(email, generateLink(email, code))
        try {
            restTemplate.exchange(
                resolvePasswordRecoveryAddress(),
                POST,
                HttpEntity(PasswordRecoveryEmailRequest.from(passwordRecoveryEmailDetails), HttpHeaders().withAppContentType()),
                Any::class.java,
            )
        } catch (ex: HttpClientErrorException) {
            logger.warn(ex) { "Client side exception while trying to send recover-password email request: $passwordRecoveryEmailDetails" }
            throw EmailSenderClientException(ex.message)
        } catch (ex: HttpServerErrorException) {
            logger.warn(ex) { "Server side exception while trying to send recover-password email request: $passwordRecoveryEmailDetails" }
            throw RetryableEmailSenderClientException(ex.message)
        } catch (ex: Exception) {
            logger.warn(ex) { "Unexpected exception while trying to send recover-password email request: $passwordRecoveryEmailDetails" }
            throw EmailSenderClientException(ex.message)
        }
    }

    override fun sendPassword(passwordEmailDetails: PasswordEmailDetails) {
        try {
            restTemplate.exchange(
                resolvePasswordAddress(),
                POST,
                HttpEntity(PasswordEmailRequest.from(passwordEmailDetails), HttpHeaders().withAppContentType()),
                Any::class.java,
            )
        } catch (ex: HttpClientErrorException) {
            logger.warn(ex) { "Client side exception while trying to send password email request: $passwordEmailDetails" }
            throw EmailSenderClientException(ex.message)
        } catch (ex: HttpServerErrorException) {
            logger.warn(ex) { "Server side exception while trying to send password email request: $passwordEmailDetails" }
            throw RetryableEmailSenderClientException(ex.message)
        } catch (ex: Exception) {
            logger.warn(ex) { "Unexpected exception while trying to send password email request: $passwordEmailDetails" }
            throw EmailSenderClientException(ex.message)
        }
    }

    private fun generateLink(email: String, code: String) = "http://${urlProperties.gemUrl}$OPEN/send-password?email=$email&code=$code"

    private fun resolveVerificationAddress() =
        "${emailSenderClientProperties.url}/$INTERNAL/verification"

    private fun resolvePasswordRecoveryAddress() =
        "${emailSenderClientProperties.url}/$INTERNAL/recover-password"

    private fun resolvePasswordAddress() =
        "${emailSenderClientProperties.url}/$INTERNAL/password"

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}

@ConfigurationProperties(prefix = "url")
data class UrlProperties(
    val gemUrl: String,
)
