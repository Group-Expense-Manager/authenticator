package pl.edu.agh.gem.external.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import pl.edu.agh.gem.internal.model.auth.NotVerifiedUser
import java.time.Instant

@Document("not-verified")
data class NotVerifiedUserEntity(
    @Id
    val id: String,
    val username: String,
    val email: String,
    val password: String,
    @Indexed(expireAfter = "P30D")
    val createdAt: Instant,
    val code: String,
    val codeUpdatedAt: Instant,
) {
    fun toDomain() =
        NotVerifiedUser(
            id = id,
            username = username,
            email = email,
            password = password,
            createdAt = createdAt,
            code = code,
            codeUpdatedAt = codeUpdatedAt,
        )
}
