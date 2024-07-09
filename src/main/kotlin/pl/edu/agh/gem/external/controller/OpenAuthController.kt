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
import pl.edu.agh.gem.external.dto.auth.LoginResponse
import pl.edu.agh.gem.external.dto.auth.PasswordRecoveryRequest
import pl.edu.agh.gem.external.dto.auth.RegistrationRequest
import pl.edu.agh.gem.external.dto.auth.VerificationEmailRequest
import pl.edu.agh.gem.external.dto.auth.VerificationRequest
import pl.edu.agh.gem.external.dto.auth.VerificationResponse
import pl.edu.agh.gem.internal.model.auth.NotVerifiedUser
import pl.edu.agh.gem.internal.service.AuthService
import pl.edu.agh.gem.media.InternalApiMediaType.APPLICATION_JSON_INTERNAL_VER_1
import pl.edu.agh.gem.paths.Paths.OPEN
import pl.edu.agh.gem.security.JwtService
import java.time.Instant.now
import java.util.UUID.randomUUID

@RestController
@RequestMapping(OPEN)
class OpenAuthController(
    private val authService: AuthService,
    private val jwtService: JwtService,
    private val authManager: AuthenticationManager,
    private val passwordEncoder: PasswordEncoder,
) {
    @PostMapping("register", consumes = [APPLICATION_JSON_INTERNAL_VER_1])
    @ResponseStatus(CREATED)
    fun register(
        @Valid @RequestBody
        registrationRequest: RegistrationRequest,
    ) {
        authService.create(registrationRequest.toDomain())
    }

    @ResponseStatus(OK)
    @PostMapping("login", consumes = [APPLICATION_JSON_INTERNAL_VER_1])
    fun login(
        @Valid @RequestBody
        loginRequest: LoginRequest,
    ): LoginResponse {
        authManager.authenticate(
            UsernamePasswordAuthenticationToken(
                loginRequest.email,
                loginRequest.password,
            ),
        )

        val verifiedUser = authService.getVerifiedUser(loginRequest.email)
        return LoginResponse(
            verifiedUser.id,
            jwtService.createToken(verifiedUser),
        )
    }

    @PostMapping("verify", consumes = [APPLICATION_JSON_INTERNAL_VER_1])
    @ResponseStatus(OK)
    fun verify(
        @Valid @RequestBody
        verificationRequest: VerificationRequest,
    ): VerificationResponse {
        val verifiedUser = authService.verify(verificationRequest.toDomain())
        return VerificationResponse(
            verifiedUser.id,
            jwtService.createToken(verifiedUser),
        )
    }

    @PostMapping("send-verification-email", consumes = [APPLICATION_JSON_INTERNAL_VER_1])
    @ResponseStatus(OK)
    fun sendVerificationEmail(
        @Valid @RequestBody
        verificationEmailRequest: VerificationEmailRequest,
    ) {
        authService.sendVerificationEmail(verificationEmailRequest.email)
    }

    @PostMapping("recover-password", consumes = [APPLICATION_JSON_INTERNAL_VER_1])
    @ResponseStatus(OK)
    fun recoverPassword(
        @Valid @RequestBody
        passwordRecoveryRequest: PasswordRecoveryRequest,
    ) {
        authService.sendPasswordRecoveryEmail(passwordRecoveryRequest.email)
    }

    private fun RegistrationRequest.toDomain() =
        NotVerifiedUser(
            id = randomUUID().toString(),
            email = email,
            password = passwordEncoder.encode(password),
            createdAt = now(),
            code = authService.generateCode(),
            codeUpdatedAt = now(),
        )
}
