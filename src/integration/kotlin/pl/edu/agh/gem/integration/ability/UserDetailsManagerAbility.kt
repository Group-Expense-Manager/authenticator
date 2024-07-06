package pl.edu.agh.gem.integration.ability

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import pl.edu.agh.gem.external.dto.userdetailsmanager.UserDetailsCreationRequest
import pl.edu.agh.gem.headers.HeadersTestUtils.withAppContentType
import pl.edu.agh.gem.integration.environment.ProjectConfig
import pl.edu.agh.gem.paths.Paths.INTERNAL

private fun createUserDetailsUrl() = "$INTERNAL/user-details"

fun stubUserDetails(requestBody: UserDetailsCreationRequest, statusCode: HttpStatusCode = HttpStatus.CREATED) {
    ProjectConfig.wiremock.stubFor(
        post(urlMatching(createUserDetailsUrl()))
            .withRequestBody(
                equalToJson(
                    jacksonObjectMapper().writeValueAsString(requestBody),
                ),
            )
            .willReturn(
                aResponse()
                    .withStatus(statusCode.value())
                    .withAppContentType(),
            ),
    )
}
