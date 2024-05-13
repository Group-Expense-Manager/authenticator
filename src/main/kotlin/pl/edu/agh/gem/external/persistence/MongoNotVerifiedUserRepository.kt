package pl.edu.agh.gem.external.persistence

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import pl.edu.agh.gem.internal.model.auth.NotVerifiedUser
import pl.edu.agh.gem.internal.persistence.NotVerifiedUserRepository

@Repository
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

    private fun NotVerifiedUser.toEntity() =
        NotVerifiedUserEntity(
            id = id,
            email = email,
            password = password,
            createdAt = createdAt,
            code = code,
            codeUpdatedAt = codeUpdatedAt,
        )
}
