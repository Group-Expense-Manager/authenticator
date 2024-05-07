package pl.edu.agh.gem.integration.controller

import io.kotest.datatest.withData
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

    context("return validation exception cause:") {
        withData(
            nameFn = { it.first },
            Pair("Email can not be blank", createRegistrationRequest(email = "")),
            Pair("Wrong email format", createRegistrationRequest(email = "email")),
            Pair("Password can not be blank", createRegistrationRequest(password = "")),
            Pair("Minimum password length is 8", createRegistrationRequest(password = "pswd")),
            Pair("Maximum password length is 30", createRegistrationRequest(password = "passwordpasswordpasswordpassword")),
            Pair("Password must contain at least one lowercase letter", createRegistrationRequest(password = "PASSWORD")),
            Pair("Password must contain at least one uppercase letter", createRegistrationRequest(password = "password")),
            Pair("Password must contain at least one digit", createRegistrationRequest(password = "password")),
            Pair("Password must contain at least one special character among @#\$%^&+=!", createRegistrationRequest(password = "password")),
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
            errors[0].code shouldBe "DuplicateEmailException"
        }
    }
},)
