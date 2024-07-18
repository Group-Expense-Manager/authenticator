package pl.edu.agh.gem.external.client

import io.github.resilience4j.retry.annotation.Retry
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate
import pl.edu.agh.gem.config.UserDetailsManagerClientProperties
import pl.edu.agh.gem.external.dto.userdetailsmanager.InternalUsernameResponse
import pl.edu.agh.gem.external.dto.userdetailsmanager.toUserDetailsCreationRequest
import pl.edu.agh.gem.headers.HeadersUtils.withAppAcceptType
import pl.edu.agh.gem.headers.HeadersUtils.withAppContentType
import pl.edu.agh.gem.internal.client.RetryableUserDetailsManagerClientException
import pl.edu.agh.gem.internal.client.UserDetailsManagerClient
import pl.edu.agh.gem.internal.client.UserDetailsManagerClientException
import pl.edu.agh.gem.internal.model.userdetailsmanager.UserDetails
import pl.edu.agh.gem.paths.Paths.INTERNAL

@Component
class RestUserDetailsManagerClient(
    @Qualifier("UserDetailsManagerClientRestTemplate") val restTemplate: RestTemplate,
    private val userDetailsManagerClientProperties: UserDetailsManagerClientProperties,
) : UserDetailsManagerClient {

    @Retry(name = "userDetailsManager")
    override fun createUserDetails(userDetails: UserDetails) {
        try {
            restTemplate.exchange(
                resolveUserDetailsCreationAddress(),
                POST,
                HttpEntity(userDetails.toUserDetailsCreationRequest(), HttpHeaders().withAppContentType()),
                Any::class.java,
            )
        } catch (ex: HttpClientErrorException) {
            logger.warn(ex) { "Client side exception while trying to create UserDetails" }
            throw UserDetailsManagerClientException(ex.message)
        } catch (ex: HttpServerErrorException) {
            logger.warn(ex) { "Server side exception while trying to create UserDetails" }
            throw RetryableUserDetailsManagerClientException(ex.message)
        } catch (ex: Exception) {
            logger.warn(ex) { "Unexpected exception while trying to create UserDetails" }
            throw UserDetailsManagerClientException(ex.message)
        }
    }

    @Retry(name = "userDetailsManager")
    override fun getUsername(userId: String): String {
        return try {
            restTemplate.exchange(
                resolveUserDetailsUsernameAddress(userId),
                GET,
                HttpEntity<Any>(HttpHeaders().withAppAcceptType()),
                InternalUsernameResponse::class.java,
            ).body?.username ?: throw UserDetailsManagerClientException("While trying to retrieve username we receive empty body")
        } catch (ex: HttpClientErrorException) {
            logger.warn(ex) { "Client side exception while trying to get username" }
            throw UserDetailsManagerClientException(ex.message)
        } catch (ex: HttpServerErrorException) {
            logger.warn(ex) { "Server side exception while trying to get username" }
            throw RetryableUserDetailsManagerClientException(ex.message)
        } catch (ex: Exception) {
            logger.warn(ex) { "Unexpected exception while trying to get username" }
            throw UserDetailsManagerClientException(ex.message)
        }
    }

    private fun resolveUserDetailsCreationAddress() =
        "${userDetailsManagerClientProperties.url}/$INTERNAL/user-details"
    private fun resolveUserDetailsUsernameAddress(userId: String) =
        "${userDetailsManagerClientProperties.url}/$INTERNAL/user-details/username/$userId"

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
