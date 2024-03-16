package pl.edu.agh.gem.external.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import pl.edu.agh.gem.internal.domain.Product
import java.util.UUID.randomUUID

@Document("products")
data class ProductEntity(
    @Id
    val id: String = randomUUID().toString(),
    val name: String,
) {
    fun toDomain() =
        Product(
            name = name,
        )
}
