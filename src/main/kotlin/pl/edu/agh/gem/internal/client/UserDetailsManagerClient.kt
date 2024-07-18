package pl.edu.agh.gem.internal.client

import pl.edu.agh.gem.internal.model.userdetailsmanager.UserDetails

interface UserDetailsManagerClient {
    fun createUserDetails(userDetails: UserDetails)
    fun getUsername(userId: String): String
}
class UserDetailsManagerClientException(override val message: String?) : RuntimeException()

class RetryableUserDetailsManagerClientException(override val message: String?) : RuntimeException()
