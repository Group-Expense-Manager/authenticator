package pl.edu.agh.gem.external.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import pl.edu.agh.gem.internal.model.auth.NotVerifiedUser
import java.time.LocalDateTime

@Document("not-verified")
data class NotVerifiedUserEntity(
    @Id
    val id: String,
    val email: String,
    val password: String,
    @Indexed(expireAfter = "P30D")
    val createdAt: LocalDateTime,
    val code: String,
    val codeUpdatedAt: LocalDateTime,
) {
    fun toDomain() =
        NotVerifiedUser(
            id = id,
            email = email,
            password = password,
            createdAt = createdAt,
            code = code,
            codeUpdatedAt = codeUpdatedAt,
        )
}
