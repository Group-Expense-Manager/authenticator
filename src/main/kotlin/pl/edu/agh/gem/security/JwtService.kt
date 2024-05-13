package pl.edu.agh.gem.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import pl.edu.agh.gem.internal.model.auth.VerifiedUser
import java.time.Duration
import java.util.Date

@Component
class JwtService(
    private val tokenProperties: TokenProperties,
) {
    private fun getSecretKey() = Keys.hmacShaKeyFor(Decoders.BASE64.decode(tokenProperties.secretKey))

    fun createToken(verifiedUser: VerifiedUser): String {
        val now = Date()
        val expiryDate = Date(now.time + tokenProperties.expiration.toMillis())

        return Jwts.builder()
            .subject(verifiedUser.id)
            .claim("email", verifiedUser.email)
            .issuedAt(Date())
            .expiration(expiryDate)
            .signWith(getSecretKey())
            .compact()
    }
}

@ConfigurationProperties(prefix = "token")
data class TokenProperties(
    val expiration: Duration,
    val secretKey: String,
)
