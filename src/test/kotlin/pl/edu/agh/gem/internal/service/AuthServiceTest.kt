package pl.edu.agh.gem.internal.service

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldHaveLength
import io.kotest.matchers.string.shouldMatch
import org.mockito.kotlin.anyVararg
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.crypto.password.PasswordEncoder
import pl.edu.agh.gem.helper.user.DummyUser.USER_ID
import pl.edu.agh.gem.internal.client.EmailSenderClient
import pl.edu.agh.gem.internal.client.UserDetailsManagerClient
import pl.edu.agh.gem.internal.model.auth.PasswordRecoveryCode
import pl.edu.agh.gem.internal.model.emailsender.VerificationEmailDetails
import pl.edu.agh.gem.internal.model.userdetailsmanager.UserDetails
import pl.edu.agh.gem.internal.persistence.NotVerifiedUserRepository
import pl.edu.agh.gem.internal.persistence.PasswordRecoveryCodeRepository
import pl.edu.agh.gem.internal.persistence.VerifiedUserRepository
import pl.edu.agh.gem.util.DummyData.DUMMY_CODE
import pl.edu.agh.gem.util.DummyData.DUMMY_EMAIL
import pl.edu.agh.gem.util.DummyData.DUMMY_PASSWORD
import pl.edu.agh.gem.util.DummyData.OTHER_DUMMY_CODE
import pl.edu.agh.gem.util.DummyData.OTHER_DUMMY_PASSWORD
import pl.edu.agh.gem.util.createNotVerifiedUser
import pl.edu.agh.gem.util.createPasswordRecoveryCode
import pl.edu.agh.gem.util.createVerification
import pl.edu.agh.gem.util.createVerifiedUser
import java.time.Duration
import java.time.Instant.now
import java.time.temporal.ChronoUnit.MINUTES

class AuthServiceTest : ShouldSpec(
    {
        val notVerifiedUserRepository = mock<NotVerifiedUserRepository>()
        val verifiedUserRepository = mock<VerifiedUserRepository>()
        val passwordRecoveryCodeRepository = mock<PasswordRecoveryCodeRepository>()
        val emailSenderClient = mock<EmailSenderClient>()
        val emailProperties = mock<EmailProperties>()
        val userDetailsManagerClient = mock<UserDetailsManagerClient>()
        val passwordEncoder = mock<PasswordEncoder>()

        val authService = AuthService(
            notVerifiedUserRepository,
            verifiedUserRepository,
            passwordRecoveryCodeRepository,
            emailSenderClient,
            userDetailsManagerClient,
            emailProperties,
            passwordEncoder,

        )

        should("create user") {
            // given
            val notVerifiedUser = createNotVerifiedUser()
            whenever(notVerifiedUserRepository.findByEmail(notVerifiedUser.email)).thenReturn(null)
            whenever(verifiedUserRepository.findByEmail(notVerifiedUser.email)).thenReturn(null)
            whenever(notVerifiedUserRepository.create(notVerifiedUser)).thenReturn(notVerifiedUser)
            val emailDetails = VerificationEmailDetails(notVerifiedUser.email, notVerifiedUser.code)

            // when
            authService.create(notVerifiedUser)

            // then
            verify(notVerifiedUserRepository, times(1)).findByEmail(notVerifiedUser.email)
            verify(verifiedUserRepository, times(1)).findByEmail(notVerifiedUser.email)
            verify(notVerifiedUserRepository, times(1)).create(notVerifiedUser)
            verify(emailSenderClient, times(1)).sendVerificationEmail(emailDetails)
        }

        should("throw DuplicateEmailException when creating user and there exists not verified user with given mail") {
            // given
            val notVerifiedUser = createNotVerifiedUser()
            whenever(notVerifiedUserRepository.findByEmail(notVerifiedUser.email)).thenReturn(notVerifiedUser)

            // when & then
            shouldThrowExactly<DuplicateEmailException> {
                authService.create(notVerifiedUser)
            }
            verify(notVerifiedUserRepository, times(1)).findByEmail(notVerifiedUser.email)
        }

        should("throw DuplicateEmailException when creating user and there exists verified user with given mail") {
            // given
            val notVerifiedUser = createNotVerifiedUser(email = DUMMY_EMAIL)
            val verifiedUser = createVerifiedUser(email = DUMMY_EMAIL)
            whenever(notVerifiedUserRepository.findByEmail(DUMMY_EMAIL)).thenReturn(null)
            whenever(verifiedUserRepository.findByEmail(DUMMY_EMAIL)).thenReturn(verifiedUser)

            // when & then
            shouldThrowExactly<DuplicateEmailException> {
                authService.create(notVerifiedUser)
            }
            verify(notVerifiedUserRepository, times(1)).findByEmail(DUMMY_EMAIL)
            verify(verifiedUserRepository, times(1)).findByEmail(DUMMY_EMAIL)
        }

        should("get verified user") {
            // given
            val verifiedUser = createVerifiedUser(email = DUMMY_EMAIL)
            whenever(verifiedUserRepository.findByEmail(DUMMY_EMAIL)).thenReturn(verifiedUser)

            // when
            val result = authService.getVerifiedUser(DUMMY_EMAIL)

            // then
            result shouldBe verifiedUser
            verify(verifiedUserRepository, times(1)).findByEmail(DUMMY_EMAIL)
        }

        should("throw UserNotVerifiedException when getting verified user and user is not present") {
            // given
            whenever(verifiedUserRepository.findByEmail(DUMMY_EMAIL)).thenReturn(null)

            // when & then
            shouldThrowExactly<UserNotVerifiedException> {
                authService.getVerifiedUser(DUMMY_EMAIL)
            }
            verify(verifiedUserRepository, times(1)).findByEmail(DUMMY_EMAIL)
        }

        should("verify user") {
            // given
            val verification = createVerification(DUMMY_EMAIL, DUMMY_CODE)
            val notVerifiedUser = createNotVerifiedUser(email = DUMMY_EMAIL, code = DUMMY_CODE)
            val verifiedUser = createVerifiedUser(id = notVerifiedUser.id, email = notVerifiedUser.email, password = notVerifiedUser.password)

            whenever(notVerifiedUserRepository.findByEmail(verification.email)).thenReturn(notVerifiedUser)
            whenever(verifiedUserRepository.create(verifiedUser)).thenReturn(verifiedUser)

            // when
            authService.verify(verification)

            // then
            verify(notVerifiedUserRepository, times(1)).findByEmail(DUMMY_EMAIL)
            verify(notVerifiedUserRepository, times(1)).deleteById(notVerifiedUser.id)
            verify(verifiedUserRepository, times(1)).create(verifiedUser)
            verify(userDetailsManagerClient, times(1)).createUserDetails(anyVararg(UserDetails::class))
        }

        should("throw UserNotFoundException when verifying user and user does not exist") {
            // given
            val verification = createVerification(email = DUMMY_EMAIL)
            whenever(notVerifiedUserRepository.findByEmail(DUMMY_EMAIL)).thenReturn(null)

            // when & then
            shouldThrowExactly<UserNotFoundException> { authService.verify(verification) }
            verify(notVerifiedUserRepository, times(1)).findByEmail(DUMMY_EMAIL)
        }

        should("throw VerificationException when verifying user and code is not correct") {
            // given
            val verification = createVerification(email = DUMMY_EMAIL, code = DUMMY_CODE)
            val notVerifiedUser = createNotVerifiedUser(email = DUMMY_EMAIL, code = OTHER_DUMMY_CODE)
            whenever(notVerifiedUserRepository.findByEmail(DUMMY_EMAIL)).thenReturn(notVerifiedUser)

            // when & then
            shouldThrowExactly<VerificationException> { authService.verify(verification) }
            verify(notVerifiedUserRepository, times(1)).findByEmail(DUMMY_EMAIL)
        }

        should("send verification email") {
            // given
            val notVerifiedUser = createNotVerifiedUser(email = DUMMY_EMAIL, updatedCodeAt = now().minus(10, MINUTES))
            whenever(notVerifiedUserRepository.findByEmail(DUMMY_EMAIL)).thenReturn(notVerifiedUser)
            whenever(emailProperties.timeBetweenEmails).thenReturn(Duration.ofMinutes(5))

            // when
            authService.sendVerificationEmail(DUMMY_EMAIL)

            // then
            verify(notVerifiedUserRepository, times(1)).findByEmail(DUMMY_EMAIL)
            verify(notVerifiedUserRepository, times(1)).updateVerificationCode(anyVararg(String::class), anyVararg(String::class))
            verify(emailSenderClient, times(1)).sendVerificationEmail(anyVararg(VerificationEmailDetails::class))
        }

        should("throw UserNotFoundException when sending email and user do not exist") {
            // given
            whenever(notVerifiedUserRepository.findByEmail(DUMMY_EMAIL)).thenReturn(null)

            // when & then
            shouldThrowExactly<UserNotFoundException> { authService.sendVerificationEmail(DUMMY_EMAIL) }
            verify(notVerifiedUserRepository, times(1)).findByEmail(DUMMY_EMAIL)
        }

        should("throw EmailRecentlySentException when sending email and email was recently sent") {
            // given
            val notVerifiedUser = createNotVerifiedUser(email = DUMMY_EMAIL, updatedCodeAt = now().minus(4, MINUTES))
            whenever(notVerifiedUserRepository.findByEmail(DUMMY_EMAIL)).thenReturn(notVerifiedUser)
            whenever(emailProperties.timeBetweenEmails).thenReturn(Duration.ofMinutes(5))

            // then when
            shouldThrowExactly<EmailRecentlySentException> { authService.sendVerificationEmail(DUMMY_EMAIL) }
            verify(notVerifiedUserRepository, times(1)).findByEmail(DUMMY_EMAIL)
        }

        should("generate code") {
            // given when
            val code = authService.generateCode()

            // then
            code shouldHaveLength 6
            code shouldMatch "\\d+"
        }

        should("change password") {
            // given
            val newEncodedPassword = "encoded"
            whenever(verifiedUserRepository.findById(USER_ID)).thenReturn(createVerifiedUser(id = USER_ID, password = DUMMY_PASSWORD))
            whenever(passwordEncoder.matches(anyVararg(String::class), anyVararg(String::class))).thenReturn(true)
            whenever(passwordEncoder.encode(OTHER_DUMMY_PASSWORD)).thenReturn(newEncodedPassword)

            // when
            authService.changePassword(USER_ID, DUMMY_PASSWORD, OTHER_DUMMY_PASSWORD)

            // then
            verify(verifiedUserRepository, times(1)).findById(USER_ID)
            verify(passwordEncoder, times(1)).matches(anyVararg(String::class), anyVararg(String::class))
            verify(verifiedUserRepository, times(1)).updatePassword(USER_ID, newEncodedPassword)
            verify(passwordEncoder, times(1)).encode(OTHER_DUMMY_PASSWORD)
        }

        should("throw UserNotFoundException when changing password and user does not exist") {
            // given
            whenever(verifiedUserRepository.findById(USER_ID)).thenReturn(null)

            // when then
            shouldThrowExactly<UserNotFoundException> { authService.changePassword(USER_ID, DUMMY_PASSWORD, OTHER_DUMMY_PASSWORD) }
            verify(verifiedUserRepository, times(1)).findById(USER_ID)
        }

        should("throw WrongPasswordException when changing password and user does not exist") {
            // given
            whenever(verifiedUserRepository.findById(USER_ID)).thenReturn(createVerifiedUser(id = USER_ID, password = DUMMY_PASSWORD))
            whenever(passwordEncoder.matches(anyVararg(String::class), anyVararg(String::class))).thenReturn(false)

            // when then
            shouldThrowExactly<WrongPasswordException> { authService.changePassword(USER_ID, DUMMY_PASSWORD, OTHER_DUMMY_PASSWORD) }
            verify(verifiedUserRepository, times(1)).findById(USER_ID)
            verify(passwordEncoder, times(1)).matches(anyVararg(String::class), anyVararg(String::class))
            verify(verifiedUserRepository, times(0)).updatePassword(USER_ID, OTHER_DUMMY_PASSWORD)
        }

        should("send password-recovery email") {
            // given
            val verifiedUser = createVerifiedUser(id = USER_ID, email = DUMMY_EMAIL)
            val passwordRecoveryCode = createPasswordRecoveryCode(userId = USER_ID)
            whenever(verifiedUserRepository.findByEmail(DUMMY_EMAIL)).thenReturn(verifiedUser)
            whenever(passwordRecoveryCodeRepository.findByUserId(USER_ID)).thenReturn(null)
            whenever(passwordRecoveryCodeRepository.create(anyVararg(PasswordRecoveryCode::class))).thenReturn(passwordRecoveryCode)
            // when
            authService.sendPasswordRecoveryEmail(DUMMY_EMAIL)

            // then
            verify(verifiedUserRepository, times(1)).findByEmail(DUMMY_EMAIL)
            verify(passwordRecoveryCodeRepository, times(1)).findByUserId(USER_ID)
            verify(passwordRecoveryCodeRepository, times(1)).create(anyVararg(PasswordRecoveryCode::class))
            verify(emailSenderClient, times(1)).sendPasswordRecoveryEmail(anyVararg(String::class), anyVararg(String::class))
        }

        should("throw UserNotFoundException when sending password recovery mail and user doesn't exist") {
            // given
            whenever(verifiedUserRepository.findByEmail(DUMMY_EMAIL)).thenReturn(null)

            // when & then
            shouldThrowExactly<UserNotFoundException> { authService.sendPasswordRecoveryEmail(DUMMY_EMAIL) }

            verify(verifiedUserRepository, times(1)).findByEmail(DUMMY_EMAIL)
            verify(passwordRecoveryCodeRepository, times(0)).findByUserId(USER_ID)
            verify(passwordRecoveryCodeRepository, times(0)).create(anyVararg(PasswordRecoveryCode::class))
            verify(emailSenderClient, times(0)).sendPasswordRecoveryEmail(anyVararg(String::class), anyVararg(String::class))
        }

        should("throw EmailRecentlySentException when sending password recovery mail and email was recently sent") {
            // given
            val verifiedUser = createVerifiedUser(id = USER_ID, email = DUMMY_EMAIL)
            val passwordRecoveryCode = createPasswordRecoveryCode(userId = USER_ID)
            whenever(verifiedUserRepository.findByEmail(DUMMY_EMAIL)).thenReturn(verifiedUser)
            whenever(passwordRecoveryCodeRepository.findByUserId(USER_ID)).thenReturn(passwordRecoveryCode)

            // when & then
            shouldThrowExactly<EmailRecentlySentException> { authService.sendPasswordRecoveryEmail(DUMMY_EMAIL) }

            verify(verifiedUserRepository, times(1)).findByEmail(DUMMY_EMAIL)
            verify(passwordRecoveryCodeRepository, times(1)).findByUserId(USER_ID)
            verify(passwordRecoveryCodeRepository, times(0)).create(anyVararg(PasswordRecoveryCode::class))
            verify(emailSenderClient, times(0)).sendPasswordRecoveryEmail(anyVararg(String::class), anyVararg(String::class))
        }
    },
)
