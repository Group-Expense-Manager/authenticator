package pl.edu.agh.gem.internal.persistence

import pl.edu.agh.gem.internal.model.auth.VerifiedUser

interface VerifiedUserRepository {
    fun findByEmail(email: String): VerifiedUser?
    fun create(verifiedUser: VerifiedUser): VerifiedUser
}
