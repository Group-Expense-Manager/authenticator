package pl.edu.agh.gem.internal.service

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldHaveLength
import io.kotest.matchers.string.shouldMatch
import org.mockito.kotlin.anyVararg
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.crypto.password.PasswordEncoder
import pl.edu.agh.gem.helper.user.DummyUser.EMAIL
import pl.edu.agh.gem.helper.user.DummyUser.USER_ID
import pl.edu.agh.gem.internal.client.EmailSenderClient
import pl.edu.agh.gem.internal.client.UserDetailsManagerClient
import pl.edu.agh.gem.internal.model.auth.PasswordRecoveryCode
import pl.edu.agh.gem.internal.model.emailsender.PasswordEmailDetails
import pl.edu.agh.gem.internal.model.emailsender.PasswordRecoveryEmailDetails
import pl.edu.agh.gem.internal.model.emailsender.VerificationEmailDetails
import pl.edu.agh.gem.internal.model.userdetailsmanager.UserDetails
import pl.edu.agh.gem.internal.persistence.NotVerifiedUserRepository
import pl.edu.agh.gem.internal.persistence.PasswordRecoveryCodeRepository
import pl.edu.agh.gem.internal.persistence.VerifiedUserRepository
import pl.edu.agh.gem.util.DummyData.DUMMY_CODE
import pl.edu.agh.gem.util.DummyData.DUMMY_PASSWORD
import pl.edu.agh.gem.util.DummyData.DUMMY_USERNAME
import pl.edu.agh.gem.util.DummyData.OTHER_DUMMY_CODE
import pl.edu.agh.gem.util.DummyData.OTHER_DUMMY_PASSWORD
import pl.edu.agh.gem.util.createNotVerifiedUser
import pl.edu.agh.gem.util.createPasswordRecoveryCode
import pl.edu.agh.gem.util.createVerification
import pl.edu.agh.gem.util.createVerificationEmailDetails
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
            val emailDetails = createVerificationEmailDetails(
                notVerifiedUser.username,
                notVerifiedUser.email,
                notVerifiedUser.code,
            )

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
            val notVerifiedUser = createNotVerifiedUser(email = EMAIL)
            val verifiedUser = createVerifiedUser(email = EMAIL)
            whenever(notVerifiedUserRepository.findByEmail(EMAIL)).thenReturn(null)
            whenever(verifiedUserRepository.findByEmail(EMAIL)).thenReturn(verifiedUser)

            // when & then
            shouldThrowExactly<DuplicateEmailException> {
                authService.create(notVerifiedUser)
            }
            verify(notVerifiedUserRepository, times(1)).findByEmail(EMAIL)
            verify(verifiedUserRepository, times(1)).findByEmail(EMAIL)
        }

        should("get verified user") {
            // given
            val verifiedUser = createVerifiedUser(email = EMAIL)
            whenever(verifiedUserRepository.findByEmail(EMAIL)).thenReturn(verifiedUser)

            // when
            val result = authService.getVerifiedUser(EMAIL)

            // then
            result shouldBe verifiedUser
            verify(verifiedUserRepository, times(1)).findByEmail(EMAIL)
        }

        should("throw UserNotVerifiedException when getting verified user and user is not present") {
            // given
            whenever(verifiedUserRepository.findByEmail(EMAIL)).thenReturn(null)

            // when & then
            shouldThrowExactly<UserNotVerifiedException> {
                authService.getVerifiedUser(EMAIL)
            }
            verify(verifiedUserRepository, times(1)).findByEmail(EMAIL)
        }

        should("verify user") {
            // given
            val verification = createVerification(EMAIL, DUMMY_CODE)
            val notVerifiedUser = createNotVerifiedUser(email = EMAIL, code = DUMMY_CODE)
            val verifiedUser = createVerifiedUser(id = notVerifiedUser.id, email = notVerifiedUser.email, password = notVerifiedUser.password)

            whenever(notVerifiedUserRepository.findByEmail(verification.email)).thenReturn(notVerifiedUser)
            whenever(verifiedUserRepository.create(verifiedUser)).thenReturn(verifiedUser)

            // when
            authService.verify(verification)

            // then
            verify(notVerifiedUserRepository, times(1)).findByEmail(EMAIL)
            verify(notVerifiedUserRepository, times(1)).deleteById(notVerifiedUser.id)
            verify(verifiedUserRepository, times(1)).create(verifiedUser)
            verify(userDetailsManagerClient, times(1)).createUserDetails(anyVararg(UserDetails::class))
        }

        should("throw UserNotFoundException when verifying user and user does not exist") {
            // given
            val verification = createVerification(email = EMAIL)
            whenever(notVerifiedUserRepository.findByEmail(EMAIL)).thenReturn(null)

            // when & then
            shouldThrowExactly<UserNotFoundException> { authService.verify(verification) }
            verify(notVerifiedUserRepository, times(1)).findByEmail(EMAIL)
        }

        should("throw VerificationException when verifying user and code is not correct") {
            // given
            val verification = createVerification(email = EMAIL, code = DUMMY_CODE)
            val notVerifiedUser = createNotVerifiedUser(email = EMAIL, code = OTHER_DUMMY_CODE)
            whenever(notVerifiedUserRepository.findByEmail(EMAIL)).thenReturn(notVerifiedUser)

            // when & then
            shouldThrowExactly<VerificationException> { authService.verify(verification) }
            verify(notVerifiedUserRepository, times(1)).findByEmail(EMAIL)
        }

        should("send verification email") {
            // given
            val notVerifiedUser = createNotVerifiedUser(email = EMAIL, updatedCodeAt = now().minus(10, MINUTES))
            whenever(notVerifiedUserRepository.findByEmail(EMAIL)).thenReturn(notVerifiedUser)
            whenever(emailProperties.timeBetweenEmails).thenReturn(Duration.ofMinutes(5))
            // when
            authService.sendVerificationEmail(EMAIL)

            // then
            verify(notVerifiedUserRepository, times(1)).findByEmail(EMAIL)
            verify(notVerifiedUserRepository, times(1)).updateVerificationCode(anyVararg(String::class), anyVararg(String::class))
            verify(emailSenderClient, times(1)).sendVerificationEmail(anyVararg(VerificationEmailDetails::class))
        }

        should("throw UserNotFoundException when sending email and user do not exist") {
            // given
            whenever(notVerifiedUserRepository.findByEmail(EMAIL)).thenReturn(null)

            // when & then
            shouldThrowExactly<UserNotFoundException> { authService.sendVerificationEmail(EMAIL) }
            verify(notVerifiedUserRepository, times(1)).findByEmail(EMAIL)
        }

        should("throw EmailRecentlySentException when sending email and email was recently sent") {
            // given
            val notVerifiedUser = createNotVerifiedUser(email = EMAIL, updatedCodeAt = now().minus(4, MINUTES))
            whenever(notVerifiedUserRepository.findByEmail(EMAIL)).thenReturn(notVerifiedUser)
            whenever(emailProperties.timeBetweenEmails).thenReturn(Duration.ofMinutes(5))

            // then when
            shouldThrowExactly<EmailRecentlySentException> { authService.sendVerificationEmail(EMAIL) }
            verify(notVerifiedUserRepository, times(1)).findByEmail(EMAIL)
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
            val verifiedUser = createVerifiedUser(id = USER_ID, email = EMAIL)
            val passwordRecoveryCode = createPasswordRecoveryCode(userId = USER_ID)
            whenever(verifiedUserRepository.findByEmail(EMAIL)).thenReturn(verifiedUser)
            whenever(passwordRecoveryCodeRepository.findByUserId(USER_ID)).thenReturn(null)
            whenever(passwordRecoveryCodeRepository.create(anyVararg(PasswordRecoveryCode::class))).thenReturn(passwordRecoveryCode)
            whenever(userDetailsManagerClient.getUsername(verifiedUser.id)).thenReturn(DUMMY_USERNAME)

            // when
            authService.sendPasswordRecoveryEmail(EMAIL)

            // then
            verify(verifiedUserRepository, times(1)).findByEmail(EMAIL)
            verify(passwordRecoveryCodeRepository, times(1)).findByUserId(USER_ID)
            verify(passwordRecoveryCodeRepository, times(1)).create(anyVararg(PasswordRecoveryCode::class))
            verify(emailSenderClient, times(1)).sendPasswordRecoveryEmail(anyVararg(PasswordRecoveryEmailDetails::class))
            verify(userDetailsManagerClient, times(1)).getUsername(verifiedUser.id)
        }

        should("throw UserNotFoundException when sending password recovery mail and user doesn't exist") {
            // given
            whenever(verifiedUserRepository.findByEmail(EMAIL)).thenReturn(null)

            // when & then
            shouldThrowExactly<UserNotFoundException> { authService.sendPasswordRecoveryEmail(EMAIL) }

            verify(verifiedUserRepository, times(1)).findByEmail(EMAIL)
            verify(passwordRecoveryCodeRepository, times(0)).findByUserId(USER_ID)
            verify(passwordRecoveryCodeRepository, times(0)).create(anyVararg(PasswordRecoveryCode::class))
            verify(emailSenderClient, times(0)).sendPasswordRecoveryEmail(anyVararg(PasswordRecoveryEmailDetails::class))
        }

        should("throw EmailRecentlySentException when sending password recovery mail and email was recently sent") {
            // given
            val verifiedUser = createVerifiedUser(id = USER_ID, email = EMAIL)
            val passwordRecoveryCode = createPasswordRecoveryCode(userId = USER_ID)
            whenever(verifiedUserRepository.findByEmail(EMAIL)).thenReturn(verifiedUser)
            whenever(passwordRecoveryCodeRepository.findByUserId(USER_ID)).thenReturn(passwordRecoveryCode)

            // when & then
            shouldThrowExactly<EmailRecentlySentException> { authService.sendPasswordRecoveryEmail(EMAIL) }

            verify(verifiedUserRepository, times(1)).findByEmail(EMAIL)
            verify(passwordRecoveryCodeRepository, times(1)).findByUserId(USER_ID)
            verify(passwordRecoveryCodeRepository, times(0)).create(anyVararg(PasswordRecoveryCode::class))
            verify(emailSenderClient, times(0)).sendPasswordRecoveryEmail(anyVararg(PasswordRecoveryEmailDetails::class))
        }

        should("send email with password") {
            // given
            val newEncodedPassword = "encoded"
            val verifiedUser = createVerifiedUser(id = USER_ID, email = EMAIL)
            val passwordRecoveryCode = createPasswordRecoveryCode(userId = USER_ID, code = DUMMY_CODE)
            whenever(verifiedUserRepository.findByEmail(EMAIL)).thenReturn(verifiedUser)
            whenever(passwordRecoveryCodeRepository.findByUserId(USER_ID)).thenReturn(passwordRecoveryCode)
            whenever(passwordEncoder.encode(anyVararg(String::class))).thenReturn(newEncodedPassword)
            whenever(userDetailsManagerClient.getUsername(verifiedUser.id)).thenReturn(DUMMY_USERNAME)

            // when
            authService.sendPasswordEmail(EMAIL, DUMMY_CODE)

            // then
            verify(verifiedUserRepository, times(1)).findByEmail(EMAIL)
            verify(passwordRecoveryCodeRepository, times(1)).findByUserId(USER_ID)

            verify(verifiedUserRepository, times(1)).updatePassword(eq(USER_ID), anyVararg(String::class))
            verify(passwordRecoveryCodeRepository, times(1)).deleteByUserId(USER_ID)
            verify(passwordEncoder, times(1)).encode(anyVararg(String::class))
            verify(emailSenderClient, times(1)).sendPassword(anyVararg(PasswordEmailDetails::class))
            verify(userDetailsManagerClient, times(1)).getUsername(verifiedUser.id)
        }

        should("throw PasswordRecoveryException when sending email with password and user does not exist") {
            // given
            whenever(verifiedUserRepository.findByEmail(EMAIL)).thenReturn(null)

            // when & then
            shouldThrowExactly<PasswordRecoveryException> {
                authService.sendPasswordEmail(EMAIL, DUMMY_CODE)
            }

            verify(verifiedUserRepository, times(1)).findByEmail(EMAIL)
            verify(passwordRecoveryCodeRepository, times(0)).findByUserId(USER_ID)

            verify(verifiedUserRepository, times(0)).updatePassword(anyVararg(String::class), anyVararg(String::class))
            verify(passwordRecoveryCodeRepository, times(0)).deleteByUserId(anyVararg(String::class))
            verify(emailSenderClient, times(0)).sendPassword(anyVararg(PasswordEmailDetails::class))
        }

        should("throw PasswordRecoveryException when sending email with password and PasswordRecoveryCode does not exist") {
            // given
            val verifiedUser = createVerifiedUser(id = USER_ID, email = EMAIL)
            whenever(verifiedUserRepository.findByEmail(EMAIL)).thenReturn(verifiedUser)
            whenever(passwordRecoveryCodeRepository.findByUserId(USER_ID)).thenReturn(null)

            // when & then
            shouldThrowExactly<PasswordRecoveryException> {
                authService.sendPasswordEmail(EMAIL, DUMMY_CODE)
            }

            verify(verifiedUserRepository, times(1)).findByEmail(EMAIL)
            verify(passwordRecoveryCodeRepository, times(1)).findByUserId(USER_ID)

            verify(verifiedUserRepository, times(0)).updatePassword(anyVararg(String::class), anyVararg(String::class))
            verify(passwordRecoveryCodeRepository, times(0)).deleteByUserId(anyVararg(String::class))
            verify(emailSenderClient, times(0)).sendPassword(anyVararg(PasswordEmailDetails::class))
        }

        should("throw PasswordRecoveryException when sending email with password and code is not correct") {
            // given
            val verifiedUser = createVerifiedUser(id = USER_ID, email = EMAIL)
            val passwordRecoveryCode = createPasswordRecoveryCode(userId = USER_ID, code = DUMMY_CODE)
            whenever(verifiedUserRepository.findByEmail(EMAIL)).thenReturn(verifiedUser)
            whenever(passwordRecoveryCodeRepository.findByUserId(USER_ID)).thenReturn(passwordRecoveryCode)

            // when & then
            shouldThrowExactly<PasswordRecoveryException> {
                authService.sendPasswordEmail(EMAIL, OTHER_DUMMY_CODE)
            }

            verify(verifiedUserRepository, times(1)).findByEmail(EMAIL)
            verify(passwordRecoveryCodeRepository, times(1)).findByUserId(USER_ID)

            verify(verifiedUserRepository, times(0)).updatePassword(anyVararg(String::class), anyVararg(String::class))
            verify(passwordRecoveryCodeRepository, times(0)).deleteByUserId(anyVararg(String::class))
            verify(emailSenderClient, times(0)).sendPassword(anyVararg(PasswordEmailDetails::class))
        }

        should("return email address") {
            // given
            val verifiedUser = createVerifiedUser(id = USER_ID, email = EMAIL)
            whenever(verifiedUserRepository.findById(USER_ID)).thenReturn(verifiedUser)

            // when
            val email = authService.getEmailAddress(USER_ID)

            // then
            email shouldBe EMAIL
            verify(verifiedUserRepository, times(1)).findById(USER_ID)
        }

        should("return UserNotFound when getting email address") {
            // given
            whenever(verifiedUserRepository.findById(USER_ID)).thenReturn(null)

            // when & then
            shouldThrowExactly<UserNotFoundException> {
                authService.getEmailAddress(USER_ID)
            }

            verify(verifiedUserRepository, times(1)).findById(USER_ID)
        }
    },
)
