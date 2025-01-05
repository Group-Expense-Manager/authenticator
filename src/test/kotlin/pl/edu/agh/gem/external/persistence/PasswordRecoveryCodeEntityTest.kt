package pl.edu.agh.gem.external.persistence

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import pl.edu.agh.gem.util.createPasswordRecoveryCodeEntity

class PasswordRecoveryCodeEntityTest : ShouldSpec({
    should("map PasswordRecoveryCodeEntity to domain correctly") {
        // given
        val passwordRecoveryCodeEntity = createPasswordRecoveryCodeEntity()

        // when
        val result = passwordRecoveryCodeEntity.toDomain()

        // then
        result.also {
            it.userId shouldBe passwordRecoveryCodeEntity.userId
            it.code shouldBe passwordRecoveryCodeEntity.code
            it.createdAt shouldBe passwordRecoveryCodeEntity.createdAt
        }
    }
})
