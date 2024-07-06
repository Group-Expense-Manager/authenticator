package pl.edu.agh.gem.integration.controller

import io.kotest.datatest.withData
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.OK
import org.springframework.security.crypto.password.PasswordEncoder
import pl.edu.agh.gem.assertion.shouldHaveErrors
import pl.edu.agh.gem.assertion.shouldHaveHttpStatus
import pl.edu.agh.gem.assertion.shouldHaveValidationError
import pl.edu.agh.gem.external.dto.ValidationMessage.MAX_PASSWORD_LENGTH
import pl.edu.agh.gem.external.dto.ValidationMessage.MIN_PASSWORD_LENGTH
import pl.edu.agh.gem.external.dto.ValidationMessage.PASSWORD_DIGIT
import pl.edu.agh.gem.external.dto.ValidationMessage.PASSWORD_LOWERCASE
import pl.edu.agh.gem.external.dto.ValidationMessage.PASSWORD_NOT_BLANK
import pl.edu.agh.gem.external.dto.ValidationMessage.PASSWORD_SPECIAL_CHARACTER
import pl.edu.agh.gem.external.dto.ValidationMessage.PASSWORD_UPPERCASE
import pl.edu.agh.gem.helper.user.DummyUser.EMAIL
import pl.edu.agh.gem.helper.user.DummyUser.USER_ID
import pl.edu.agh.gem.helper.user.createGemUser
import pl.edu.agh.gem.integration.BaseIntegrationSpec
import pl.edu.agh.gem.integration.ability.ServiceTestClient
import pl.edu.agh.gem.internal.persistence.VerifiedUserRepository
import pl.edu.agh.gem.internal.service.UserNotFoundException
import pl.edu.agh.gem.internal.service.WrongPasswordException
import pl.edu.agh.gem.security.GemUser
import pl.edu.agh.gem.util.DummyData.DUMMY_PASSWORD
import pl.edu.agh.gem.util.DummyData.OTHER_DUMMY_PASSWORD
import pl.edu.agh.gem.util.createPasswordChangeRequest
import pl.edu.agh.gem.util.saveVerifiedUser

class ExternalAuthControllerIT(
    private val service: ServiceTestClient,
    private val passwordEncoder: PasswordEncoder,
    private val verifiedUserRepository: VerifiedUserRepository,
) : BaseIntegrationSpec({

    should("change password") {
        // given
        val passwordChangeRequest = createPasswordChangeRequest(oldPassword = DUMMY_PASSWORD, newPassword = OTHER_DUMMY_PASSWORD)
        saveVerifiedUser(id = USER_ID, password = passwordEncoder.encode(DUMMY_PASSWORD), verifiedUserRepository = verifiedUserRepository)

        // when
        val response = service.changePassword(passwordChangeRequest, createGemUser(USER_ID, EMAIL))

        // then
        response shouldHaveHttpStatus OK

        verifiedUserRepository.findById(USER_ID).also {
            it.shouldNotBeNull()
            passwordEncoder.matches(OTHER_DUMMY_PASSWORD, it.password).shouldBeTrue()
        }
    }
    context("return validation exception cause:") {
        withData(
            nameFn = { it.first },
            Pair(PASSWORD_NOT_BLANK, createPasswordChangeRequest(oldPassword = "")),
            Pair(PASSWORD_NOT_BLANK, createPasswordChangeRequest(newPassword = "")),
            Pair(MIN_PASSWORD_LENGTH, createPasswordChangeRequest(newPassword = "pswd")),
            Pair(MAX_PASSWORD_LENGTH, createPasswordChangeRequest(newPassword = "passwordpasswordpasswordpassword")),
            Pair(PASSWORD_LOWERCASE, createPasswordChangeRequest(newPassword = "PASSWORD")),
            Pair(PASSWORD_UPPERCASE, createPasswordChangeRequest(newPassword = "password")),
            Pair(PASSWORD_DIGIT, createPasswordChangeRequest(newPassword = "password")),
            Pair(PASSWORD_SPECIAL_CHARACTER, createPasswordChangeRequest(newPassword = "Password")),
        ) { (expectedMessage, registrationRequest) ->
            // when
            val response = service.changePassword(registrationRequest, GemUser(USER_ID, EMAIL))

            // then
            response shouldHaveHttpStatus BAD_REQUEST
            response shouldHaveValidationError expectedMessage
        }
    }

    should("return UserNotFoundException when user does not exist") {
        // given
        val passwordChangeRequest = createPasswordChangeRequest(oldPassword = DUMMY_PASSWORD, newPassword = OTHER_DUMMY_PASSWORD)

        // when
        val response = service.changePassword(passwordChangeRequest, createGemUser(USER_ID, EMAIL))

        // then
        response shouldHaveHttpStatus NOT_FOUND
        response shouldHaveErrors {
            errors shouldHaveSize 1
            errors.first().code shouldBe UserNotFoundException::class.simpleName
        }
    }

    should("return WrongPasswordException when oldPassword is not correct") {
        // given
        val passwordChangeRequest = createPasswordChangeRequest(oldPassword = DUMMY_PASSWORD, newPassword = OTHER_DUMMY_PASSWORD)
        saveVerifiedUser(id = USER_ID, password = passwordEncoder.encode(OTHER_DUMMY_PASSWORD), verifiedUserRepository = verifiedUserRepository)

        // when
        val response = service.changePassword(passwordChangeRequest, createGemUser(USER_ID, EMAIL))

        // then
        response shouldHaveHttpStatus BAD_REQUEST
        response shouldHaveErrors {
            errors shouldHaveSize 1
            errors.first().code shouldBe WrongPasswordException::class.simpleName
        }
    }
},)
