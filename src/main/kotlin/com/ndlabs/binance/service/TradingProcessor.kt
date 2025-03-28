package com.ndlabs.binance.service

import com.ndlabs.binance.configuration.KafkaConfiguration.KafkaConfig
import com.ndlabs.binance.model.TradeModel
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

interface TradingProcessor {

    fun process(data: TradeModel)
}

@Service
class KafkaTradingProcessor(private val kafkaConfig: KafkaConfig,
                            private val kafkaTemplate: KafkaTemplate<String, TradeModel>) : TradingProcessor {
    private val logger = KotlinLogging.logger { }

    override fun process(data: TradeModel) {
        kafkaTemplate.send(kafkaConfig.binanceTradeTopic, data)

        logger.info { "Published into kafka" }
    }
}