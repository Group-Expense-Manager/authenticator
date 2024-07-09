package pl.edu.agh.gem.integration.client

import io.kotest.assertions.throwables.shouldNotThrowAny
import pl.edu.agh.gem.integration.BaseIntegrationSpec
import pl.edu.agh.gem.integration.ability.stubEmailSenderPasswordRecovery
import pl.edu.agh.gem.integration.ability.stubEmailSenderVerification
import pl.edu.agh.gem.internal.client.EmailSenderClient
import pl.edu.agh.gem.util.DummyData.DUMMY_CODE
import pl.edu.agh.gem.util.DummyData.DUMMY_EMAIL
import pl.edu.agh.gem.util.createVerificationEmailDetails

class EmailSenderClientIT(
    private val emailSenderClient: EmailSenderClient,
) : BaseIntegrationSpec({
    should("send verification email") {
        // given
        stubEmailSenderVerification()

        // when & then
        shouldNotThrowAny {
            emailSenderClient.sendVerificationEmail(createVerificationEmailDetails())
        }
    }

    should("send password-recovery email") {
        // given
        stubEmailSenderPasswordRecovery()

        // when & then
        shouldNotThrowAny {
            emailSenderClient.sendPasswordRecoveryEmail(DUMMY_EMAIL, DUMMY_CODE)
        }
    }
},)
