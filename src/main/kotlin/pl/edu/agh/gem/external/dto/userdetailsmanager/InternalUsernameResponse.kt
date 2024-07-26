package pl.edu.agh.gem.external.dto.userdetailsmanager

data class InternalUsernameResponse(
    val username: String,
)

fun String.toInternalUsernameResponse() = InternalUsernameResponse(this)
