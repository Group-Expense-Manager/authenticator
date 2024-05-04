package pl.edu.agh.gem.external.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.OK
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import pl.edu.agh.gem.external.dto.auth.LoginRequest
import pl.edu.agh.gem.external.dto.auth.RegistrationRequest
import pl.edu.agh.gem.external.dto.auth.VerificationEmailRequest
import pl.edu.agh.gem.external.dto.auth.VerificationRequest
import pl.edu.agh.gem.internal.model.auth.NotVerifiedUser
import pl.edu.agh.gem.internal.service.AuthService
import pl.edu.agh.gem.media.InternalApiMediaType.APPLICATION_JSON_INTERNAL_VER_1
import pl.edu.agh.gem.security.JwtService
import java.time.LocalDateTime
import java.util.UUID.randomUUID

@RestController
@RequestMapping
class AuthController(
    private val authService: AuthService,
    private val jwtService: JwtService,
    private val authManager: AuthenticationManager,
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

    @ResponseStatus(OK)
    @PostMapping("/open/login", consumes = [APPLICATION_JSON_INTERNAL_VER_1])
    fun login(
        @Valid @RequestBody
        loginRequest: LoginRequest,
    ): String {
        authManager.authenticate(
            UsernamePasswordAuthenticationToken(
                loginRequest.email,
                loginRequest.password,
            ),
        )

        val verifiedUser = authService.getVerifiedUser(loginRequest.email)
        return jwtService.createToken(verifiedUser)
    }

    @PostMapping("/open/verify", consumes = [APPLICATION_JSON_INTERNAL_VER_1])
    @ResponseStatus(OK)
    fun verify(
        @Valid @RequestBody
        verificationRequest: VerificationRequest,
    ): String {
        val verifiedUser = authService.verify(verificationRequest.toDomain())
        return jwtService.createToken(verifiedUser)
    }

    @PostMapping("/open/send-verification-email", consumes = [APPLICATION_JSON_INTERNAL_VER_1])
    @ResponseStatus(OK)
    fun sendVerificationEmail(
        @Valid @RequestBody
        verificationEmailRequest: VerificationEmailRequest,
    ) {
        authService.sendVerificationEmail(verificationEmailRequest.email)
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
