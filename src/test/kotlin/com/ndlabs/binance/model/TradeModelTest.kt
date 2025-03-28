package com.ndlabs.binance.model

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.LongNode
import com.fasterxml.jackson.databind.node.TextNode
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.time.Instant

class TradeModelTest : ShouldSpec({

    should("able to build TradeModel from json") {
        //given
        val jsonNode = mockk<JsonNode>()

        every { jsonNode.get("t") } returns TextNode("tradeId")
        every { jsonNode.get("s") } returns TextNode("BTCUSDT")
        every { jsonNode.get("p") } returns TextNode("100")
        every { jsonNode.get("q") } returns TextNode("100")
        every { jsonNode.get("T") } returns LongNode(Instant.now().epochSecond)

        //when
        val result = BinanceTradeModel.from(jsonNode)

        //then
        with(result) {
            id shouldBe "tradeId"
        }
    }
})