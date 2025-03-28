package com.ndlabs.binance.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.ndlabs.binance.exception.BinanceWebsocketConnectionException
import com.ndlabs.binance.model.BinanceTradeModel
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.slf4j.MDC
import org.springframework.stereotype.Service
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.WebSocketClient
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.handler.TextWebSocketHandler

@Service
class BinanceWebsocketSubscriptionService(private val client: WebSocketClient = StandardWebSocketClient(),
                                          private val objectMapper: ObjectMapper,
                                          private val processor: TradingProcessor) {

    fun subscribe(url: String, pair: String, podName: String): WebSocketSession {
        return TradingWebSocketHandler(url, pair, podName, objectMapper, client, processor).execute()
    }
}

class TradingWebSocketHandler(
    private val url: String,
    private val pair: String,
    private val podName: String,
    private val objectMapper: ObjectMapper,
    private val client: WebSocketClient,
    private val processor: TradingProcessor
) : TextWebSocketHandler() {
    private val logger: KLogger = KotlinLogging.logger {}

    fun execute(): WebSocketSession {
        try {
            return client.execute(this, url).get()
        } catch (exception: Exception) {
            logger.error { "Error establishing websocket for: $url, exception: ${exception.message}" }
            throw BinanceWebsocketConnectionException("Unable to establish connection to websocket $url")
        }
    }

    public override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        MDC.put("instanceName", podName)
        MDC.put("pair", pair)
        val binanceTrade = BinanceTradeModel.from(objectMapper.readTree(message.payload))

        logger.info { "handling trading data: ${binanceTrade.id}" }
        processor.process(binanceTrade)
    }
}