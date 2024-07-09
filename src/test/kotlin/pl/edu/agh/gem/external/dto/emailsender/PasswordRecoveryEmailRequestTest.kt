package pl.edu.agh.gem.external.dto.emailsender

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import pl.edu.agh.gem.util.createPasswordRecoveryEmailDetails

class PasswordRecoveryEmailRequestTest : ShouldSpec({

    should("map correctly from PasswordRecoveryEmailDetails") {
        // given
        val passwordRecoveryEmailDetails = createPasswordRecoveryEmailDetails()

        // when
        val passwordRecoveryRecoveryEmailRequest = PasswordRecoveryEmailRequest.from(passwordRecoveryEmailDetails)

        // then
        passwordRecoveryRecoveryEmailRequest.also {
            it.email shouldBe passwordRecoveryEmailDetails.email
            it.link shouldBe passwordRecoveryEmailDetails.link
        }
    }
},)
