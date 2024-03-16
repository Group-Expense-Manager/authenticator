package pl.edu.agh.gem.external.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import pl.edu.agh.gem.internal.domain.User
import java.util.UUID.randomUUID

@Document("users")
data class UserEntity(
    @Id
    val id: String = randomUUID().toString(),
    @Indexed(unique = true, background = true)
    val username: String,
    val password: String,
) {
    fun toDomain() =
        User(
            username = username,
            password = password,
        )
}
