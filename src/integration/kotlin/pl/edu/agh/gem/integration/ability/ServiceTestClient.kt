package pl.edu.agh.gem.integration.ability

import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec
import org.springframework.test.web.servlet.client.MockMvcWebTestClient.bindToApplicationContext
import org.springframework.web.context.WebApplicationContext
import pl.edu.agh.gem.external.dto.auth.LoginRequest
import pl.edu.agh.gem.external.dto.auth.PasswordChangeRequest
import pl.edu.agh.gem.external.dto.auth.PasswordRecoveryRequest
import pl.edu.agh.gem.external.dto.auth.RegistrationRequest
import pl.edu.agh.gem.external.dto.auth.VerificationEmailRequest
import pl.edu.agh.gem.external.dto.auth.VerificationRequest
import pl.edu.agh.gem.headers.HeadersUtils.withAppContentType
import pl.edu.agh.gem.headers.HeadersUtils.withValidatedUser
import pl.edu.agh.gem.paths.Paths.EXTERNAL
import pl.edu.agh.gem.paths.Paths.OPEN
import pl.edu.agh.gem.security.GemUser
import java.net.URI

@Component
@Lazy
class ServiceTestClient(applicationContext: WebApplicationContext) {
    private val webClient = bindToApplicationContext(applicationContext)
        .configureClient()
        .build()

    fun register(body: RegistrationRequest): ResponseSpec {
        return webClient.post()
            .uri(URI("$OPEN/register"))
            .headers { it.withAppContentType() }
            .bodyValue(body)
            .exchange()
    }

    fun login(body: LoginRequest): ResponseSpec {
        return webClient.post()
            .uri(URI("$OPEN/login"))
            .headers { it.withAppContentType() }
            .bodyValue(body)
            .exchange()
    }

    fun verify(body: VerificationRequest): ResponseSpec {
        return webClient.post()
            .uri(URI("$OPEN/verify"))
            .headers { it.withAppContentType() }
            .bodyValue(body)
            .exchange()
    }

    fun sendVerificationEmail(body: VerificationEmailRequest): ResponseSpec {
        return webClient.post()
            .uri(URI("$OPEN/send-verification-email"))
            .headers { it.withAppContentType() }
            .bodyValue(body)
            .exchange()
    }

    fun changePassword(body: PasswordChangeRequest, gemUser: GemUser): ResponseSpec {
        return webClient.put()
            .uri(URI("$EXTERNAL/change-password"))
            .headers { it.withAppContentType().withValidatedUser(gemUser) }
            .bodyValue(body)
            .exchange()
    }

    fun recoverPassword(body: PasswordRecoveryRequest): ResponseSpec {
        return webClient.post()
            .uri(URI("$OPEN/recover-password"))
            .headers { it.withAppContentType() }
            .bodyValue(body)
            .exchange()
    }

    fun sendPassword(email: String, code: String): ResponseSpec {
        return webClient.post()
            .uri {
                it.path("$OPEN/send-password")
                    .queryParam("email", email)
                    .queryParam("code", code)
                    .build()
            }
            .exchange()
    }
}
