package pl.edu.agh.gem.integration.client

import io.kotest.assertions.throwables.shouldNotThrowAny
import pl.edu.agh.gem.external.dto.userdetailsmanager.toUserDetailsCreationRequest
import pl.edu.agh.gem.integration.BaseIntegrationSpec
import pl.edu.agh.gem.integration.ability.stubUserDetailsCreation
import pl.edu.agh.gem.internal.client.UserDetailsManagerClient
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
    })
