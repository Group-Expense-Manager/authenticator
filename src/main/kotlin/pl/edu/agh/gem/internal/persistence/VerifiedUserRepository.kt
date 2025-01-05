package pl.edu.agh.gem.internal.persistence

import pl.edu.agh.gem.internal.model.auth.VerifiedUser

interface VerifiedUserRepository {
    fun findById(id: String): VerifiedUser?

    fun findByEmail(email: String): VerifiedUser?

    fun create(verifiedUser: VerifiedUser): VerifiedUser

    fun updatePassword(
        id: String,
        password: String,
    )
}
