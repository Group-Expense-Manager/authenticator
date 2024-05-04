package pl.edu.agh.gem.external.persistence

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import pl.edu.agh.gem.internal.persistence.VerifiedUserRepository

@Repository
class MongoVerifiedUserRepository(
    private val mongo: MongoTemplate,
) : VerifiedUserRepository {

    override fun existByEmail(email: String): Boolean {
        val query = Query().addCriteria(where("email").`is`(email))
        return mongo.exists(query, VerifiedUserEntity::class.java)
    }
}
