package pl.edu.agh.gem.util

import pl.edu.agh.gem.external.dto.auth.LoginRequest
import pl.edu.agh.gem.external.dto.auth.PasswordChangeRequest
import pl.edu.agh.gem.external.dto.auth.RegistrationRequest
import pl.edu.agh.gem.external.dto.auth.VerificationEmailRequest
import pl.edu.agh.gem.external.dto.auth.VerificationRequest
import pl.edu.agh.gem.external.dto.userdetailsmanager.UserDetailsCreationRequest
import pl.edu.agh.gem.internal.model.auth.NotVerifiedUser
import pl.edu.agh.gem.internal.model.auth.Verification
import pl.edu.agh.gem.internal.model.auth.VerifiedUser
import pl.edu.agh.gem.internal.model.emailsender.VerificationEmailDetails
import pl.edu.agh.gem.internal.model.userdetailsmanager.UserDetails
import pl.edu.agh.gem.internal.persistence.NotVerifiedUserRepository
import pl.edu.agh.gem.internal.persistence.VerifiedUserRepository
import pl.edu.agh.gem.util.DummyData.DUMMY_EMAIL
import pl.edu.agh.gem.util.DummyData.DUMMY_PASSWORD
import pl.edu.agh.gem.util.DummyData.OTHER_DUMMY_PASSWORD
import java.time.Instant
import java.time.Instant.now

fun createEmailDetails(
    email: String = "my@mail.com",
    code: String = "123456",
) = VerificationEmailDetails(
    email = email,
    code = code,
)

fun createNotVerifiedUser(
    id: String = "id",
    email: String = "my@mail.com",
    password: String = "encodedPassword",
    createdAt: Instant = now(),
    code: String = "123456",
    updatedCodeAt: Instant = now(),
) = NotVerifiedUser(
    id = id,
    email = email,
    password = password,
    createdAt = createdAt,
    code = code,
    codeUpdatedAt = updatedCodeAt,
)

fun saveNotVerifiedUser(
    id: String = "id",
    email: String = "my@mail.com",
    password: String = "encodedPassword",
    createdAt: Instant = now(),
    code: String = "123456",
    updatedCodeAt: Instant = now(),
    notVerifiedUserRepository: NotVerifiedUserRepository,
) = notVerifiedUserRepository.create(
    createNotVerifiedUser(
        id = id,
        email = email,
        password = password,
        createdAt = createdAt,
        code = code,
        updatedCodeAt = updatedCodeAt,
    ),
)

fun createRegistrationRequest(
    email: String = "my@mail.com",
    password: String = "Password1!",
) = RegistrationRequest(
    email = email,
    password = password,
)

fun createVerifiedUser(
    id: String = "id",
    email: String = "my@mail.com",
    password: String = "encodedPassword",

) = VerifiedUser(
    id = id,
    email = email,
    password = password,
)

fun createLoginRequest(
    email: String = "my@mail.com",
    password: String = "encodedPassword",
) = LoginRequest(
    email = email,
    password = password,
)

fun saveVerifiedUser(
    id: String = "id",
    email: String = "my@mail.com",
    password: String = "encodedPassword",
    verifiedUserRepository: VerifiedUserRepository,
) = verifiedUserRepository.create(
    createVerifiedUser(
        id = id,
        email = email,
        password = password,
    ),
)

fun createVerificationRequest(
    email: String = "my@mail.com",
    code: String = "123456",
) = VerificationRequest(
    email = email,
    code = code,
)

fun createVerification(
    email: String = "my@mail.com",
    code: String = "123456",
) = Verification(
    email = email,
    code = code,
)

fun createVerificationEmailRequest(
    email: String = "my@mail.com",
) = VerificationEmailRequest(
    email = email,
)

fun createUserDetails(
    userId: String = "userId",
    username: String = "username",
) = UserDetails(
    userId = userId,
    username = username,
)

fun createUserDetailsCreationRequest(
    userId: String = "userId",
    email: String = DUMMY_EMAIL,
) = UserDetailsCreationRequest(
    userId = userId,
    username = email.substringBefore("@"),
)

fun createPasswordChangeRequest(
    oldPassword: String = DUMMY_PASSWORD,
    newPassword: String = OTHER_DUMMY_PASSWORD,
) = PasswordChangeRequest(
    oldPassword = oldPassword,
    newPassword = newPassword,
)

object DummyData {
    const val DUMMY_EMAIL = "email@email.com"
    const val DUMMY_CODE = "123456"
    const val OTHER_DUMMY_CODE = "654321"
    const val DUMMY_PASSWORD = "Password123!"
    const val OTHER_DUMMY_PASSWORD = "Password321!"
}
