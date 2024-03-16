package pl.edu.agh.gem.internal.service

import org.springframework.stereotype.Service
import pl.edu.agh.gem.internal.domain.User
import pl.edu.agh.gem.internal.persistence.UserRepository

@Service
class RegistrationService(
    private val userRepository: UserRepository,
) {
    fun create(user: User) {
        userRepository.create(user)
    }
}
