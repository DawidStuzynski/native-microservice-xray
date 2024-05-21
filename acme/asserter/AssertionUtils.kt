package com.tui.gcotest.asserter

import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.assertj.core.api.Assertions.assertThat
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZonedDateTime

object AssertionUtils {
    private val mapper =
        ObjectMapper()
            .registerModule(JavaTimeModule())
            .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(KotlinModule.Builder().build())

    fun <T> assertRecursive(given: T, expected: T, vararg ignoringField: String) {
        assertThat(given)
            .usingRecursiveComparison()
            .ignoringFieldsOfTypes(
                OffsetDateTime::class.java,
                LocalDateTime::class.java,
                ZonedDateTime::class.java,
                Instant::class.java
            )
            .ignoringFields(*ignoringField)
            .isEqualTo(expected)
    }

    fun <T> assertRecursive(given: String, expected: T, vararg ignoringField: String) {
        val givenNode = mapper.readTree(given)
        val givenBody = mapper.treeToValue(givenNode, expected!!::class.java)

        assertRecursive(givenBody, expected, *ignoringField)
    }

    fun <T> assertRecursive(given: String, expected: String, type: Class<T>, vararg ignoringField: String) {
        val givenNode = mapper.readTree(given)
        val expectedNode = mapper.readTree(expected)
        val givenBody = mapper.treeToValue(givenNode, type)
        val expectedBody = mapper.treeToValue(expectedNode, type)

        assertRecursive(givenBody, expectedBody, *ignoringField)
    }
}
