package pl.edu.agh.gem.internal.service

import org.springframework.stereotype.Service
import pl.edu.agh.gem.internal.client.EmailSenderClient
import pl.edu.agh.gem.internal.model.auth.NotVerifiedUser
import pl.edu.agh.gem.internal.model.emailsender.VerificationEmailDetails
import pl.edu.agh.gem.internal.persistence.NotVerifiedUserRepository
import pl.edu.agh.gem.internal.persistence.VerifiedUserRepository
import java.security.SecureRandom
import java.util.stream.Collectors

@Service
class AuthService(
    private val notVerifiedUserRepository: NotVerifiedUserRepository,
    private val verifiedUserRepository: VerifiedUserRepository,
    private val senderClient: EmailSenderClient,
) {

    fun create(notVerifiedUser: NotVerifiedUser) {
        if (notVerifiedUserRepository.existByEmail(notVerifiedUser.email) || verifiedUserRepository.existByEmail(notVerifiedUser.email)) {
            throw DuplicateEmailException()
        }
        notVerifiedUserRepository.create(notVerifiedUser)
        senderClient.sendVerificationEmail(VerificationEmailDetails(notVerifiedUser.email, notVerifiedUser.code))
    }

    fun generateCode(): String {
        return SecureRandom().ints(0, RANDOM_NUMBER_BOUND)
            .limit(CODE_LENGTH)
            .boxed()
            .map(Int::toString)
            .collect(Collectors.joining(""))
    }

    companion object {
        private const val CODE_LENGTH = 6L
        private const val RANDOM_NUMBER_BOUND = 10
    }
}

class DuplicateEmailException : RuntimeException("Email is already taken")
