package pl.edu.agh.gem.external.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import pl.edu.agh.gem.internal.model.auth.VerifiedUser

@Document("verified")
data class VerifiedUserEntity(
    @Id
    val id: String,
    val email: String,
    val password: String,
) {
    fun toDomain() =
        VerifiedUser(
            id = id,
            email = email,
            password = password,
        )
}
