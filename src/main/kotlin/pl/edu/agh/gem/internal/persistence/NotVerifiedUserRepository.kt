package pl.edu.agh.gem.internal.persistence

import pl.edu.agh.gem.internal.model.auth.NotVerifiedUser

interface NotVerifiedUserRepository {
    fun create(notVerifiedUser: NotVerifiedUser): NotVerifiedUser

    fun findByEmail(email: String): NotVerifiedUser?

    fun deleteById(userId: String)

    fun updateVerificationCode(
        id: String,
        newCode: String,
    )
}
