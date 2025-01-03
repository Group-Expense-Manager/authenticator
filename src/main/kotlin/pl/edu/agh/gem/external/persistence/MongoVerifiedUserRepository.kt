package pl.edu.agh.gem.external.persistence

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update.update
import org.springframework.stereotype.Repository
import pl.edu.agh.gem.internal.model.auth.VerifiedUser
import pl.edu.agh.gem.internal.persistence.VerifiedUserRepository
import pl.edu.agh.gem.metrics.MeteredRepository

@Repository
@MeteredRepository
class MongoVerifiedUserRepository(
    private val mongo: MongoTemplate,
) : VerifiedUserRepository {
    override fun findById(id: String): VerifiedUser? {
        val query = Query().addCriteria(where(VerifiedUserEntity::id.name).`is`(id))
        return mongo.findOne(query, VerifiedUserEntity::class.java)?.toDomain()
    }

    override fun findByEmail(email: String): VerifiedUser? {
        val query = Query().addCriteria(where(VerifiedUserEntity::email.name).`is`(email))
        return mongo.findOne(query, VerifiedUserEntity::class.java)?.toDomain()
    }

    override fun create(verifiedUser: VerifiedUser): VerifiedUser {
        return mongo.insert(verifiedUser.toEntity()).toDomain()
    }

    override fun updatePassword(
        id: String,
        password: String,
    ) {
        mongo.updateFirst(Query.query(where("id").`is`(id)), update("password", password), VerifiedUserEntity::class.java)
    }

    private fun VerifiedUser.toEntity() =
        VerifiedUserEntity(
            id = id,
            email = email,
            password = password,
        )
}
