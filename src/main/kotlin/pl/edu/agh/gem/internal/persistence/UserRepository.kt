package pl.edu.agh.gem.internal.persistence

import pl.edu.agh.gem.internal.domain.User

interface UserRepository {
    fun create(user: User): User
}
