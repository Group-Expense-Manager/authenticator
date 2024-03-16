package pl.edu.agh.gem.external.controller

import org.springframework.http.HttpStatus.OK
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import pl.edu.agh.gem.external.dto.ProductResponse
import pl.edu.agh.gem.internal.service.ProductService

@RestController
@RequestMapping("/api")
class ProductController(
    val productService: ProductService,
) {
    @GetMapping("/names")
    @ResponseStatus(OK)
    fun getAll(): List<ProductResponse> {
        return productService.getAll().map { ProductResponse.from(it) }
    }
}
