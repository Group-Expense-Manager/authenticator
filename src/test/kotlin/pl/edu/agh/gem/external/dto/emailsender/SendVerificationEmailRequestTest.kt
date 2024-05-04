package pl.edu.agh.gem.external.dto.emailsender

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import pl.edu.agh.gem.util.createEmailDetails

class SendVerificationEmailRequestTest : ShouldSpec({

    should("map correct from Verification") {
        // given
        val emailDetails = createEmailDetails(
            email = "my@mail.com",
            code = "123456",
        )
        // when
        val sendVerificationRequest = VerificationEmailRequest.from(emailDetails)

        // then
        sendVerificationRequest.also {
            it.email.shouldNotBeNull()
            it.email shouldBe "my@mail.com"
            it.code.shouldNotBeNull()
            it.code shouldBe "123456"
        }
    }
},)
