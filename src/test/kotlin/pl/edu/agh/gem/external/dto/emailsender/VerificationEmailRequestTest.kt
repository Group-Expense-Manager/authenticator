package pl.edu.agh.gem.external.dto.emailsender

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import pl.edu.agh.gem.helper.user.DummyUser.EMAIL
import pl.edu.agh.gem.util.DummyData.DUMMY_CODE
import pl.edu.agh.gem.util.DummyData.DUMMY_USERNAME
import pl.edu.agh.gem.util.createVerificationEmailDetails

class VerificationEmailRequestTest : ShouldSpec({

    should("map correct from Verification") {
        // given
        val emailDetails = createVerificationEmailDetails(
            username = DUMMY_USERNAME,
            email = EMAIL,
            code = DUMMY_CODE,
        )
        // when
        val sendVerificationRequest = VerificationEmailRequest.from(emailDetails)

        // then
        sendVerificationRequest.also {
            it.username shouldBe DUMMY_USERNAME
            it.email shouldBe EMAIL
            it.code shouldBe DUMMY_CODE
        }
    }
},)
