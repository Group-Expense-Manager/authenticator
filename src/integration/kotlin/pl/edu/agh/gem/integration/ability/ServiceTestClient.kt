package pl.edu.agh.gem.integration.ability

import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec
import org.springframework.test.web.servlet.client.MockMvcWebTestClient.bindToApplicationContext
import org.springframework.web.context.WebApplicationContext
import pl.edu.agh.gem.external.dto.auth.RegistrationRequest
import pl.edu.agh.gem.headers.HeadersUtils.withAppContentType
import java.net.URI

@Component
@Lazy
class ServiceTestClient(applicationContext: WebApplicationContext) {
    private val webClient = bindToApplicationContext(applicationContext)
        .configureClient()
        .build()

    fun register(body: RegistrationRequest): ResponseSpec {
        return webClient.post()
            .uri(URI("/open/register"))
            .headers { it.withAppContentType() }
            .bodyValue(body)
            .exchange()
    }
}
