package pl.edu.agh.gem.internal.model.auth

import java.time.Instant

data class NotVerifiedUser(
    val id: String,
    val email: String,
    val password: String,
    val createdAt: Instant,
    val code: String,
    val codeUpdatedAt: Instant,
)
