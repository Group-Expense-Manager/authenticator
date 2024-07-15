package pl.edu.agh.gem.external.dto.emailsender

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import pl.edu.agh.gem.util.createPasswordEmailDetails

class PasswordEmailRequestTest : ShouldSpec({

    should("map correctly from PasswordEmailDetails") {
        // given
        val passwordEmailDetails = createPasswordEmailDetails()

        // when
        val passwordRecoveryEmailRequest = PasswordEmailRequest.from(passwordEmailDetails)

        // then
        passwordRecoveryEmailRequest.also {
            it.email shouldBe passwordEmailDetails.email
            it.password shouldBe passwordEmailDetails.password
        }
    }
},)
