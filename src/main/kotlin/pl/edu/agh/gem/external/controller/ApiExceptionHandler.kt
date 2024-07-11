package pl.edu.agh.gem.external.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.core.Ordered.LOWEST_PRECEDENCE
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.TOO_MANY_REQUESTS
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import pl.edu.agh.gem.error.SimpleError
import pl.edu.agh.gem.error.SimpleErrorsHolder
import pl.edu.agh.gem.error.handleError
import pl.edu.agh.gem.error.withCode
import pl.edu.agh.gem.error.withDetails
import pl.edu.agh.gem.error.withMessage
import pl.edu.agh.gem.error.withUserMessage
import pl.edu.agh.gem.internal.client.EmailSenderClientException
import pl.edu.agh.gem.internal.client.RetryableEmailSenderClientException
import pl.edu.agh.gem.internal.client.RetryableUserDetailsManagerClientException
import pl.edu.agh.gem.internal.client.UserDetailsManagerClientException
import pl.edu.agh.gem.internal.service.DuplicateEmailException
import pl.edu.agh.gem.internal.service.EmailRecentlySentException
import pl.edu.agh.gem.internal.service.UserNotFoundException
import pl.edu.agh.gem.internal.service.UserNotVerifiedException
import pl.edu.agh.gem.internal.service.VerificationException
import pl.edu.agh.gem.internal.service.WrongPasswordException

@ControllerAdvice
@Order(LOWEST_PRECEDENCE)
class ApiExceptionHandler {

    @ExceptionHandler(DuplicateEmailException::class)
    fun handleDuplicateEmailException(exception: DuplicateEmailException): ResponseEntity<SimpleErrorsHolder> {
        return ResponseEntity(handleError(exception), CONFLICT)
    }

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentialsException(exception: BadCredentialsException): ResponseEntity<SimpleErrorsHolder> {
        return ResponseEntity(handleError(exception), BAD_REQUEST)
    }

    @ExceptionHandler(UserNotVerifiedException::class)
    fun handleUserNotVerifiedException(exception: UserNotVerifiedException): ResponseEntity<SimpleErrorsHolder> {
        return ResponseEntity(handleError(exception), FORBIDDEN)
    }

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFoundException(exception: UserNotFoundException): ResponseEntity<SimpleErrorsHolder> {
        return ResponseEntity(handleError(exception), NOT_FOUND)
    }

    @ExceptionHandler(VerificationException::class)
    fun handleVerificationException(exception: VerificationException): ResponseEntity<SimpleErrorsHolder> {
        return ResponseEntity(handleError(exception), BAD_REQUEST)
    }

    @ExceptionHandler(EmailRecentlySentException::class)
    fun handleEmailRecentlySentException(exception: EmailRecentlySentException): ResponseEntity<SimpleErrorsHolder> {
        return ResponseEntity(handleError(exception), TOO_MANY_REQUESTS)
    }

    @ExceptionHandler(WrongPasswordException::class)
    fun handleWrongPasswordException(exception: WrongPasswordException): ResponseEntity<SimpleErrorsHolder> {
        return ResponseEntity(handleError(exception), BAD_REQUEST)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(
        exception: MethodArgumentNotValidException,
    ): ResponseEntity<SimpleErrorsHolder> {
        return ResponseEntity(handleNotValidException(exception), BAD_REQUEST)
    }

    @ExceptionHandler(RetryableUserDetailsManagerClientException::class)
    fun handleRetryableUserDetailsManagerClientException(
        exception: RetryableUserDetailsManagerClientException,
    ): ResponseEntity<SimpleErrorsHolder> {
        return ResponseEntity(handleError(exception), INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(UserDetailsManagerClientException::class)
    fun handleUserDetailsManagerClientException(
        exception: UserDetailsManagerClientException,
    ): ResponseEntity<SimpleErrorsHolder> {
        return ResponseEntity(handleError(exception), INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(RetryableEmailSenderClientException::class)
    fun handleRetryableEmailSenderClientException(
        exception: RetryableEmailSenderClientException,
    ): ResponseEntity<SimpleErrorsHolder> {
        return ResponseEntity(handleError(exception), INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(EmailSenderClientException::class)
    fun handleEmailSenderClientException(
        exception: EmailSenderClientException,
    ): ResponseEntity<SimpleErrorsHolder> {
        return ResponseEntity(handleError(exception), INTERNAL_SERVER_ERROR)
    }

    private fun handleNotValidException(exception: MethodArgumentNotValidException): SimpleErrorsHolder {
        val errors = exception.bindingResult.fieldErrors
            .map { error ->
                SimpleError()
                    .withCode("VALIDATION_ERROR")
                    .withDetails(error.field)
                    .withUserMessage(error.defaultMessage)
                    .withMessage(error.defaultMessage)
            }
        return SimpleErrorsHolder(errors).apply {
            jacksonObjectMapper().writeValueAsString(this)
        }
    }
}
