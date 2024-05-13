package pl.edu.agh.gem.internal.model.auth

import java.time.LocalDateTime

data class NotVerifiedUser(
    val id: String,
    val email: String,
    val password: String,
    val createdAt: LocalDateTime,
    val code: String,
    val codeUpdatedAt: LocalDateTime,
)
