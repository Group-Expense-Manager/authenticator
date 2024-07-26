package pl.edu.agh.gem.integration.client

import io.kotest.assertions.throwables.shouldNotThrowAny
import pl.edu.agh.gem.external.dto.userdetailsmanager.toInternalUsernameResponse
import pl.edu.agh.gem.external.dto.userdetailsmanager.toUserDetailsCreationRequest
import pl.edu.agh.gem.helper.user.DummyUser.USER_ID
import pl.edu.agh.gem.integration.BaseIntegrationSpec
import pl.edu.agh.gem.integration.ability.stubGetUsername
import pl.edu.agh.gem.integration.ability.stubUserDetailsCreation
import pl.edu.agh.gem.internal.client.UserDetailsManagerClient
import pl.edu.agh.gem.util.DummyData.DUMMY_USERNAME
import pl.edu.agh.gem.util.createUserDetails

class UserDetailsManagerClientIT(
    private val userDetailsManagerClient: UserDetailsManagerClient,
) : BaseIntegrationSpec({
    should("create userDetails") {
        // given
        val userDetails = createUserDetails()
        stubUserDetailsCreation(userDetails.toUserDetailsCreationRequest())

        // when & then
        shouldNotThrowAny {
            userDetailsManagerClient.createUserDetails(userDetails)
        }
    }

    should("get username") {
        // given
        stubGetUsername(DUMMY_USERNAME.toInternalUsernameResponse(), USER_ID)

        // when & then
        shouldNotThrowAny {
            userDetailsManagerClient.getUsername(USER_ID)
        }
    }
},)
