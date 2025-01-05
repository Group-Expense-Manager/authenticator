package pl.edu.agh.gem.external.controller

import org.springframework.http.HttpStatus.OK
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import pl.edu.agh.gem.external.dto.auth.EmailAddressResponse
import pl.edu.agh.gem.external.dto.auth.toEmailAddressResponse
import pl.edu.agh.gem.internal.service.AuthService
import pl.edu.agh.gem.media.InternalApiMediaType.APPLICATION_JSON_INTERNAL_VER_1
import pl.edu.agh.gem.paths.Paths.INTERNAL

@RestController
@RequestMapping(INTERNAL)
class InternalAuthController(
    private val authService: AuthService,
) {
    @GetMapping("/users/{userId}/email", produces = [APPLICATION_JSON_INTERNAL_VER_1])
    @ResponseStatus(OK)
    fun getEmailAddress(
        @PathVariable userId: String,
    ): EmailAddressResponse {
        return authService.getEmailAddress(userId).toEmailAddressResponse()
    }
}
