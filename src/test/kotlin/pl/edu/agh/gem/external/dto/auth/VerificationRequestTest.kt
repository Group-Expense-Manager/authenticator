package pl.edu.agh.gem.external.dto.auth

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import pl.edu.agh.gem.util.createVerificationRequest

class VerificationRequestTest : ShouldSpec({

    should("map correct to Verification") {
        // given
        val verificationRequest = createVerificationRequest(
            email = "my@mail.com",
            code = "123456",
        )
        // when
        val verification = verificationRequest.toDomain()

        // then
        verification.also {
            it.email shouldBe "my@mail.com"
            it.code shouldBe "123456"
        }
    }
},)
