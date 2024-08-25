package pl.edu.agh.gem.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import pl.edu.agh.gem.helper.http.GemRestTemplateFactory
import java.time.Duration

@Configuration
class ClientConfig {

    @Bean
    @Qualifier("EmailSenderClientRestTemplate")
    fun emailSenderClientRestTemplate(
        emailSenderClientProperties: EmailSenderClientProperties,
        gemRestTemplateFactory: GemRestTemplateFactory,
    ): RestTemplate {
        return gemRestTemplateFactory
            .builder()
            .withReadTimeout(emailSenderClientProperties.readTimeout)
            .withConnectTimeout(emailSenderClientProperties.connectTimeout)
            .build()
    }

    @Bean
    @Qualifier("UserDetailsManagerClientRestTemplate")
    fun userDetailsManagerClientRestTemplate(
        userDetailsManagerClientProperties: UserDetailsManagerClientProperties,
        gemRestTemplateFactory: GemRestTemplateFactory,
    ): RestTemplate {
        return gemRestTemplateFactory
            .builder()
            .withReadTimeout(userDetailsManagerClientProperties.readTimeout)
            .withConnectTimeout(userDetailsManagerClientProperties.connectTimeout)
            .build()
    }
}

@ConfigurationProperties(prefix = "email-sender-client")
data class EmailSenderClientProperties(
    val url: String,
    val connectTimeout: Duration,
    val readTimeout: Duration,
)

@ConfigurationProperties(prefix = "user-details-manager-client")
data class UserDetailsManagerClientProperties(
    val url: String,
    val connectTimeout: Duration,
    val readTimeout: Duration,
)
