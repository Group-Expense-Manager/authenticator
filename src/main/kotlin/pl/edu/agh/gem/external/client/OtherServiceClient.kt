package pl.edu.agh.gem.external.client

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import pl.edu.agh.gem.config.ExampleProperties

@Component
class OtherServiceClient(
    @Qualifier("ExampleRestTemplate") val restTemplate: RestTemplate,
    val exampleProperties: ExampleProperties,
) {
    fun getSomeData(): String {
        val response = restTemplate.getForObject(exampleProperties.url, String::class.java)

        return response ?: "No data"
    }
}
