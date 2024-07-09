package pl.edu.agh.gem.internal.model.auth

import java.time.Instant
import java.time.Instant.now

data class PasswordRecoveryCode(
    val userId: String,
    val code: String,
    val createdAt: Instant = now(),
)
