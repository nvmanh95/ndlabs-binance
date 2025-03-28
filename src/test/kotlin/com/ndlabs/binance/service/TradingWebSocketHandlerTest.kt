package com.ndlabs.binance.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ndlabs.binance.exception.BinanceWebsocketConnectionException
import com.ndlabs.binance.model.BinanceTradeModel
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.WebSocketClient
import java.net.SocketTimeoutException

class TradingWebSocketHandlerTest : ShouldSpec({

    val url = "tradeUrl"
    val pair = "bnbusdt"
    val podName = "service1"
    val client = mockk<WebSocketClient>()
    val objectMapper = jacksonObjectMapper()
    val processor = mockk<TradingProcessor>()
    val testInstance = TradingWebSocketHandler(url, pair, podName, objectMapper, client, processor)

    should("able to establish connection to socket") {
        //given
        val websocketSession = mockk<WebSocketSession>()
        every { client.execute(any(), eq(url)).get() } returns websocketSession

        //when
        val result = testInstance.execute()

        //then
        result shouldBe websocketSession
    }


    should("throw an exception if unable to establish connection to socket") {
        //given
        every { client.execute(any(), eq(url)).get() } throws SocketTimeoutException()

        //when
        val result = shouldThrow<BinanceWebsocketConnectionException> { testInstance.execute() }

        //then
        result.message shouldBe "Unable to establish connection to websocket $url"
    }

    should("handle trade data") {
        //given
        val textMessage = mockk<TextMessage>()
        val dataSlot = slot<BinanceTradeModel>()
        val tradeInfo = """
            {
            	"t": "4749038460",
            	"s": "BTCUSDT",
            	"p": "87233.69000000",
            	"q": "0.00090000",
            	"T": 1743092405979
            }
        """.trimIndent()

        every { textMessage.payload } returns tradeInfo
        justRun { processor.process(any()) }

        //then
        shouldNotThrowAny {
            testInstance.handleTextMessage(mockk<WebSocketSession>(), textMessage)
        }

        verify(exactly = 1) { processor.process(capture(dataSlot)) }

        with(dataSlot.captured) {
            id shouldBe "4749038460"
        }
    }
})