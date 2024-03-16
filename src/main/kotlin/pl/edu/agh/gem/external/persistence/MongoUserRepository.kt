package pl.edu.agh.gem.external.persistence

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Repository
import pl.edu.agh.gem.internal.domain.User
import pl.edu.agh.gem.internal.persistence.UserRepository

@Repository
class MongoUserRepository(
    private val mongo: MongoTemplate,
    private val passwordEncoder: PasswordEncoder,
) : UserRepository {
    override fun create(user: User): User {
        return mongo.insert(user.toEntity()).toDomain()
    }

    private fun User.toEntity() =
        UserEntity(
            username = username,
            password = passwordEncoder.encode(password),
        )
}
