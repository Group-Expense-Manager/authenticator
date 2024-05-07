package pl.edu.agh.gem.internal.service

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pl.edu.agh.gem.internal.client.EmailSenderClient
import pl.edu.agh.gem.internal.model.auth.NotVerifiedUser
import pl.edu.agh.gem.internal.model.auth.Verification
import pl.edu.agh.gem.internal.model.auth.VerifiedUser
import pl.edu.agh.gem.internal.model.emailsender.VerificationEmailDetails
import pl.edu.agh.gem.internal.persistence.NotVerifiedUserRepository
import pl.edu.agh.gem.internal.persistence.VerifiedUserRepository
import java.security.SecureRandom
import java.time.Duration
import java.time.LocalDateTime
import java.util.stream.Collectors

@Service
class AuthService(
    private val notVerifiedUserRepository: NotVerifiedUserRepository,
    private val verifiedUserRepository: VerifiedUserRepository,
    private val senderClient: EmailSenderClient,
    private val emailProperties: EmailProperties,
) {

    fun create(notVerifiedUser: NotVerifiedUser) {
        if (isEmailTaken(notVerifiedUser.email)) {
            throw DuplicateEmailException(notVerifiedUser.email)
        }
        notVerifiedUserRepository.create(notVerifiedUser)
        senderClient.sendVerificationEmail(VerificationEmailDetails(notVerifiedUser.email, notVerifiedUser.code))
    }

    private fun isEmailTaken(email: String) =
        notVerifiedUserRepository.findByEmail(email) != null || verifiedUserRepository.findByEmail(email) != null

    fun generateCode(): String {
        return SecureRandom().ints(0, RANDOM_NUMBER_BOUND)
            .limit(CODE_LENGTH)
            .boxed()
            .map(Int::toString)
            .collect(Collectors.joining(""))
    }

    fun getVerifiedUser(email: String): VerifiedUser {
        return verifiedUserRepository.findByEmail(email) ?: throw UserNotVerifiedException()
    }

    @Transactional
    fun verify(verification: Verification): VerifiedUser {
        val notVerifiedUser = notVerifiedUserRepository.findByEmail(verification.email) ?: throw UserNotFoundException()

        if (notVerifiedUser.code != verification.code) {
            throw VerificationException(notVerifiedUser.email)
        }
        notVerifiedUserRepository.deleteById(notVerifiedUser.id)
        return verifiedUserRepository.create(notVerifiedUser.toVerified())
    }

    fun sendVerificationEmail(email: String) {
        val notVerifiedUser = notVerifiedUserRepository.findByEmail(email) ?: throw UserNotFoundException()

        if (notVerifiedUser.codeUpdatedAt.isAfter(LocalDateTime.now().minus(emailProperties.timeBetweenEmails))) {
            throw EmailRecentlySentException()
        }
        val newCode = generateCode()
        senderClient.sendVerificationEmail(VerificationEmailDetails(notVerifiedUser.email, newCode))
        notVerifiedUserRepository.updateVerificationCode(notVerifiedUser.id, newCode)
    }

    companion object {
        private const val CODE_LENGTH = 6L
        private const val RANDOM_NUMBER_BOUND = 10
    }
}

private fun NotVerifiedUser.toVerified() =
    VerifiedUser(
        id = id,
        email = email,
        password = password,
    )

@ConfigurationProperties(prefix = "email")
data class EmailProperties(
    val timeBetweenEmails: Duration,
)

class DuplicateEmailException(email: String) : RuntimeException("Email address $email is already taken")
class UserNotVerifiedException : RuntimeException("User is not verified")
class UserNotFoundException : RuntimeException("User not found")
class VerificationException(email: String) : RuntimeException("Verification failed for $email")
class EmailRecentlySentException : RuntimeException("Email was recently sent, please wait 5 minutes")
