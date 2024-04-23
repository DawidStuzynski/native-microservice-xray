package com.tui.gcotest.sns

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.eclipse.microprofile.config.ConfigProvider
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.QueueAttributeName.QUEUE_ARN
import java.net.URI
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.locks.ReentrantLock

@Suppress("TooManyFunctions")
class SnsTestClientImpl(
    private val topicName: String,
) : SnsTestClient {
    private val snsClient: SnsClient = initializeSNSClient()
    private val sqsClient: SqsClient = initializeSQSClient()

    private var topicArn: String? = null
    private val lock: ReentrantLock = ReentrantLock()

    companion object {
        const val TIMEOUT = 10L
        const val SNS_ENDPOINT = "quarkus.sns.endpoint-override"
        const val SQS_ENDPOINT = "quarkus.sqs.endpoint-override"
        const val AWS_REGION = "quarkus.sns.aws.region"
        const val ACCESS_KEY_ID = "quarkus.sns.aws.credentials.static-provider.access-key-id"
        const val SECRET_ACCESS_KEY = "quarkus.sns.aws.credentials.static-provider.secret-access-key"
    }

    override fun create() {
        withLock { this.topicArn = createAndSubscribeTopic(topicName).topicArn }
    }

    override fun delete() {
        withLock {
            topicArn?.let {
                snsClient.deleteTopic { it.topicArn(topicArn) }
                topicArn = null
            }
        }
    }

    override fun getMessages(maxMessageNumber: Int, waitTimeSeconds: Int): List<JsonNode> {
        val queueUrl = getQueueUrl(topicName)
        val massagesList = mutableListOf<JsonNode>()

        val messages =
            sqsClient.receiveMessage { it.queueUrl(queueUrl).maxNumberOfMessages(maxMessageNumber).waitTimeSeconds(waitTimeSeconds) }
                .messages()

        messages.forEach {
            massagesList.addFirst(
                jacksonObjectMapper()
                    .readTree(it.body())["Message"]
            )
        }

        return massagesList
    }

    private fun initializeSNSClient(): SnsClient {
        return SnsClient.builder()
            .endpointOverride(URI.create(getConfigValue(SNS_ENDPOINT)))
            .region(Region.of(getConfigValue(AWS_REGION)))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(getConfigValue(ACCESS_KEY_ID), getConfigValue(SECRET_ACCESS_KEY))
                )
            )
            .build()
    }

    private fun initializeSQSClient(): SqsClient {
        return SqsClient.builder()
            .endpointOverride(URI.create(getConfigValue(SQS_ENDPOINT)))
            .region(Region.of(getConfigValue(AWS_REGION)))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(getConfigValue(ACCESS_KEY_ID), getConfigValue(SECRET_ACCESS_KEY))
                )
            )
            .build()
    }

    private fun createAndSubscribeTopic(topicName: String): CreateTopicResponse {
        val topicArn = createTopic(topicName)
        val queueArn = createAndSubscribeQueue(topicArn, topicName)

        return CreateTopicResponse(
            topicArn = topicArn,
            queueArn = queueArn
        )
    }

    private fun createTopic(topicName: String): String {
        return snsClient.createTopic { it.name(topicName) }.topicArn()
    }

    private fun createAndSubscribeQueue(topicArn: String, queueName: String): String {
        createQueue(queueName)
        val queueArn = getQueueArn(queueName)

        snsClient.subscribe {
            it.topicArn(topicArn)
                .endpoint(queueArn)
                .protocol("sqs")
        }

        return queueArn!!
    }

    private fun createQueue(queueName: String) {
        sqsClient.createQueue { it.queueName(queueName) }
    }

    private fun getQueueArn(queueName: String): String? {
        return sqsClient.getQueueAttributes { it.queueUrl(queueName).attributeNames(QUEUE_ARN) }.attributes()[QUEUE_ARN]
    }

    private fun getQueueUrl(queueName: String): String {
        return sqsClient.listQueues { it.queueNamePrefix(queueName) }.queueUrls().first()
    }

    private fun withLock(action: () -> Unit) {
        try {
            lock.tryLock(TIMEOUT, SECONDS)
            action()
        } finally {
            lock.unlock()
        }
    }

    private data class CreateTopicResponse(
        val topicArn: String,
        val queueArn: String,
    )

    private fun getConfigValue(propertyName: String): String {
        return ConfigProvider.getConfig().getConfigValue(propertyName).rawValue
    }
}
