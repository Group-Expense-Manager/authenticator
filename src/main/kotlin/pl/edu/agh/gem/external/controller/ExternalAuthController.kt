package pl.edu.agh.gem.external.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus.OK
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import pl.edu.agh.gem.external.dto.auth.PasswordChangeRequest
import pl.edu.agh.gem.internal.service.AuthService
import pl.edu.agh.gem.media.InternalApiMediaType.APPLICATION_JSON_INTERNAL_VER_1
import pl.edu.agh.gem.paths.Paths.EXTERNAL
import pl.edu.agh.gem.security.GemUserId

@RestController
@RequestMapping(EXTERNAL)
class ExternalAuthController(
    private val authService: AuthService,
) {

    @PutMapping("change-password", consumes = [APPLICATION_JSON_INTERNAL_VER_1])
    @ResponseStatus(OK)
    fun changePassword(
        @GemUserId userId: String,
        @Valid @RequestBody
        passwordChangeRequest: PasswordChangeRequest,
    ) {
        authService.changePassword(userId, passwordChangeRequest.oldPassword, passwordChangeRequest.newPassword)
    }
}
