package com.ndlabs.binance.configuration

import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.Config
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("!local")
@Configuration
class KubernetesConfiguration {

    @Bean
    fun apiClient(): ApiClient {
        val client: ApiClient = Config.defaultClient()
        io.kubernetes.client.openapi.Configuration.setDefaultApiClient(client)
        return client
    }

    @Bean
    fun coreV1Api(apiClient: ApiClient): CoreV1Api {
        return CoreV1Api(apiClient)
    }
}