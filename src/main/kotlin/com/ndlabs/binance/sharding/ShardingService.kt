package com.ndlabs.binance.sharding

import com.ndlabs.binance.configuration.AppConfiguration
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.openapi.models.V1Pod
import io.kubernetes.client.util.Watch
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

interface ShardingService {
    fun getActiveInstanceNames(): List<String>

    fun getCurrentInstanceName(): String

    fun watch(onChange: () -> Unit)
}

@Service
@Profile("local")
class LocalShardingService: ShardingService {
    override fun getActiveInstanceNames(): List<String> {
        return listOf("local")
    }

    override fun getCurrentInstanceName(): String {
        return "local"
    }

    override fun watch(onChange: () -> Unit) {
        onChange()
    }
}

@Service
@Profile("!local")
class KubernetesShardingService(
    private val kubernetesConfig: AppConfiguration.KubernetesConfig,
    private val api: CoreV1Api = CoreV1Api()
): ShardingService {
    private val logger = KotlinLogging.logger { }

    override fun watch(onChange: () -> Unit) {
        val call = api.listNamespacedPodCall(
            kubernetesConfig.nameSpace,
            null,
            null,
            null,
            null,
            "app=${kubernetesConfig.serviceName}",
            null,
            null,
            null,
            null,
            true,
            null
        )
        val watch = Watch.createWatch<V1Pod>(
            api.apiClient,
            call,
            V1Pod::class.java
        )

        watch.forEach { event ->
            if (event.type == "ADDED" || event.type == "DELETED") {
                onChange()
            }
        }
    }

    override fun getActiveInstanceNames(): List<String> {
        try {
            val podList = api.listNamespacedPod(
                kubernetesConfig.nameSpace,
                null,
                null,
                null,
                null,
                "app=${kubernetesConfig.serviceName}",
                null,
                null,
                null,
                null,
                null
            )
            return podList.items
                .filter { it.status?.phase == "Running" }
                .mapNotNull { it.metadata?.name }
        } catch (e: Exception) {
            logger.error { "Error fetching active pods, exception: $e" }
            return emptyList()
        }
    }

    override fun getCurrentInstanceName(): String {
        return System.getenv("POD_NAME") ?: "unknown-pod"
    }
}
