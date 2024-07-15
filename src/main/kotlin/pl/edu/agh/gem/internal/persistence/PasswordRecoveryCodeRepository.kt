package pl.edu.agh.gem.internal.persistence

import pl.edu.agh.gem.internal.model.auth.PasswordRecoveryCode

interface PasswordRecoveryCodeRepository {
    fun create(passwordRecoveryCode: PasswordRecoveryCode): PasswordRecoveryCode
    fun findByUserId(userId: String): PasswordRecoveryCode?
    fun deleteByUserId(userId: String)
}
