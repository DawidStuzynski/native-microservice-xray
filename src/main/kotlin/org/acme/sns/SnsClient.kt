package com.tui.gcosnsclient.sns

import io.smallrye.mutiny.Uni
import software.amazon.awssdk.services.sns.model.PublishRequest
import software.amazon.awssdk.services.sns.model.PublishResponse
import software.amazon.awssdk.services.sns.model.SnsException

interface SnsClient {
    @Throws(SnsException::class)
    fun publish(requestBuilder: (PublishRequest.Builder) -> Unit): Uni<PublishResponse>
}
