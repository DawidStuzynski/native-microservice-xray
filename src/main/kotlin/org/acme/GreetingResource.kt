package org.acme

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.tui.gcosnsclient.sns.SnsClient
import io.github.oshai.kotlinlogging.KotlinLogging
import io.smallrye.mutiny.Uni
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType.APPLICATION_JSON
import org.acme.model.EventModel
import org.acme.sns.EventEmitException
import org.eclipse.microprofile.config.inject.ConfigProperty
import software.amazon.awssdk.services.sns.model.MessageAttributeValue

@Path("/hello")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
class GreetingResource(
    @ConfigProperty(name = "acme.topic-arn") private val topicArn: String,
    val snsClient: SnsClient
) {

    val logger = KotlinLogging.logger {}

    companion object {
        val objectMapper: ObjectMapper =
            ObjectMapper()
                .registerModule(JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }

    @GET
    fun hello(): Uni<Unit> {
        println(topicArn)
        val messageAttribute = MessageAttributeValue.builder().dataType("String").stringValue("attribute").build()
        val attributesMap = mapOf("eventType" to messageAttribute)
        val event = EventModel(title = "title", description = "description")
        val serializedEvent = objectMapper.writeValueAsString(event)

        return publishToSnsTopic(serializedEvent, attributesMap)
    }

    private fun publishToSnsTopic(
        serializedEvent: String,
        attributes: Map<String, MessageAttributeValue>,
    ): Uni<Unit> {
        return snsClient.publish { it.topicArn(topicArn).message(serializedEvent).messageAttributes(attributes) }
            .onFailure().transform { EventEmitException(it.message ?: "Sending event to SNS topic failed") }
            .invoke { res ->
                logger.info { "Published event with id ${res.messageId()} to SNS topic" }
            }
            .replaceWith(Unit)
    }
}
