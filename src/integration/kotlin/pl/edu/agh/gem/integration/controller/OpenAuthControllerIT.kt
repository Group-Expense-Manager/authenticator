package pl.edu.agh.gem.integration.controller

import io.kotest.datatest.withData
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.OK
import org.springframework.http.HttpStatus.TOO_MANY_REQUESTS
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import pl.edu.agh.gem.assertion.shouldBody
import pl.edu.agh.gem.assertion.shouldHaveErrors
import pl.edu.agh.gem.assertion.shouldHaveHttpStatus
import pl.edu.agh.gem.assertion.shouldHaveValidationError
import pl.edu.agh.gem.external.dto.ValidationMessage.CODE_NOT_BLANK
import pl.edu.agh.gem.external.dto.ValidationMessage.EMAIL_NOT_BLANK
import pl.edu.agh.gem.external.dto.ValidationMessage.MAX_PASSWORD_LENGTH
import pl.edu.agh.gem.external.dto.ValidationMessage.MIN_PASSWORD_LENGTH
import pl.edu.agh.gem.external.dto.ValidationMessage.PASSWORD_DIGIT
import pl.edu.agh.gem.external.dto.ValidationMessage.PASSWORD_LOWERCASE
import pl.edu.agh.gem.external.dto.ValidationMessage.PASSWORD_NOT_BLANK
import pl.edu.agh.gem.external.dto.ValidationMessage.PASSWORD_SPECIAL_CHARACTER
import pl.edu.agh.gem.external.dto.ValidationMessage.PASSWORD_UPPERCASE
import pl.edu.agh.gem.external.dto.ValidationMessage.USERNAME_PATTERN_MESSAGE
import pl.edu.agh.gem.external.dto.ValidationMessage.WRONG_EMAIL_FORMAT
import pl.edu.agh.gem.external.dto.auth.LoginResponse
import pl.edu.agh.gem.external.dto.auth.VerificationResponse
import pl.edu.agh.gem.external.dto.userdetailsmanager.toInternalUsernameResponse
import pl.edu.agh.gem.helper.user.DummyUser.EMAIL
import pl.edu.agh.gem.helper.user.DummyUser.USER_ID
import pl.edu.agh.gem.integration.BaseIntegrationSpec
import pl.edu.agh.gem.integration.ability.ServiceTestClient
import pl.edu.agh.gem.integration.ability.stubEmailSenderPasswordRecovery
import pl.edu.agh.gem.integration.ability.stubEmailSenderVerification
import pl.edu.agh.gem.integration.ability.stubGetUsername
import pl.edu.agh.gem.integration.ability.stubUserDetailsCreation
import pl.edu.agh.gem.internal.persistence.NotVerifiedUserRepository
import pl.edu.agh.gem.internal.persistence.PasswordRecoveryCodeRepository
import pl.edu.agh.gem.internal.persistence.VerifiedUserRepository
import pl.edu.agh.gem.internal.service.DuplicateEmailException
import pl.edu.agh.gem.internal.service.EmailRecentlySentException
import pl.edu.agh.gem.internal.service.UserNotFoundException
import pl.edu.agh.gem.internal.service.UserNotVerifiedException
import pl.edu.agh.gem.internal.service.VerificationException
import pl.edu.agh.gem.util.DummyData.DUMMY_CODE
import pl.edu.agh.gem.util.DummyData.DUMMY_PASSWORD
import pl.edu.agh.gem.util.DummyData.DUMMY_USERNAME
import pl.edu.agh.gem.util.DummyData.OTHER_DUMMY_CODE
import pl.edu.agh.gem.util.DummyData.OTHER_DUMMY_PASSWORD
import pl.edu.agh.gem.util.createLoginRequest
import pl.edu.agh.gem.util.createNotVerifiedUser
import pl.edu.agh.gem.util.createPasswordRecoveryCode
import pl.edu.agh.gem.util.createPasswordRecoveryRequest
import pl.edu.agh.gem.util.createRegistrationRequest
import pl.edu.agh.gem.util.createUserDetailsCreationRequest
import pl.edu.agh.gem.util.createVerificationEmailRequest
import pl.edu.agh.gem.util.createVerificationRequest
import pl.edu.agh.gem.util.saveNotVerifiedUser
import pl.edu.agh.gem.util.saveVerifiedUser
import java.time.Instant.now
import java.time.temporal.ChronoUnit.MINUTES

class OpenAuthControllerIT(
    private val service: ServiceTestClient,
    private val notVerifiedUserRepository: NotVerifiedUserRepository,
    private val verifiedUserRepository: VerifiedUserRepository,
    private val passwordRecoveryCodeRepository: PasswordRecoveryCodeRepository,
    private val passwordEncoder: PasswordEncoder,
) : BaseIntegrationSpec({
    should("register user") {
        // given
        stubEmailSenderVerification()
        val registrationRequest = createRegistrationRequest(username = DUMMY_USERNAME, email = EMAIL)

        // when
        val response = service.register(registrationRequest)

        // then
        response shouldHaveHttpStatus CREATED
        notVerifiedUserRepository.findByEmail(EMAIL).also {
            it.shouldNotBeNull()
            it.username shouldBe DUMMY_USERNAME
        }
    }

    should("rollback when registering user and emailSenderFails") {
        // given
        stubEmailSenderVerification(INTERNAL_SERVER_ERROR)
        val registrationRequest = createRegistrationRequest(email = EMAIL)

        // when
        val response = service.register(registrationRequest)

        // then
        response shouldHaveHttpStatus INTERNAL_SERVER_ERROR
        notVerifiedUserRepository.findByEmail(EMAIL).also {
            it.shouldBeNull()
        }
    }

    context("return validation exception cause:") {
        withData(
            nameFn = { it.first },
            Pair(USERNAME_PATTERN_MESSAGE, createRegistrationRequest(username = "")),
            Pair(USERNAME_PATTERN_MESSAGE, createRegistrationRequest(username = "oo")),
            Pair(USERNAME_PATTERN_MESSAGE, createRegistrationRequest(username = "ooooooooooooooooooooo")),
            Pair(USERNAME_PATTERN_MESSAGE, createRegistrationRequest(username = "user$#")),
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
        val registrationRequest = createRegistrationRequest(email = EMAIL.uppercase())
        saveNotVerifiedUser(email = EMAIL, notVerifiedUserRepository = notVerifiedUserRepository)

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
        stubEmailSenderVerification()
        val verifiedUser = saveVerifiedUser(
            email = EMAIL,
            password = passwordEncoder.encode(DUMMY_PASSWORD),
            verifiedUserRepository = verifiedUserRepository,
        )
        val loginRequest = createLoginRequest(email = EMAIL.uppercase(), password = DUMMY_PASSWORD)

        // when
        val response = service.login(loginRequest)

        // then
        response shouldHaveHttpStatus OK
        response.shouldBody<LoginResponse> {
            userId shouldBe verifiedUser.id
            token.shouldNotBeNull()
        }
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
        saveNotVerifiedUser(
            email = EMAIL,
            password = passwordEncoder.encode(DUMMY_PASSWORD),
            notVerifiedUserRepository = notVerifiedUserRepository,
        )
        val loginRequest = createLoginRequest(email = EMAIL, password = DUMMY_PASSWORD)

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
        saveVerifiedUser(email = EMAIL, password = passwordEncoder.encode(DUMMY_PASSWORD), verifiedUserRepository = verifiedUserRepository)
        val loginRequest = createLoginRequest(email = EMAIL, password = OTHER_DUMMY_PASSWORD)

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
        saveNotVerifiedUser(
            email = EMAIL,
            password = passwordEncoder.encode(DUMMY_PASSWORD),
            notVerifiedUserRepository = notVerifiedUserRepository,
        )
        val loginRequest = createLoginRequest(email = EMAIL, password = OTHER_DUMMY_PASSWORD)

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
        val notVerifiedUser = saveNotVerifiedUser(email = EMAIL, notVerifiedUserRepository = notVerifiedUserRepository)
        val verificationRequest = createVerificationRequest(email = EMAIL.uppercase(), code = notVerifiedUser.code)
        stubUserDetailsCreation(createUserDetailsCreationRequest(notVerifiedUser.id, notVerifiedUser.username))
        // when
        val response = service.verify(verificationRequest)

        // then
        response shouldHaveHttpStatus OK
        response.shouldBody<VerificationResponse> {
            userId shouldBe notVerifiedUser.id
            token.shouldNotBeNull()
        }
        notVerifiedUserRepository.findByEmail(EMAIL).also {
            it.shouldBeNull()
        }
        verifiedUserRepository.findByEmail(EMAIL).also {
            it.shouldNotBeNull()
        }
    }

    should("rollback when verifying user and user details creation fails") {
        // given
        val notVerifiedUser = saveNotVerifiedUser(email = EMAIL, notVerifiedUserRepository = notVerifiedUserRepository)
        val verificationRequest = createVerificationRequest(email = EMAIL, code = notVerifiedUser.code)
        stubUserDetailsCreation(createUserDetailsCreationRequest(notVerifiedUser.id, notVerifiedUser.username), INTERNAL_SERVER_ERROR)
        // when
        val response = service.verify(verificationRequest)

        // then
        response shouldHaveHttpStatus INTERNAL_SERVER_ERROR
        notVerifiedUserRepository.findByEmail(EMAIL).also {
            it.shouldNotBeNull()
        }
        verifiedUserRepository.findByEmail(EMAIL).also {
            it.shouldBeNull()
        }
    }

    should("return validation exception when code is blank") {
        // given
        val verificationRequest = createVerificationRequest(code = "")

        // when
        val response = service.verify(verificationRequest)

        // then
        response shouldHaveHttpStatus BAD_REQUEST
        response shouldHaveValidationError CODE_NOT_BLANK
    }

    should("return validation exception when email is blank") {
        // given
        val verificationRequest = createVerificationRequest(email = "")

        // when
        val response = service.verify(verificationRequest)

        // then
        response shouldHaveHttpStatus BAD_REQUEST
        response shouldHaveValidationError EMAIL_NOT_BLANK
    }

    should("return UserNotFoundException when verifying user does not exist") {
        // given
        val verificationRequest = createVerificationRequest()

        // when
        val response = service.verify(verificationRequest)

        // then
        response shouldHaveHttpStatus NOT_FOUND
        response shouldHaveErrors {
            errors.first().code shouldBe UserNotFoundException::class.simpleName
        }
    }

    should("return VerificationException when verifying and code is invalid") {
        // given
        saveNotVerifiedUser(email = EMAIL, code = DUMMY_CODE, notVerifiedUserRepository = notVerifiedUserRepository)
        val verificationRequest = createVerificationRequest(email = EMAIL, code = OTHER_DUMMY_CODE)

        // when
        val response = service.verify(verificationRequest)

        // then
        response shouldHaveHttpStatus BAD_REQUEST
        response shouldHaveErrors {
            errors.first().code shouldBe VerificationException::class.simpleName
        }
    }
    should("send verification email") {
        // given
        val notVerifiedUser = createNotVerifiedUser(email = EMAIL, updatedCodeAt = now().minus(10, MINUTES))
        notVerifiedUserRepository.create(notVerifiedUser)

        stubEmailSenderVerification()
        val verificationEmailRequest = createVerificationEmailRequest(EMAIL.uppercase())

        // when
        val response = service.sendVerificationEmail(verificationEmailRequest)

        // then
        response shouldHaveHttpStatus OK
        notVerifiedUserRepository.findByEmail(EMAIL).also {
            it.shouldNotBeNull()
            it.code shouldNotBe notVerifiedUser.code
        }
    }

    should("rollback when sending verification email and emailSender fails") {
        // given
        val notVerifiedUser = createNotVerifiedUser(email = EMAIL, updatedCodeAt = now().minus(10, MINUTES))
        notVerifiedUserRepository.create(notVerifiedUser)

        stubEmailSenderVerification(INTERNAL_SERVER_ERROR)
        val verificationEmailRequest = createVerificationEmailRequest(EMAIL)

        // when
        val response = service.sendVerificationEmail(verificationEmailRequest)

        // then
        response shouldHaveHttpStatus INTERNAL_SERVER_ERROR
        notVerifiedUserRepository.findByEmail(EMAIL).also {
            it.shouldNotBeNull()
            it.code shouldBe notVerifiedUser.code
        }
    }

    should("return validation exception when email is blank") {
        // given
        val email = ""
        saveNotVerifiedUser(
            email = email,
            updatedCodeAt = now().minus(10, MINUTES),
            notVerifiedUserRepository = notVerifiedUserRepository,
        )
        val verificationEmailRequest = createVerificationEmailRequest(email)

        // when
        val response = service.sendVerificationEmail(verificationEmailRequest)

        // then
        response shouldHaveHttpStatus BAD_REQUEST
        response shouldHaveValidationError EMAIL_NOT_BLANK
    }

    should("return UserNotFoundException when sending verification and user does not exist") {
        // given  when
        val verificationEmailRequest = createVerificationEmailRequest()
        val response = service.sendVerificationEmail(verificationEmailRequest)

        // then
        response shouldHaveHttpStatus NOT_FOUND
        response shouldHaveErrors {
            errors.first().code shouldBe UserNotFoundException::class.simpleName
        }
    }

    should("return EmailRecentlySentException when mail was recently sent") {
        // given
        saveNotVerifiedUser(email = EMAIL, notVerifiedUserRepository = notVerifiedUserRepository)
        stubEmailSenderVerification()
        val verificationEmailRequest = createVerificationEmailRequest(EMAIL)

        // when
        val response = service.sendVerificationEmail(verificationEmailRequest)

        // then
        response shouldHaveHttpStatus TOO_MANY_REQUESTS
        response shouldHaveErrors {
            errors.first().code shouldBe EmailRecentlySentException::class.simpleName
        }
    }

    should("send password-recovery email") {
        // given
        saveVerifiedUser(id = USER_ID, email = EMAIL, verifiedUserRepository = verifiedUserRepository)
        stubEmailSenderPasswordRecovery()
        stubGetUsername(DUMMY_USERNAME.toInternalUsernameResponse(), USER_ID)

        // when
        val response = service.recoverPassword(createPasswordRecoveryRequest(EMAIL.uppercase()))

        // then
        response shouldHaveHttpStatus OK
        passwordRecoveryCodeRepository.findByUserId(USER_ID).also {
            it.shouldNotBeNull()
        }
    }
    should("rollback when sending password-recovery and sending email fails") {
        // given
        saveVerifiedUser(id = USER_ID, email = EMAIL, verifiedUserRepository = verifiedUserRepository)
        stubEmailSenderPasswordRecovery(INTERNAL_SERVER_ERROR)
        stubGetUsername(DUMMY_USERNAME.toInternalUsernameResponse(), USER_ID)

        // when
        val response = service.recoverPassword(createPasswordRecoveryRequest(EMAIL))

        // then
        response shouldHaveHttpStatus INTERNAL_SERVER_ERROR
        passwordRecoveryCodeRepository.findByUserId(USER_ID).also {
            it.shouldBeNull()
        }
    }

    should("return validation exception when recovering password and email is blank") {
        // given
        val passwordRecoveryRequest = createPasswordRecoveryRequest("")

        // when
        val response = service.recoverPassword(passwordRecoveryRequest)

        // then
        response shouldHaveHttpStatus BAD_REQUEST
        response shouldHaveValidationError EMAIL_NOT_BLANK
    }

    should("return NOT_FOUND when recovering password and user does not exist") {
        // given
        val passwordRecoveryRequest = createPasswordRecoveryRequest(EMAIL)

        // when
        val response = service.recoverPassword(passwordRecoveryRequest)

        // then
        response shouldHaveHttpStatus NOT_FOUND
    }

    should("return TOO_MANY_REQUESTS when recovering password and email was recently sent") {
        // given
        val passwordRecoveryRequest = createPasswordRecoveryRequest(EMAIL)
        saveVerifiedUser(id = USER_ID, email = EMAIL, verifiedUserRepository = verifiedUserRepository)
        passwordRecoveryCodeRepository.create(createPasswordRecoveryCode(USER_ID))

        // when
        val response = service.recoverPassword(passwordRecoveryRequest)

        // then
        response shouldHaveHttpStatus TOO_MANY_REQUESTS
    }
},)
