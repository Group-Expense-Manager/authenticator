package pl.edu.agh.gem.internal.persistence

import pl.edu.agh.gem.internal.domain.Product

interface ProductRepository {
    fun findAll(): List<Product>
}
