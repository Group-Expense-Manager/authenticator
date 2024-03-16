package pl.edu.agh.gem.external.persistence

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Repository
import pl.edu.agh.gem.internal.domain.Product
import pl.edu.agh.gem.internal.persistence.ProductRepository

@Repository
class MongoProductRepository(private val mongo: MongoTemplate) : ProductRepository {
    override fun findAll(): List<Product> {
        return mongo.findAll(ProductEntity::class.java).map { it.toDomain() }
    }
}
