package pl.edu.agh.gem.external.dto.auth

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class EmailAddressResponseTest : ShouldSpec({

    should("map to EmailAddressResponse") {
        // given
        val email = "test@gmail.com"

        // when
        val emailAddressResponse = email.toEmailAddressResponse()

        // then
        emailAddressResponse.email shouldBe email
    }
})
