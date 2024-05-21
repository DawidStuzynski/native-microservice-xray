package org.acme

import io.smallrye.mutiny.Uni
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType.APPLICATION_JSON
import software.amazon.awssdk.services.sns.SnsClient

@Path("/hello")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
class GreetingResource(
    val snsClient: SnsClient
) {

    @GET
    fun hello(): Uni<Unit> {
        return Uni.createFrom().item(Unit)
    }
}
