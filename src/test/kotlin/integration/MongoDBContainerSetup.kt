package integration

import org.junit.jupiter.api.BeforeAll
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
interface MongoDBContainerSetup {
    companion object {
        @JvmStatic
        val mongoDBContainer: MongoDBContainer =
            MongoDBContainer("mongo:7.0.5")
                .withReuse(true)

        @JvmStatic
        @DynamicPropertySource
        fun setProperties(dynamicPropertyRegistry: DynamicPropertyRegistry) {
            dynamicPropertyRegistry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl)
        }

        @JvmStatic
        @BeforeAll
        fun setup() {
            mongoDBContainer.start()
        }
    }
}
