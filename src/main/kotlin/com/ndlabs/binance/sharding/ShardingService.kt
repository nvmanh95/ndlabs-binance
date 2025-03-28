package com.ndlabs.binance.sharding

import com.google.gson.reflect.TypeToken
import com.ndlabs.binance.configuration.AppConfiguration
import com.ndlabs.binance.exception.KubernetesShardingException
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
class LocalShardingService : ShardingService {
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
    private val kubernetesApi: CoreV1Api
) : ShardingService {
    private val logger = KotlinLogging.logger { }

    override fun watch(onChange: () -> Unit) {
        onChange()

        val watch = establishKubernetesEventWatch()
        watch.filter { it.`object`.metadata?.name?.contains(kubernetesConfig.serviceName) ?: false }
            .filter { it.type == "ADDED" || it.type == "DELETED" }
            .forEach { _ -> onChange() }
    }

    private fun establishKubernetesEventWatch(): Watch<V1Pod> {
        try {
            val call = kubernetesApi.listNamespacedPodCall(
                kubernetesConfig.nameSpace,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                true,
                null
            )
            return Watch.createWatch(kubernetesApi.apiClient, call, object : TypeToken<Watch.Response<V1Pod>>() {}.type)

        } catch (exception: Exception) {
            logger.error { "Unable to establish watch for namespace: ${kubernetesConfig.nameSpace} from pod: ${getCurrentInstanceName()}" }
            throw KubernetesShardingException(
                "Unable to establish watch for namespace: ${kubernetesConfig.nameSpace} from pod: ${getCurrentInstanceName()}",
                exception
            )
        }
    }

    override fun getActiveInstanceNames(): List<String> {
        try {
            logger.debug { "fetching pod list from namespace: ${kubernetesConfig.nameSpace} and app: ${kubernetesConfig.serviceName}" }
            val podList = kubernetesApi.listNamespacedPod(
                kubernetesConfig.nameSpace,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
            )
            return podList.items
                .filter { it.status?.phase == "Running" && it.metadata?.name?.contains(kubernetesConfig.serviceName) ?: false }
                .mapNotNull { it.metadata?.name }
        } catch (e: Exception) {
            logger.error { "Error fetching active pods, exception: ${e.message}" }
            return emptyList()
        }
    }

    override fun getCurrentInstanceName(): String {
        return System.getenv("HOSTNAME") ?: "unknown-pod"
    }
}
