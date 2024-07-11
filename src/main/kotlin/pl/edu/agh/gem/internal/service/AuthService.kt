package pl.edu.agh.gem.internal.service

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pl.edu.agh.gem.internal.client.EmailSenderClient
import pl.edu.agh.gem.internal.client.UserDetailsManagerClient
import pl.edu.agh.gem.internal.model.auth.NotVerifiedUser
import pl.edu.agh.gem.internal.model.auth.PasswordRecoveryCode
import pl.edu.agh.gem.internal.model.auth.Verification
import pl.edu.agh.gem.internal.model.auth.VerifiedUser
import pl.edu.agh.gem.internal.model.emailsender.PasswordRecoveryEmailDetails
import pl.edu.agh.gem.internal.model.emailsender.VerificationEmailDetails
import pl.edu.agh.gem.internal.model.userdetailsmanager.UserDetails
import pl.edu.agh.gem.internal.persistence.NotVerifiedUserRepository
import pl.edu.agh.gem.internal.persistence.PasswordRecoveryCodeRepository
import pl.edu.agh.gem.internal.persistence.VerifiedUserRepository
import pl.edu.agh.gem.paths.Paths.OPEN
import java.security.SecureRandom
import java.time.Duration
import java.time.Instant.now
import java.util.stream.Collectors

@Service
class AuthService(
    private val notVerifiedUserRepository: NotVerifiedUserRepository,
    private val verifiedUserRepository: VerifiedUserRepository,
    private val passwordRecoveryCodeRepository: PasswordRecoveryCodeRepository,
    private val senderClient: EmailSenderClient,
    private val userDetailsManagerClient: UserDetailsManagerClient,
    private val emailProperties: EmailProperties,
    private val urlProperties: UrlProperties,
    private val passwordEncoder: PasswordEncoder,
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
        val verifiedUser = verifiedUserRepository.create(notVerifiedUser.toVerified())
        userDetailsManagerClient.createUserDetails(getUserDetails(verifiedUser))
        return verifiedUser
    }

    private fun getUserDetails(verifiedUser: VerifiedUser) = UserDetails(
        userId = verifiedUser.id,
        username = verifiedUser.email.substringBefore("@"),
    )

    fun sendVerificationEmail(email: String) {
        val notVerifiedUser = notVerifiedUserRepository.findByEmail(email) ?: throw UserNotFoundException()

        if (!canSendEmail(notVerifiedUser)) {
            throw EmailRecentlySentException()
        }
        val newCode = generateCode()
        senderClient.sendVerificationEmail(VerificationEmailDetails(notVerifiedUser.email, newCode))
        notVerifiedUserRepository.updateVerificationCode(notVerifiedUser.id, newCode)
    }

    fun changePassword(userId: String, oldPassword: String, newPassword: String) {
        val verifiedUser = verifiedUserRepository.findById(userId) ?: throw UserNotFoundException()

        if (!passwordEncoder.matches(oldPassword, verifiedUser.password)) {
            throw WrongPasswordException()
        }

        verifiedUserRepository.updatePassword(verifiedUser.id, passwordEncoder.encode(newPassword))
    }

    private fun canSendEmail(notVerifiedUser: NotVerifiedUser) =
        notVerifiedUser.codeUpdatedAt.isBefore(now().minus(emailProperties.timeBetweenEmails))

    @Transactional
    fun sendPasswordRecoveryEmail(email: String) {
        val verifiedUser = verifiedUserRepository.findByEmail(email) ?: throw UserNotFoundException()

        if (passwordRecoveryCodeRepository.findByUserId(verifiedUser.id) != null) {
            throw EmailRecentlySentException()
        }

        val passwordRecoveryCode = passwordRecoveryCodeRepository.create(
            PasswordRecoveryCode(
                userId = verifiedUser.id,
                code = generateCode(),
            ),
        )

        val link = "http://${urlProperties.gemUrl}/$OPEN/send-password?email=$email&code=${passwordRecoveryCode.code}"
        senderClient.sendPasswordRecoveryEmail(PasswordRecoveryEmailDetails(email, link))
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

@ConfigurationProperties(prefix = "url")
data class UrlProperties(
    val gemUrl: String,
)

class DuplicateEmailException(email: String) : RuntimeException("Email address $email is already taken")
class UserNotVerifiedException : RuntimeException("User is not verified")
class UserNotFoundException : RuntimeException("User not found")
class VerificationException(email: String) : RuntimeException("Verification failed for $email")
class EmailRecentlySentException : RuntimeException("Email was recently sent, please wait 5 minutes")
class WrongPasswordException : RuntimeException("Wrong password")
