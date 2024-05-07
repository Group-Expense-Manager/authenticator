package pl.edu.agh.gem.external.persistence

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import pl.edu.agh.gem.internal.model.auth.VerifiedUser
import pl.edu.agh.gem.internal.persistence.VerifiedUserRepository

@Repository
class MongoVerifiedUserRepository(
    private val mongo: MongoTemplate,
) : VerifiedUserRepository {

    override fun findByEmail(email: String): VerifiedUser? {
        val query = Query().addCriteria(where(VerifiedUser::email.name).`is`(email))
        return mongo.findOne(query, VerifiedUserEntity::class.java)?.toDomain()
    }

    override fun create(verifiedUser: VerifiedUser): VerifiedUser {
        return mongo.insert(verifiedUser.toEntity()).toDomain()
    }

    private fun VerifiedUser.toEntity() =
        VerifiedUserEntity(
            id = id,
            email = email,
            password = password,
        )
}
