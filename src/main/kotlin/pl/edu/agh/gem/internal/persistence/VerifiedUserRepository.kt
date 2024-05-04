package pl.edu.agh.gem.internal.persistence

interface VerifiedUserRepository {
    fun existByEmail(email: String): Boolean
}
