package pl.edu.agh.gem.integration.controller

import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.HttpStatus.CREATED
import pl.edu.agh.gem.assertion.shouldHaveErrors
import pl.edu.agh.gem.assertion.shouldHaveHttpStatus
import pl.edu.agh.gem.assertion.shouldHaveValidationError
import pl.edu.agh.gem.integration.BaseIntegrationSpec
import pl.edu.agh.gem.integration.ability.ServiceTestClient
import pl.edu.agh.gem.integration.ability.stubEmailSenderVerification
import pl.edu.agh.gem.internal.persistence.NotVerifiedUserRepository
import pl.edu.agh.gem.util.createRegistrationRequest
import pl.edu.agh.gem.util.saveNotVerifiedUser

class AuthControllerIT(
    private val service: ServiceTestClient,
    private val notVerifiedUserRepository: NotVerifiedUserRepository,
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

    should("return validation exception when email is blank") {
        // given
        val registrationRequest = createRegistrationRequest(email = "")

        // when
        val response = service.register(registrationRequest)

        // then
        response shouldHaveHttpStatus BAD_REQUEST
        response shouldHaveValidationError "Email can not be blank"
    }

    should("return validation exception when email has wrong format") {
        // given
        val registrationRequest = createRegistrationRequest(email = "email")

        // when
        val response = service.register(registrationRequest)

        // then
        response shouldHaveHttpStatus BAD_REQUEST
        response shouldHaveValidationError "Wrong email format"
    }

    should("return validation exception when password is blank") {
        // given
        val registrationRequest = createRegistrationRequest(password = "")

        // when
        val response = service.register(registrationRequest)

        // then
        response shouldHaveHttpStatus BAD_REQUEST
        response shouldHaveValidationError "Password can not be blank"
    }

    should("return validation exception when password is too short") {
        // given
        val registrationRequest = createRegistrationRequest(password = "pswd")

        // when
        val response = service.register(registrationRequest)

        // then
        response shouldHaveHttpStatus BAD_REQUEST
        response shouldHaveValidationError "Minimum password length is 8"
    }

    should("return validation exception when password is too long") {
        // given
        val registrationRequest = createRegistrationRequest(password = "passwordpasswordpasswordpassword")

        // when
        val response = service.register(registrationRequest)

        // then
        response shouldHaveHttpStatus BAD_REQUEST
        response shouldHaveValidationError "Maximum password length is 30"
    }

    should("return validation exception when password does not contain any lowercase letter") {
        // given
        val registrationRequest = createRegistrationRequest(password = "PASSWORD")

        // when
        val response = service.register(registrationRequest)

        // then
        response shouldHaveHttpStatus BAD_REQUEST
        response shouldHaveValidationError "Password must contain at least one lowercase letter"
    }

    should("return validation exception when password does not contain any uppercase letter") {
        // given
        val registrationRequest = createRegistrationRequest(password = "password")

        // when
        val response = service.register(registrationRequest)

        // then
        response shouldHaveHttpStatus BAD_REQUEST
        response shouldHaveValidationError "Password must contain at least one uppercase letter"
    }

    should("return validation exception when password does not contain any digit") {
        // given
        val registrationRequest = createRegistrationRequest(password = "password")

        // when
        val response = service.register(registrationRequest)

        // then
        response shouldHaveHttpStatus BAD_REQUEST
        response shouldHaveValidationError "Password must contain at least one digit"
    }

    should("return validation exception when password does not contain any special character") {
        // given
        val registrationRequest = createRegistrationRequest(password = "password")

        // when
        val response = service.register(registrationRequest)

        // then
        response shouldHaveHttpStatus BAD_REQUEST
        response shouldHaveValidationError "Password must contain at least one special character among @#\$%^&+=!"
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
            errors[0].code shouldBe "DuplicateEmailException"
        }
    }
},)
