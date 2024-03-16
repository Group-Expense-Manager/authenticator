package pl.edu.agh.gem.external.dto

import pl.edu.agh.gem.internal.domain.Product

data class ProductResponse(
    val name: String,
) {
    companion object {
        fun from(product: Product): ProductResponse {
            return ProductResponse(product.name)
        }
    }
}
