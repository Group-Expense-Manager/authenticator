package pl.edu.agh.gem.internal.persistence

import pl.edu.agh.gem.internal.model.auth.VerifiedUser

interface VerifiedUserRepository {
    fun existByEmail(email: String): Boolean
    fun findByEmail(email: String): VerifiedUser?
    fun create(verifiedUser: VerifiedUser): VerifiedUser
}
