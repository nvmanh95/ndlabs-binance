package com.ndlabs.binance.configuration

import org.springframework.kafka.support.serializer.JsonSerializer
import org.apache.kafka.common.serialization.StringSerializer
import com.ndlabs.binance.model.TradeModel
import org.apache.kafka.clients.producer.ProducerConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory


@EnableKafka
@Configuration
class KafkaConfiguration {
    class KafkaConfig {
        var binanceTradeTopic: String = ""
        var boostrapServer: String = ""
    }

    @Bean
    @ConfigurationProperties("app.kafka")
    fun kafkaConfig(): KafkaConfig {
        return KafkaConfig()
    }

    @Bean
    fun producerFactory(kafkaConfig: KafkaConfig): ProducerFactory<String, TradeModel> {
        val conf: MutableMap<String, Any> = HashMap()
        conf[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaConfig.boostrapServer
        conf[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        conf[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java

        return DefaultKafkaProducerFactory(conf)
    }


    @Bean
    fun kafkaTemplate(producerFactory: ProducerFactory<String, TradeModel>): KafkaTemplate<String, TradeModel> {
        return KafkaTemplate(producerFactory)
    }
}