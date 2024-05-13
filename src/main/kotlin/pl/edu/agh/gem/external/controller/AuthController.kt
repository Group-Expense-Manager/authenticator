package pl.edu.agh.gem.external.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus.CREATED
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import pl.edu.agh.gem.external.dto.auth.RegistrationRequest
import pl.edu.agh.gem.internal.model.auth.NotVerifiedUser
import pl.edu.agh.gem.internal.service.AuthService
import pl.edu.agh.gem.media.InternalApiMediaType.APPLICATION_JSON_INTERNAL_VER_1
import java.time.LocalDateTime
import java.util.UUID.randomUUID

@RestController
@RequestMapping
class AuthController(
    private val authService: AuthService,
    private val passwordEncoder: PasswordEncoder,
) {
    @PostMapping("/open/register", consumes = [APPLICATION_JSON_INTERNAL_VER_1])
    @ResponseStatus(CREATED)
    fun register(
        @Valid @RequestBody
        registrationRequest: RegistrationRequest,
    ) {
        authService.create(registrationRequest.toDomain())
    }

    private fun RegistrationRequest.toDomain() =
        NotVerifiedUser(
            id = randomUUID().toString(),
            email = email,
            password = passwordEncoder.encode(password),
            createdAt = LocalDateTime.now(),
            code = authService.generateCode(),
            codeUpdatedAt = LocalDateTime.now(),
        )
}
