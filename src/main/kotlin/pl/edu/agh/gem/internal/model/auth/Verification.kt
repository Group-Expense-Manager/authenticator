package pl.edu.agh.gem.internal.model.auth

data class Verification(
    val email: String,
    val code: String,
)
