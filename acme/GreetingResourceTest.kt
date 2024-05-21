package org.acme

import com.lectra.koson.obj
import com.tui.gcotest.asserter.AssertionUtils.assertRecursive
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.acme.model.EventModel
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.Test

@QuarkusTest
class GreetingResourceTest : BaseTest() {

    @Test
    fun `todo `() {
        When {
            get("/hello")
        } Then {
            statusCode(200)
        } Extract {

            val snsMessages = snsTestClient.getMessages()
            val actualFromSns = snsMessages.first().asText()
            assertThat(snsMessages.size).isEqualTo(1)

            val expectedFromSns = obj {
                "title" to "title"
                "description" to "description"
            }

            assertRecursive(
                actualFromSns,
                expectedFromSns.toString(),
                EventModel::class.java,
            )
            println(actualFromSns)
        }
    }
}
