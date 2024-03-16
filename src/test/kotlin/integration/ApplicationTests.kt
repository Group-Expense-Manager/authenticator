package integration

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.junit.jupiter.Testcontainers
import pl.edu.agh.gem.Application

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(classes = [Application::class])
@ActiveProfiles("integration")
class ApplicationTests(
    @Autowired val mockMvc: MockMvc,
    @Autowired val mongoTemplate: MongoTemplate,
) : MongoDBContainerSetup {
    @AfterEach
    fun cleanUp() {
        mongoTemplate.collectionNames.forEach { mongoTemplate.dropCollection(it) }
    }

    @Test
    fun shouldReturnAllProduct() {
        mockMvc.perform(
            MockMvcRequestBuilders
                .get("/api/names")
                .contentType(MediaType.APPLICATION_JSON),
        ).andExpectAll(status().isOk)
    }
}
