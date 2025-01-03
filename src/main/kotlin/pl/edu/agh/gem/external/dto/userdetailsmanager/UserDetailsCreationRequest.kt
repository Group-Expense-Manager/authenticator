package pl.edu.agh.gem.external.dto.userdetailsmanager

import pl.edu.agh.gem.internal.model.userdetailsmanager.UserDetails

data class UserDetailsCreationRequest(
    val userId: String,
    val username: String,
)

fun UserDetails.toUserDetailsCreationRequest() =
    UserDetailsCreationRequest(
        userId = userId,
        username = username,
    )
