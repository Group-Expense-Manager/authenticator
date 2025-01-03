package pl.edu.agh.gem.integration.ability

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import pl.edu.agh.gem.headers.HeadersTestUtils.withAppContentType
import pl.edu.agh.gem.integration.environment.ProjectConfig
import pl.edu.agh.gem.paths.Paths.INTERNAL

private fun sendVerificationMailUrl() = "$INTERNAL/verification"

private fun sendPasswordRecoveryMailUrl() = "$INTERNAL/recover-password"

private fun sendPasswordMailUrl() = "$INTERNAL/password"

fun stubEmailSenderVerification(statusCode: HttpStatusCode = HttpStatus.OK) {
    ProjectConfig.wiremock.stubFor(
        post(urlMatching(sendVerificationMailUrl()))
            .willReturn(
                aResponse()
                    .withStatus(statusCode.value())
                    .withAppContentType(),
            ),
    )
}

fun stubEmailSenderPasswordRecovery(statusCode: HttpStatusCode = HttpStatus.OK) {
    ProjectConfig.wiremock.stubFor(
        post(urlMatching(sendPasswordRecoveryMailUrl()))
            .willReturn(
                aResponse()
                    .withStatus(statusCode.value())
                    .withAppContentType(),
            ),
    )
}

fun stubEmailSenderPassword(statusCode: HttpStatusCode = HttpStatus.OK) {
    ProjectConfig.wiremock.stubFor(
        post(urlMatching(sendPasswordMailUrl()))
            .willReturn(
                aResponse()
                    .withStatus(statusCode.value())
                    .withAppContentType(),
            ),
    )
}
