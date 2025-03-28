package com.ndlabs.binance.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfiguration {


    class BinanceConfig {
        var tradeWebsocketUrl: String = ""
        var tradingPairs: List<String> = emptyList()
    }

    class KubernetesConfig {
        var nameSpace: String = ""
        var serviceName: String = ""
    }

    @Bean
    @ConfigurationProperties("app.kubernetes")
    fun kubernetesConfig() : KubernetesConfig{
        return KubernetesConfig()
    }

    @Bean
    @ConfigurationProperties("app.binance")
    fun binanceConfig(): BinanceConfig {
        return BinanceConfig()
    }
}