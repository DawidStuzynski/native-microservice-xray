package org.acme

import io.quarkus.test.junit.QuarkusTest
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.junit.jupiter.api.Test

@QuarkusTest
class GreetingResourceTest {

    @Test
    fun `todo `() {
        When {
            get("/hello")
        } Then {
            statusCode(200)
        }
    }
}
