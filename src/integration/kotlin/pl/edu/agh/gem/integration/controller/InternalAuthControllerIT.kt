package pl.edu.agh.gem.integration.controller

import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.OK
import pl.edu.agh.gem.assertion.shouldBody
import pl.edu.agh.gem.assertion.shouldHaveErrors
import pl.edu.agh.gem.assertion.shouldHaveHttpStatus
import pl.edu.agh.gem.external.dto.auth.EmailAddressResponse
import pl.edu.agh.gem.helper.user.DummyUser.EMAIL
import pl.edu.agh.gem.helper.user.DummyUser.USER_ID
import pl.edu.agh.gem.integration.BaseIntegrationSpec
import pl.edu.agh.gem.integration.ability.ServiceTestClient
import pl.edu.agh.gem.internal.persistence.VerifiedUserRepository
import pl.edu.agh.gem.internal.service.UserNotFoundException
import pl.edu.agh.gem.util.saveVerifiedUser

class InternalAuthControllerIT(
    private val service: ServiceTestClient,
    private val verifiedUserRepository: VerifiedUserRepository,
) : BaseIntegrationSpec({

    should("return email address") {
        // given
        saveVerifiedUser(id = USER_ID, verifiedUserRepository = verifiedUserRepository)

        // when
        val response = service.getEmailAddress(USER_ID)

        // then
        response shouldHaveHttpStatus OK
        response.shouldBody<EmailAddressResponse> {
            email shouldBe EMAIL
        }
    }
    should("return UserNotFound when user don't exist") {
        // given & when
        val response = service.getEmailAddress(USER_ID)

        // then
        response shouldHaveHttpStatus NOT_FOUND
        response shouldHaveErrors {
            errors.first().code shouldBe UserNotFoundException::class.simpleName
        }
    }
},)
