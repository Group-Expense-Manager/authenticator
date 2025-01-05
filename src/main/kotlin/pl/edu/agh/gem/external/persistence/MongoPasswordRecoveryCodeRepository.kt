package pl.edu.agh.gem.external.persistence

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import pl.edu.agh.gem.internal.model.auth.PasswordRecoveryCode
import pl.edu.agh.gem.internal.persistence.PasswordRecoveryCodeRepository
import pl.edu.agh.gem.metrics.MeteredRepository

@Repository
@MeteredRepository
class MongoPasswordRecoveryCodeRepository(
    private val mongo: MongoTemplate,
) : PasswordRecoveryCodeRepository {
    override fun create(passwordRecoveryCode: PasswordRecoveryCode): PasswordRecoveryCode {
        return mongo.insert(passwordRecoveryCode.toEntity()).toDomain()
    }

    override fun findByUserId(userId: String): PasswordRecoveryCode? {
        val query = Query().addCriteria(where(PasswordRecoveryCodeEntity::userId.name).`is`(userId))
        return mongo.findOne(query, PasswordRecoveryCodeEntity::class.java)?.toDomain()
    }

    override fun deleteByUserId(userId: String) {
        val query = Query().addCriteria(where(PasswordRecoveryCodeEntity::userId.name).`is`(userId))
        mongo.remove(query, PasswordRecoveryCodeEntity::class.java)
    }

    private fun PasswordRecoveryCode.toEntity() =
        PasswordRecoveryCodeEntity(
            userId = userId,
            code = code,
            createdAt = createdAt,
        )
}
