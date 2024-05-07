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
import pl.edu.agh.gem.internal.client.EmailSenderClient
import pl.edu.agh.gem.internal.model.auth.VerifiedUser
import pl.edu.agh.gem.internal.model.emailsender.VerificationEmailDetails
import pl.edu.agh.gem.internal.persistence.NotVerifiedUserRepository
import pl.edu.agh.gem.internal.persistence.VerifiedUserRepository
import pl.edu.agh.gem.util.DummyData.DUMMY_CODE
import pl.edu.agh.gem.util.DummyData.DUMMY_EMAIL
import pl.edu.agh.gem.util.DummyData.OTHER_DUMMY_CODE
import pl.edu.agh.gem.util.createNotVerifiedUser
import pl.edu.agh.gem.util.createVerification
import pl.edu.agh.gem.util.createVerifiedUser

class AuthServiceTest : ShouldSpec(
    {
        val notVerifiedUserRepository = mock<NotVerifiedUserRepository>()
        val verifiedUserRepository = mock<VerifiedUserRepository>()
        val emailSenderClient = mock<EmailSenderClient>()
        val authService = AuthService(
            notVerifiedUserRepository,
            verifiedUserRepository,
            emailSenderClient,
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

            // when then
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

            // when then
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

            // when then
            shouldThrowExactly<UserNotVerifiedException> {
                authService.getVerifiedUser(DUMMY_EMAIL)
            }
            verify(verifiedUserRepository, times(1)).findByEmail(DUMMY_EMAIL)
        }

        should("verify user") {
            // given
            val verification = createVerification(DUMMY_EMAIL, DUMMY_CODE)
            val notVerifiedUser = createNotVerifiedUser(email = DUMMY_EMAIL, code = DUMMY_CODE)
            whenever(notVerifiedUserRepository.findByEmail(verification.email)).thenReturn(notVerifiedUser)

            // when
            authService.verify(verification)

            // then
            verify(notVerifiedUserRepository, times(1)).findByEmail(DUMMY_EMAIL)
            verify(notVerifiedUserRepository, times(1)).deleteById(notVerifiedUser.id)
            verify(verifiedUserRepository, times(1)).create(anyVararg(VerifiedUser::class))
        }

        should("throw UserNotFoundException when verifying user and user does not exist") {
            // given
            val verification = createVerification(email = DUMMY_EMAIL)
            whenever(notVerifiedUserRepository.findByEmail(DUMMY_EMAIL)).thenReturn(null)

            // when then
            shouldThrowExactly<UserNotFoundException> { authService.verify(verification) }
            verify(notVerifiedUserRepository, times(1)).findByEmail(DUMMY_EMAIL)
        }

        should("throw VerificationException when verifying user and code is not correct") {
            // given
            val verification = createVerification(email = DUMMY_EMAIL, code = DUMMY_CODE)
            val notVerifiedUser = createNotVerifiedUser(email = DUMMY_EMAIL, code = OTHER_DUMMY_CODE)
            whenever(notVerifiedUserRepository.findByEmail(DUMMY_EMAIL)).thenReturn(notVerifiedUser)

            // when then
            shouldThrowExactly<VerificationException> { authService.verify(verification) }
            verify(notVerifiedUserRepository, times(1)).findByEmail(DUMMY_EMAIL)
        }

        should("generate code") {
            // given when
            val code = authService.generateCode()

            // then
            code shouldHaveLength 6
            code shouldMatch "\\d+"
        }
    },
)
