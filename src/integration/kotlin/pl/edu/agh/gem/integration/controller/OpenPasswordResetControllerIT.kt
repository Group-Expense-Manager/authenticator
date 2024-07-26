package pl.edu.agh.gem.integration.controller

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.contain
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.OK
import pl.edu.agh.gem.assertion.shouldBody
import pl.edu.agh.gem.assertion.shouldHaveHttpStatus
import pl.edu.agh.gem.external.dto.userdetailsmanager.toInternalUsernameResponse
import pl.edu.agh.gem.helper.user.DummyUser.EMAIL
import pl.edu.agh.gem.helper.user.DummyUser.USER_ID
import pl.edu.agh.gem.integration.BaseIntegrationSpec
import pl.edu.agh.gem.integration.ability.ServiceTestClient
import pl.edu.agh.gem.integration.ability.stubEmailSenderPassword
import pl.edu.agh.gem.integration.ability.stubGetUsername
import pl.edu.agh.gem.internal.persistence.PasswordRecoveryCodeRepository
import pl.edu.agh.gem.internal.persistence.VerifiedUserRepository
import pl.edu.agh.gem.util.DummyData.DUMMY_CODE
import pl.edu.agh.gem.util.DummyData.DUMMY_USERNAME
import pl.edu.agh.gem.util.DummyData.OTHER_DUMMY_CODE
import pl.edu.agh.gem.util.createPasswordRecoveryCode
import pl.edu.agh.gem.util.createVerifiedUser

class OpenPasswordResetControllerIT(
    private val service: ServiceTestClient,
    private val verifiedUserRepository: VerifiedUserRepository,
    private val passwordRecoveryCodeRepository: PasswordRecoveryCodeRepository,
) : BaseIntegrationSpec({
    should("send password email") {
        // given
        val verifiedUser = createVerifiedUser(id = USER_ID, email = EMAIL)
        verifiedUserRepository.create(verifiedUser)
        val passwordRecoveryCode = createPasswordRecoveryCode(userId = USER_ID, code = DUMMY_CODE)
        passwordRecoveryCodeRepository.create(passwordRecoveryCode)

        stubEmailSenderPassword()
        stubGetUsername(DUMMY_USERNAME.toInternalUsernameResponse(), USER_ID)

        // when
        val response = service.sendPassword(email = EMAIL, code = DUMMY_CODE)

        // then
        response shouldHaveHttpStatus OK
        response.shouldBody<String> {
            contain("success")
        }

        passwordRecoveryCodeRepository.findByUserId(USER_ID).also {
            it.shouldBeNull()
        }

        verifiedUserRepository.findByEmail(EMAIL).also {
            it.shouldNotBeNull()
            it.password shouldNotBe verifiedUser.password
        }
    }

    should("rollback when sending password email and EmailSender fails") {
        // given
        val verifiedUser = createVerifiedUser(id = USER_ID, email = EMAIL)
        verifiedUserRepository.create(verifiedUser)
        val passwordRecoveryCode = createPasswordRecoveryCode(userId = USER_ID, code = DUMMY_CODE)
        passwordRecoveryCodeRepository.create(passwordRecoveryCode)

        stubEmailSenderPassword(statusCode = INTERNAL_SERVER_ERROR)
        stubGetUsername(DUMMY_USERNAME.toInternalUsernameResponse(), USER_ID)

        // when
        val response = service.sendPassword(email = EMAIL, code = DUMMY_CODE)

        // then
        response shouldHaveHttpStatus INTERNAL_SERVER_ERROR
        passwordRecoveryCodeRepository.findByUserId(USER_ID).also {
            it.shouldNotBeNull()
        }

        verifiedUserRepository.findByEmail(EMAIL).also {
            it.shouldNotBeNull()
            it.password shouldBe verifiedUser.password
        }
    }

    should("return BAD_REQUEST when sending password and user does not exist ") {
        // given
        stubEmailSenderPassword()

        // when
        val response = service.sendPassword(email = EMAIL, code = DUMMY_CODE)

        // then
        response shouldHaveHttpStatus BAD_REQUEST
        response.shouldBody<String> {
            contain("invalid")
        }
    }

    should("return BAD_REQUEST when sending password and passwordRecoveryCode does not exist ") {
        // given
        stubEmailSenderPassword()
        val verifiedUser = createVerifiedUser(id = USER_ID, email = EMAIL)
        verifiedUserRepository.create(verifiedUser)

        // when
        val response = service.sendPassword(email = EMAIL, code = DUMMY_CODE)

        // then
        response shouldHaveHttpStatus BAD_REQUEST
        response.shouldBody<String> {
            contain("invalid")
        }

        verifiedUserRepository.findByEmail(EMAIL).also {
            it.shouldNotBeNull()
            it.password shouldBe verifiedUser.password
        }
    }

    should("return BAD_REQUEST when sending password and code is not correct") {
        // given
        stubEmailSenderPassword()
        val verifiedUser = createVerifiedUser(id = USER_ID, email = EMAIL)
        verifiedUserRepository.create(verifiedUser)
        val passwordRecoveryCode = createPasswordRecoveryCode(userId = USER_ID, code = DUMMY_CODE)
        passwordRecoveryCodeRepository.create(passwordRecoveryCode)

        // when
        val response = service.sendPassword(email = EMAIL, code = OTHER_DUMMY_CODE)

        // then
        response shouldHaveHttpStatus BAD_REQUEST
        response.shouldBody<String> {
            contain("invalid")
        }
        passwordRecoveryCodeRepository.findByUserId(USER_ID).also {
            it.shouldNotBeNull()
        }
        verifiedUserRepository.findByEmail(EMAIL).also {
            it.shouldNotBeNull()
            it.password shouldBe verifiedUser.password
        }
    }
},)
