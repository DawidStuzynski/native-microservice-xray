package com.tui.gcotest.sns

import com.fasterxml.jackson.databind.JsonNode

interface SnsTestClient {
    fun create()

    fun delete()

    fun getMessages(maxMessageNumber: Int = 1, waitTimeSeconds: Int = 10): List<JsonNode>
}
