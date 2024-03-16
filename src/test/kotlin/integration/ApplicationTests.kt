package integration

import com.google.gson.Gson
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
import pl.edu.agh.gem.external.dto.RegistrationRequest

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(classes = [Application::class])
@ActiveProfiles("integration")
class ApplicationTests(
    @Autowired val mockMvc: MockMvc,
    @Autowired val mongoTemplate: MongoTemplate,
) : MongoDBContainerSetup {
    companion object {
        private const val VALID_USERNAME = "test"
        private const val VALID_PASSWORD = "Password123!"
        private const val TOO_SHORT_USERNAME = "tst"

        private val validRequest =
            request(
                VALID_USERNAME,
                VALID_PASSWORD,
            )

        @JvmStatic
        private fun request(
            username: String,
            password: String,
        ): String {
            return Gson().toJson(
                RegistrationRequest(
                    username,
                    password,
                ),
            )
        }
    }

    @AfterEach
    fun cleanUp() {
        mongoTemplate.collectionNames.forEach { mongoTemplate.dropCollection(it) }
    }

    @Test
    fun shouldRegisterUser() {
        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/api/register")
                .content(validRequest)
                .contentType(MediaType.APPLICATION_JSON),
        ).andExpect(status().isCreated)
    }

    @Test
    fun shouldNotRegisterUserWhenUsernameTaken() {
        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/api/register")
                .content(validRequest)
                .contentType(MediaType.APPLICATION_JSON),
        ).andExpect(status().isCreated)
        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/api/register")
                .content(validRequest)
                .contentType(MediaType.APPLICATION_JSON),
        ).andExpect(status().isConflict)
    }

    @Test
    fun shouldNotRegisterUserWhenUsernameTooShort() {
        mockMvc.perform(
            MockMvcRequestBuilders
                .post("/api/register")
                .content(request(TOO_SHORT_USERNAME, VALID_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON),
        ).andExpect(status().isBadRequest)
    }
}
