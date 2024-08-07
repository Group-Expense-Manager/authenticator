package pl.edu.agh.gem.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import java.time.Duration

@Configuration
class ClientConfig {

    @Bean
    @Qualifier("EmailSenderClientRestTemplate")
    fun emailSenderClientRestTemplate(emailSenderClientProperties: EmailSenderClientProperties): RestTemplate {
        return RestTemplateBuilder()
            .setConnectTimeout(emailSenderClientProperties.connectTimeout)
            .setReadTimeout(emailSenderClientProperties.readTimeout)
            .build()
    }

    @Bean
    @Qualifier("UserDetailsManagerClientRestTemplate")
    fun userDetailsManagerClientRestTemplate(
        userDetailsManagerClientProperties: UserDetailsManagerClientProperties,
    ): RestTemplate {
        return RestTemplateBuilder()
            .setConnectTimeout(userDetailsManagerClientProperties.connectTimeout)
            .setReadTimeout(userDetailsManagerClientProperties.readTimeout)
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
