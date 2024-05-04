package pl.edu.agh.gem.security

import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import pl.edu.agh.gem.internal.persistence.NotVerifiedUserRepository
import pl.edu.agh.gem.internal.persistence.VerifiedUserRepository

@Service
class UserDetailsServiceImpl(
    private val notVerifiedUserRepository: NotVerifiedUserRepository,
    private val verifiedUserRepository: VerifiedUserRepository,
) : UserDetailsService {

    override fun loadUserByUsername(email: String?): UserDetails {
        if (email == null) {
            throw BadCredentialsException("Bad credentials")
        }
        return verifiedUserRepository.findByEmail(email)?.let { UserDetailsImpl(it.email, it.password) }
            ?: notVerifiedUserRepository.findByEmail(email)?.let { UserDetailsImpl(it.email, it.password) }
            ?: throw BadCredentialsException("Bad credentials")
    }
}
