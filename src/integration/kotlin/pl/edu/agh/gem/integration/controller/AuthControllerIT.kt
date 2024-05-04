package pl.edu.agh.gem.integration.controller

import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.OK
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import pl.edu.agh.gem.assertion.shouldHaveErrors
import pl.edu.agh.gem.assertion.shouldHaveHttpStatus
import pl.edu.agh.gem.assertion.shouldHaveValidationError
import pl.edu.agh.gem.external.dto.ValidationMessage.Companion.EMAIL_NOT_BLANK
import pl.edu.agh.gem.external.dto.ValidationMessage.Companion.MAX_PASSWORD_LENGTH
import pl.edu.agh.gem.external.dto.ValidationMessage.Companion.MIN_PASSWORD_LENGTH
import pl.edu.agh.gem.external.dto.ValidationMessage.Companion.PASSWORD_DIGIT
import pl.edu.agh.gem.external.dto.ValidationMessage.Companion.PASSWORD_LOWERCASE
import pl.edu.agh.gem.external.dto.ValidationMessage.Companion.PASSWORD_NOT_BLANK
import pl.edu.agh.gem.external.dto.ValidationMessage.Companion.PASSWORD_SPECIAL_CHARACTER
import pl.edu.agh.gem.external.dto.ValidationMessage.Companion.PASSWORD_UPPERCASE
import pl.edu.agh.gem.external.dto.ValidationMessage.Companion.WRONG_EMAIL_FORMAT
import pl.edu.agh.gem.integration.BaseIntegrationSpec
import pl.edu.agh.gem.integration.ability.ServiceTestClient
import pl.edu.agh.gem.integration.ability.stubEmailSenderVerification
import pl.edu.agh.gem.internal.persistence.NotVerifiedUserRepository
import pl.edu.agh.gem.internal.persistence.VerifiedUserRepository
import pl.edu.agh.gem.internal.service.DuplicateEmailException
import pl.edu.agh.gem.internal.service.UserNotVerifiedException
import pl.edu.agh.gem.util.createLoginRequest
import pl.edu.agh.gem.util.createRegistrationRequest
import pl.edu.agh.gem.util.createVerificationRequest
import pl.edu.agh.gem.util.saveNotVerifiedUser
import pl.edu.agh.gem.util.saveVerifiedUser

class AuthControllerIT(
    private val service: ServiceTestClient,
    private val notVerifiedUserRepository: NotVerifiedUserRepository,
    private val verifiedUserRepository: VerifiedUserRepository,
    private val passwordEncoder: PasswordEncoder,
) : BaseIntegrationSpec({
    should("register user") {
        // given
        stubEmailSenderVerification()
        val registrationRequest = createRegistrationRequest()

        // when
        val response = service.register(registrationRequest)

        // then
        response shouldHaveHttpStatus CREATED
    }

    context("return validation exception cause:") {
        withData(
            nameFn = { it.first },
            Pair(EMAIL_NOT_BLANK, createRegistrationRequest(email = "")),
            Pair(WRONG_EMAIL_FORMAT, createRegistrationRequest(email = "email")),
            Pair(PASSWORD_NOT_BLANK, createRegistrationRequest(password = "")),
            Pair(MIN_PASSWORD_LENGTH, createRegistrationRequest(password = "pswd")),
            Pair(MAX_PASSWORD_LENGTH, createRegistrationRequest(password = "passwordpasswordpasswordpassword")),
            Pair(PASSWORD_LOWERCASE, createRegistrationRequest(password = "PASSWORD")),
            Pair(PASSWORD_UPPERCASE, createRegistrationRequest(password = "password")),
            Pair(PASSWORD_DIGIT, createRegistrationRequest(password = "password")),
            Pair(PASSWORD_SPECIAL_CHARACTER, createRegistrationRequest(password = "password")),
        ) { (expectedMessage, registrationRequest) ->
            // when
            val response = service.register(registrationRequest)

            // then
            response shouldHaveHttpStatus BAD_REQUEST
            response shouldHaveValidationError expectedMessage
        }
    }

    should("return DuplicateEmailException when user with given email already exists") {
        // given
        val email = "email@email.pl"
        val registrationRequest = createRegistrationRequest(email = email)
        saveNotVerifiedUser(email = email, notVerifiedUserRepository = notVerifiedUserRepository)

        // when
        val response = service.register(registrationRequest)

        // then
        response shouldHaveHttpStatus CONFLICT
        response shouldHaveErrors {
            errors.first().code shouldBe DuplicateEmailException::class.simpleName
        }
    }

    should("login user") {
        // given
        val email = "email@email.pl"
        val password = "Password1!"
        stubEmailSenderVerification()
        saveVerifiedUser(email = email, password = passwordEncoder.encode(password), verifiedUserRepository = verifiedUserRepository)
        val loginRequest = createLoginRequest(email = email, password = password)

        // when
        val response = service.login(loginRequest)

        // then
        response shouldHaveHttpStatus OK
    }

    should("return validation exception when email is blank") {
        // given
        val loginRequest = createLoginRequest(email = "")

        // when
        val response = service.login(loginRequest)

        // then
        response shouldHaveHttpStatus BAD_REQUEST
        response shouldHaveValidationError EMAIL_NOT_BLANK
    }

    should("return validation exception when password is blank") {
        // given
        val loginRequest = createLoginRequest(password = "")

        // when
        val response = service.login(loginRequest)

        // then
        response shouldHaveHttpStatus BAD_REQUEST
        response shouldHaveValidationError PASSWORD_NOT_BLANK
    }

    should("return UserNotVerifiedException when user is not verified") {
        // given
        val email = "email@email.pl"
        val password = "Password1!"
        saveNotVerifiedUser(email = email, password = passwordEncoder.encode(password), notVerifiedUserRepository = notVerifiedUserRepository)
        val loginRequest = createLoginRequest(email = email, password = password)

        // when
        val response = service.login(loginRequest)

        // then
        response shouldHaveHttpStatus FORBIDDEN
        response shouldHaveErrors {
            errors.first().code shouldBe UserNotVerifiedException::class.simpleName
        }
    }

    should("return BadCredentialsException when user is not registered") {
        // given
        val loginRequest = createLoginRequest()

        // when
        val response = service.login(loginRequest)

        // then
        response shouldHaveHttpStatus BAD_REQUEST
        response shouldHaveErrors {
            errors.first().code shouldBe BadCredentialsException::class.simpleName
        }
    }

    should("return BadCredentialsException when password is not correct for verified user") {
        // given
        val email = "email@email.pl"
        val correctPassword = "Password1!"
        val wrongPassword = "Password2!"
        saveVerifiedUser(email = email, password = passwordEncoder.encode(correctPassword), verifiedUserRepository = verifiedUserRepository)
        val loginRequest = createLoginRequest(email = email, password = wrongPassword)

        // when
        val response = service.login(loginRequest)

        // then
        response shouldHaveHttpStatus BAD_REQUEST
        response shouldHaveErrors {
            errors.first().code shouldBe BadCredentialsException::class.simpleName
        }
    }

    should("return BadCredentialsException when password is not correct for not verified user") {
        // given
        val email = "email@email.pl"
        val correctPassword = "Password1!"
        val wrongPassword = "Password2!"
        saveNotVerifiedUser(email = email, password = passwordEncoder.encode(correctPassword), notVerifiedUserRepository = notVerifiedUserRepository)
        val loginRequest = createLoginRequest(email = email, password = wrongPassword)

        // when
        val response = service.login(loginRequest)

        // then
        response shouldHaveHttpStatus BAD_REQUEST
        response shouldHaveErrors {
            errors.first().code shouldBe BadCredentialsException::class.simpleName
        }
    }

    should("Verify code") {
        // given
        val email = "email@email.com"
        val notVerifiedUser = saveNotVerifiedUser(email = email, notVerifiedUserRepository = notVerifiedUserRepository)
        val verificationRequest = createVerificationRequest(email = email, code = notVerifiedUser.code)

        // when
        val response = service.verify(verificationRequest)

        // then
        response shouldHaveHttpStatus OK
    }

    should("return validation exception when code is blank") {
        // given
        val verificationRequest = createVerificationRequest(code = "")

        // when
        val response = service.verify(verificationRequest)

        // then
        response shouldHaveHttpStatus BAD_REQUEST
        response shouldHaveValidationError "Code can not be blank"
    }

    should("return UserNotFoundException when verifying user do not exist") {
        // given
        val verificationRequest = createVerificationRequest()

        // when
        val response = service.verify(verificationRequest)

        // then
        response shouldHaveHttpStatus NOT_FOUND
        response shouldHaveErrors {
            errors[0].code shouldBe "UserNotFoundException"
        }
    }

    should("return VerificationException when verifying and code is invalid") {
        // given
        val email = "email@email.com"
        val correctCode = "123456"
        val wrongCode = "654321"
        saveNotVerifiedUser(email = email, code = correctCode, notVerifiedUserRepository = notVerifiedUserRepository)
        val verificationRequest = createVerificationRequest(email = email, code = wrongCode)

        // when
        val response = service.verify(verificationRequest)

        // then
        response shouldHaveHttpStatus BAD_REQUEST
        response shouldHaveErrors {
            errors[0].code shouldBe "VerificationException"
        }
    }
},)
