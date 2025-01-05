package pl.edu.agh.gem.external.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import pl.edu.agh.gem.internal.model.auth.PasswordRecoveryCode
import java.time.Instant

@Document("password-recovery-code")
data class PasswordRecoveryCodeEntity(
    @Id
    val userId: String,
    val code: String,
    @Indexed(expireAfter = "PT5M")
    val createdAt: Instant,
) {
    fun toDomain() =
        PasswordRecoveryCode(
            userId = userId,
            code = code,
            createdAt = createdAt,
        )
}
