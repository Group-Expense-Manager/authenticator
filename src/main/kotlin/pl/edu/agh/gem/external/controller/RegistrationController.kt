package pl.edu.agh.gem.external.controller

import jakarta.validation.Valid
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pl.edu.agh.gem.external.dto.RegistrationRequest
import pl.edu.agh.gem.internal.service.RegistrationService

@RestController
@RequestMapping("/api")
class RegistrationController(
    private val registrationService: RegistrationService,
) {
    @PostMapping("/register")
    fun register(
        @Valid @RequestBody registrationRequest: RegistrationRequest,
    ): ResponseEntity<Any> {
        return try {
            registrationService.create(registrationRequest.toDomain())
            ResponseEntity(CREATED)
        } catch (ex: DuplicateKeyException) {
            ResponseEntity(CONFLICT)
        }
    }
}
