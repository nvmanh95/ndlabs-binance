package com.ndlabs.binance

import com.ndlabs.binance.configuration.AppConfiguration
import com.ndlabs.binance.service.BinanceWebsocketSubscriptionService
import com.ndlabs.binance.sharding.ShardingService
import com.ndlabs.binance.sharding.TradingPairDistributionService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.commons.lang3.StringUtils.lowerCase
import org.slf4j.MDC
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession

@Component
class BinanceApplicationRunner(private val tradingPairDistributionService: TradingPairDistributionService,
                               private val shardingService: ShardingService,
                               private val binanceWebsocketSubscriptionService: BinanceWebsocketSubscriptionService,
                               private val binanceConfig: AppConfiguration.BinanceConfig) : ApplicationRunner {

    private val logger = KotlinLogging.logger { }

    private val subscriptions: MutableMap<String, WebSocketSession> = HashMap()

    override fun run(args: ApplicationArguments) {
        logger.info { "Starting Binance Sharding Service" }

        shardingService.watch {
            checkAndUpdateSubscriptions()
        }
    }

    private fun checkAndUpdateSubscriptions() {
        tradingPairDistributionService.distribute()

        val podName = shardingService.getCurrentInstanceName()
        val assignedPairs = tradingPairDistributionService.getAssignedPairs(podName)

        assignedPairs.forEach { pair ->
            if (!subscriptions.contains(pair)) {
                subscriptions[pair] = subscribePair(pair)
            }
        }

        subscriptions.forEach {
            if (!assignedPairs.contains(it.key)) {
                it.value.close()
            }
        }
    }

    private fun subscribePair(pair: String): WebSocketSession {
        MDC.put("instanceName", shardingService.getCurrentInstanceName())
        MDC.put("pair", pair)

        val url = String.format(binanceConfig.tradeWebsocketUrl, lowerCase(pair.replace("_", "")))
        logger.debug { "Subscribing the trading pair: $pair" }
        return binanceWebsocketSubscriptionService.subscribe(url)
    }
}