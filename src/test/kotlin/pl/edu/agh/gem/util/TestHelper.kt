package pl.edu.agh.gem.util

import pl.edu.agh.gem.external.dto.auth.LoginRequest
import pl.edu.agh.gem.external.dto.auth.PasswordChangeRequest
import pl.edu.agh.gem.external.dto.auth.PasswordRecoveryRequest
import pl.edu.agh.gem.external.dto.auth.RegistrationRequest
import pl.edu.agh.gem.external.dto.auth.VerificationEmailRequest
import pl.edu.agh.gem.external.dto.auth.VerificationRequest
import pl.edu.agh.gem.external.dto.userdetailsmanager.UserDetailsCreationRequest
import pl.edu.agh.gem.external.persistence.PasswordRecoveryCodeEntity
import pl.edu.agh.gem.helper.user.DummyUser.EMAIL
import pl.edu.agh.gem.helper.user.DummyUser.USER_ID
import pl.edu.agh.gem.internal.model.auth.NotVerifiedUser
import pl.edu.agh.gem.internal.model.auth.PasswordRecoveryCode
import pl.edu.agh.gem.internal.model.auth.Verification
import pl.edu.agh.gem.internal.model.auth.VerifiedUser
import pl.edu.agh.gem.internal.model.emailsender.PasswordEmailDetails
import pl.edu.agh.gem.internal.model.emailsender.PasswordRecoveryEmailDetails
import pl.edu.agh.gem.internal.model.emailsender.VerificationEmailDetails
import pl.edu.agh.gem.internal.model.userdetailsmanager.UserDetails
import pl.edu.agh.gem.internal.persistence.NotVerifiedUserRepository
import pl.edu.agh.gem.internal.persistence.VerifiedUserRepository
import pl.edu.agh.gem.util.DummyData.DUMMY_CODE
import pl.edu.agh.gem.util.DummyData.DUMMY_PASSWORD
import pl.edu.agh.gem.util.DummyData.DUMMY_USERNAME
import pl.edu.agh.gem.util.DummyData.OTHER_DUMMY_PASSWORD
import java.time.Instant
import java.time.Instant.now

fun createNotVerifiedUser(
    id: String = USER_ID,
    username: String = DUMMY_USERNAME,
    email: String = EMAIL,
    password: String = "encodedPassword",
    createdAt: Instant = now(),
    code: String = DUMMY_CODE,
    updatedCodeAt: Instant = now(),
) = NotVerifiedUser(
    id = id,
    username = username,
    email = email,
    password = password,
    createdAt = createdAt,
    code = code,
    codeUpdatedAt = updatedCodeAt,
)

fun saveNotVerifiedUser(
    id: String = USER_ID,
    email: String = EMAIL,
    password: String = "encodedPassword",
    createdAt: Instant = now(),
    code: String = DUMMY_CODE,
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
    username: String = DUMMY_USERNAME,
    email: String = EMAIL,
    password: String = DUMMY_PASSWORD,
) = RegistrationRequest(
    username = username,
    email = email,
    password = password,
)

fun createVerifiedUser(
    id: String = USER_ID,
    email: String = EMAIL,
    password: String = "encodedPassword",

) = VerifiedUser(
    id = id,
    email = email,
    password = password,
)

fun createLoginRequest(
    email: String = EMAIL,
    password: String = "encodedPassword",
) = LoginRequest(
    email = email,
    password = password,
)

fun saveVerifiedUser(
    id: String = USER_ID,
    email: String = EMAIL,
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
    email: String = EMAIL,
    code: String = DUMMY_CODE,
) = VerificationRequest(
    email = email,
    code = code,
)

fun createVerification(
    email: String = EMAIL,
    code: String = DUMMY_CODE,
) = Verification(
    email = email,
    code = code,
)

fun createVerificationEmailRequest(
    email: String = EMAIL,
) = VerificationEmailRequest(
    email = email,
)

fun createVerificationEmailDetails(
    username: String = DUMMY_USERNAME,
    email: String = EMAIL,
    code: String = DUMMY_CODE,

) = VerificationEmailDetails(
    username = username,
    email = email,
    code = code,
)

fun createUserDetails(
    userId: String = USER_ID,
    username: String = DUMMY_USERNAME,
) = UserDetails(
    userId = userId,
    username = username,
)

fun createUserDetailsCreationRequest(
    userId: String = USER_ID,
    username: String = DUMMY_USERNAME,
) = UserDetailsCreationRequest(
    userId = userId,
    username = username,
)

fun createPasswordChangeRequest(
    oldPassword: String = DUMMY_PASSWORD,
    newPassword: String = OTHER_DUMMY_PASSWORD,
) = PasswordChangeRequest(
    oldPassword = oldPassword,
    newPassword = newPassword,
)

fun createPasswordRecoveryEmailDetails(
    username: String = DUMMY_USERNAME,
    email: String = EMAIL,
    code: String = DUMMY_CODE,
) = PasswordRecoveryEmailDetails(
    username = username,
    email = email,
    code = code,
)

fun createPasswordRecoveryCodeEntity(
    userId: String = USER_ID,
    code: String = DUMMY_CODE,
    createdAt: Instant = now(),
) = PasswordRecoveryCodeEntity(
    userId = userId,
    code = code,
    createdAt = createdAt,
)

fun createPasswordRecoveryCode(
    userId: String = USER_ID,
    code: String = DUMMY_CODE,
    createdAt: Instant = now(),
) = PasswordRecoveryCode(
    userId = userId,
    code = code,
    createdAt = createdAt,
)

fun createPasswordRecoveryRequest(
    email: String = EMAIL,
) = PasswordRecoveryRequest(
    email = email,
)

fun createPasswordEmailDetails(
    username: String = DUMMY_USERNAME,
    email: String = EMAIL,
    password: String = DUMMY_PASSWORD,
) = PasswordEmailDetails(
    username = username,
    email = email,
    password = password,
)

object DummyData {
    const val DUMMY_USERNAME = "usernąme123"
    const val DUMMY_CODE = "123456"
    const val OTHER_DUMMY_CODE = "654321"
    const val DUMMY_PASSWORD = "Passłord123!"
    const val OTHER_DUMMY_PASSWORD = "Passłord321!"
}
