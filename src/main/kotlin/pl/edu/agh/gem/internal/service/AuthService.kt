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
import pl.edu.agh.gem.internal.model.emailsender.PasswordEmailDetails
import pl.edu.agh.gem.internal.model.emailsender.PasswordRecoveryEmailDetails
import pl.edu.agh.gem.internal.model.emailsender.VerificationEmailDetails
import pl.edu.agh.gem.internal.model.userdetailsmanager.UserDetails
import pl.edu.agh.gem.internal.persistence.NotVerifiedUserRepository
import pl.edu.agh.gem.internal.persistence.PasswordRecoveryCodeRepository
import pl.edu.agh.gem.internal.persistence.VerifiedUserRepository
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
    private val passwordEncoder: PasswordEncoder,
) {
    @Transactional
    fun create(notVerifiedUser: NotVerifiedUser) {
        if (isEmailTaken(notVerifiedUser.email)) {
            throw DuplicateEmailException(notVerifiedUser.email)
        }
        notVerifiedUserRepository.create(notVerifiedUser)
        senderClient.sendVerificationEmail(
            VerificationEmailDetails(
                notVerifiedUser.username,
                notVerifiedUser.email,
                notVerifiedUser.code,
            ),
        )
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
        userDetailsManagerClient.createUserDetails(getUserDetails(notVerifiedUser))
        return verifiedUser
    }

    private fun getUserDetails(notVerifiedUser: NotVerifiedUser) =
        UserDetails(
            userId = notVerifiedUser.id,
            username = notVerifiedUser.username,
        )

    @Transactional
    fun sendVerificationEmail(email: String) {
        val notVerifiedUser = notVerifiedUserRepository.findByEmail(email) ?: throw UserNotFoundException()

        if (!canSendEmail(notVerifiedUser)) {
            throw EmailRecentlySentException()
        }
        val newCode = generateCode()
        notVerifiedUserRepository.updateVerificationCode(notVerifiedUser.id, newCode)
        senderClient.sendVerificationEmail(
            VerificationEmailDetails(
                notVerifiedUser.username,
                notVerifiedUser.email,
                newCode,
            ),
        )
    }

    fun changePassword(
        userId: String,
        oldPassword: String,
        newPassword: String,
    ) {
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
        val passwordRecoveryCode =
            passwordRecoveryCodeRepository.create(
                PasswordRecoveryCode(
                    userId = verifiedUser.id,
                    code = generateCode(),
                ),
            )
        senderClient.sendPasswordRecoveryEmail(PasswordRecoveryEmailDetails(verifiedUser.id, email, passwordRecoveryCode.code))
    }

    @Transactional
    fun sendPasswordEmail(
        email: String,
        code: String,
    ) {
        val verifiedUser = verifiedUserRepository.findByEmail(email) ?: throw PasswordRecoveryException()
        val passwordRecoveryCode = passwordRecoveryCodeRepository.findByUserId(verifiedUser.id) ?: throw PasswordRecoveryException()
        if (passwordRecoveryCode.code != code) {
            throw PasswordRecoveryException()
        }

        val newPassword = generatePassword()
        verifiedUserRepository.updatePassword(verifiedUser.id, passwordEncoder.encode(newPassword))
        passwordRecoveryCodeRepository.deleteByUserId(verifiedUser.id)
        senderClient.sendPassword(PasswordEmailDetails(verifiedUser.id, email, newPassword))
    }

    private fun generatePassword(): String {
        val allChars = LOWERCASE + UPPERCASE + DIGITS + SPECIAL_CHARACTERS
        val initialPassword = (LOWERCASE.random().toString() + UPPERCASE.random() + DIGITS.random() + SPECIAL_CHARACTERS.random())
        val remainingLength = GENERATED_PASSWORD_LENGTH - initialPassword.length

        return buildString {
            append(initialPassword)
            repeat(remainingLength) {
                append(allChars.random())
            }
        }
    }

    fun getEmailAddress(userId: String): String {
        return verifiedUserRepository.findById(userId)?.email ?: throw UserNotFoundException()
    }

    companion object {
        private const val CODE_LENGTH = 6L
        private const val RANDOM_NUMBER_BOUND = 10
        private const val GENERATED_PASSWORD_LENGTH = 30
        private const val LOWERCASE = "abcdefghijklmnopqrstuvwxyz"
        private const val UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        private const val DIGITS = "0123456789"
        private const val SPECIAL_CHARACTERS = "@#$%^&+=!"
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

class WrongPasswordException : RuntimeException("Wrong password")

class PasswordRecoveryException : RuntimeException("Invalid password recovery link")
