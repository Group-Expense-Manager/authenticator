package pl.edu.agh.gem.external.dto.auth

data class EmailAddressResponse(
    val email: String,
)

fun String.toEmailAddressResponse(): EmailAddressResponse = EmailAddressResponse(this)
