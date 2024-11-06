package pl.edu.agh.gem.external.dto.emailsender

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import pl.edu.agh.gem.helper.user.DummyUser.EMAIL
import pl.edu.agh.gem.helper.user.DummyUser.USER_ID
import pl.edu.agh.gem.util.DummyData.DUMMY_PASSWORD
import pl.edu.agh.gem.util.createPasswordEmailDetails

class PasswordEmailRequestTest : ShouldSpec({

    should("map correctly from PasswordEmailDetails") {
        // given
        val passwordEmailDetails = createPasswordEmailDetails(
            userId = USER_ID,
            email = EMAIL,
            password = DUMMY_PASSWORD,
        )

        // when
        val passwordRecoveryEmailRequest = PasswordEmailRequest.from(passwordEmailDetails)

        // then
        passwordRecoveryEmailRequest.also {
            it.userId shouldBe USER_ID
            it.email shouldBe EMAIL
            it.password shouldBe DUMMY_PASSWORD
        }
    }
},)
