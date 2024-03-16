package pl.edu.agh.gem.internal.service

import org.springframework.stereotype.Service
import pl.edu.agh.gem.internal.domain.Product
import pl.edu.agh.gem.internal.persistence.ProductRepository

@Service
class ProductService(
    val productRepository: ProductRepository,
) {
    fun getAll(): List<Product> {
        return productRepository.findAll()
    }
}
