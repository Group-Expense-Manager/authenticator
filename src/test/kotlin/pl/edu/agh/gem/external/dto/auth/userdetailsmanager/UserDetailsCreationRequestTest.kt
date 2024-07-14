package pl.edu.agh.gem.external.dto.auth.userdetailsmanager

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import pl.edu.agh.gem.external.dto.userdetailsmanager.toUserDetailsCreationRequest
import pl.edu.agh.gem.util.createUserDetails

class UserDetailsCreationRequestTest : ShouldSpec({
    should("map from UserDetails to UserDetailsCreationRequest correctly") {
        // given
        val userDetails = createUserDetails()
        // when
        val userDetailsCreationRequest = userDetails.toUserDetailsCreationRequest()

        // then
        userDetailsCreationRequest.also {
            it.userId shouldBe userDetails.userId
            it.username shouldBe userDetails.username
        }
    }
},)
