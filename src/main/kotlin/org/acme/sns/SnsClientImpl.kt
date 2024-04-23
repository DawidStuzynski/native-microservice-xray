package com.tui.gcosnsclient.sns

import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import software.amazon.awssdk.services.sns.SnsAsyncClient
import software.amazon.awssdk.services.sns.model.PublishRequest
import software.amazon.awssdk.services.sns.model.PublishResponse

@ApplicationScoped
class SnsClientImpl(
    private val snsClient: SnsAsyncClient,
) : SnsClient {
    override fun publish(requestBuilder: (PublishRequest.Builder) -> Unit): Uni<PublishResponse> {
        return Uni.createFrom().completionStage { snsClient.publish(requestBuilder) }
    }
}
