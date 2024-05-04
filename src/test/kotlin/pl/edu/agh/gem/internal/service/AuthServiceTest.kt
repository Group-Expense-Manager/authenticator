package pl.edu.agh.gem.internal.service

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.string.shouldHaveLength
import io.kotest.matchers.string.shouldMatch
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import pl.edu.agh.gem.internal.client.EmailSenderClient
import pl.edu.agh.gem.internal.model.emailsender.VerificationEmailDetails
import pl.edu.agh.gem.internal.persistence.NotVerifiedUserRepository
import pl.edu.agh.gem.internal.persistence.VerifiedUserRepository
import pl.edu.agh.gem.util.createNotVerifiedUser

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
            whenever(notVerifiedUserRepository.existByEmail(notVerifiedUser.email)).thenReturn(false)
            whenever(verifiedUserRepository.existByEmail(notVerifiedUser.email)).thenReturn(false)
            whenever(notVerifiedUserRepository.create(notVerifiedUser)).thenReturn(notVerifiedUser)
            val emailDetails = VerificationEmailDetails(notVerifiedUser.email, notVerifiedUser.code)

            // when
            authService.create(notVerifiedUser)

            // then
            verify(notVerifiedUserRepository, times(1)).existByEmail(notVerifiedUser.email)
            verify(verifiedUserRepository, times(1)).existByEmail(notVerifiedUser.email)
            verify(notVerifiedUserRepository, times(1)).create(notVerifiedUser)
            verify(emailSenderClient, times(1)).sendVerificationEmail(emailDetails)
        }

        should("throw DuplicateEmailException when creating user and there exists not verified user with given mail") {
            // given
            val notVerifiedUser = createNotVerifiedUser()
            whenever(notVerifiedUserRepository.existByEmail(notVerifiedUser.email)).thenReturn(true)

            // when then
            shouldThrowExactly<DuplicateEmailException> {
                authService.create(notVerifiedUser)
            }
            verify(notVerifiedUserRepository, times(1)).existByEmail(notVerifiedUser.email)
        }

        should("throw DuplicateEmailException when creating user and there exists verified user with given mail") {
            // given
            val notVerifiedUser = createNotVerifiedUser()
            whenever(notVerifiedUserRepository.existByEmail(notVerifiedUser.email)).thenReturn(false)
            whenever(verifiedUserRepository.existByEmail(notVerifiedUser.email)).thenReturn(true)

            // when then
            shouldThrowExactly<DuplicateEmailException> {
                authService.create(notVerifiedUser)
            }
            verify(notVerifiedUserRepository, times(1)).existByEmail(notVerifiedUser.email)
            verify(verifiedUserRepository, times(1)).existByEmail(notVerifiedUser.email)
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
