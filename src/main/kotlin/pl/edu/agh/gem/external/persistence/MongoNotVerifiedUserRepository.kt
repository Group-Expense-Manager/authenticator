package pl.edu.agh.gem.external.persistence

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository
import pl.edu.agh.gem.internal.model.auth.NotVerifiedUser
import pl.edu.agh.gem.internal.persistence.NotVerifiedUserRepository
import pl.edu.agh.gem.metrics.MeteredRepository
import java.time.Instant.now

@Repository
@MeteredRepository
class MongoNotVerifiedUserRepository(
    private val mongo: MongoTemplate,
) : NotVerifiedUserRepository {
    override fun create(notVerifiedUser: NotVerifiedUser): NotVerifiedUser {
        return mongo.insert(notVerifiedUser.toEntity()).toDomain()
    }

    override fun findByEmail(email: String): NotVerifiedUser? {
        val query = Query().addCriteria(where(NotVerifiedUserEntity::email.name).`is`(email))
        return mongo.findOne(query, NotVerifiedUserEntity::class.java)?.toDomain()
    }

    override fun deleteById(userId: String) {
        val query = Query().addCriteria(where(NotVerifiedUserEntity::id.name).`is`(userId))

        mongo.remove(query, NotVerifiedUserEntity::class.java)
    }

    override fun updateVerificationCode(
        id: String,
        newCode: String,
    ) {
        val query = Query(where(NotVerifiedUserEntity::id.name).`is`(id))
        val update = Update()
        update.set(NotVerifiedUserEntity::code.name, newCode)
        update.set(NotVerifiedUserEntity::codeUpdatedAt.name, now())
        mongo.updateFirst(query, update, NotVerifiedUserEntity::class.java)
    }

    private fun NotVerifiedUser.toEntity() =
        NotVerifiedUserEntity(
            id = id,
            username = username,
            email = email,
            password = password,
            createdAt = createdAt,
            code = code,
            codeUpdatedAt = codeUpdatedAt,
        )
}
