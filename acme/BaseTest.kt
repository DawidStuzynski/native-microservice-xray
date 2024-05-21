package org.acme

import com.tui.gcotest.sns.SnsTestClient
import com.tui.gcotest.sns.SnsTestClientImpl
import org.eclipse.microprofile.config.ConfigProvider
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll

open class BaseTest {
    companion object {
        lateinit var snsTestClient: SnsTestClient

        @JvmStatic
        @BeforeAll
        fun init() {
            val testTopicName = ConfigProvider.getConfig().getConfigValue("acme.topic-arn").rawValue.substringAfterLast(":")
            snsTestClient = SnsTestClientImpl(testTopicName)
            snsTestClient.create()
        }

        @JvmStatic
        @AfterAll
        fun stop() {
            snsTestClient.delete()
        }
    }
}
