package com.ndlabs.binance.sharding

import com.ndlabs.binance.configuration.AppConfiguration
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.TimeUnit

interface TradingPairDistributionService {
    fun distribute()

    fun getAssignedPairs(podName: String): List<String>
}

@Service
class RedisTradingPairDistributionService(
    private val redisTemplate: StringRedisTemplate,
    private val binanceConfig: AppConfiguration.BinanceConfig,
    private val shardingService: ShardingService
) : TradingPairDistributionService {

    private val logger = KotlinLogging.logger { }

    companion object {
        const val SHARD_KEY = "sharding:assignments"
        const val LEADER_LOCK_KEY = "leader_lock"
        const val LOCK_TIMEOUT_SECONDS = 20L
    }

    override fun distribute() {
        val pods = shardingService.getActiveInstanceNames()

        val lockValue = UUID.randomUUID().toString()
        val lockAcquired = redisTemplate.opsForValue()
            .setIfAbsent(LEADER_LOCK_KEY, lockValue, LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS)

        if (lockAcquired == true) {
            try {
                rebalanceShards(pods)
            } finally {
                redisTemplate.delete(LEADER_LOCK_KEY)
            }
        } else {
            logger.info { "Another pod is currently the leader, skipping re-balance" }
        }
    }

    private fun rebalanceShards(pods: List<String>) {
        if (pods.isEmpty()) {
            logger.warn { "No active pods found, cannot re balance shards" }
            return
        }
        redisTemplate.delete(SHARD_KEY)
        val assignedPairs = mutableMapOf<String, MutableSet<String>>()
        binanceConfig.tradingPairs.forEachIndexed { index, pair ->
            val podIndex = index % pods.size
            val podName = pods[podIndex]
            assignedPairs.getOrPut(podName) { mutableSetOf() }.add(pair)
            logger.info { "Assigned pair $pair to pod $podName" }
        }

        storePodData(assignedPairs)
    }

    private fun storePodData(podData: Map<String, Set<String>>) {
        for ((podName, tradingPairs) in podData) {
            redisTemplate.opsForSet().add(podName, *tradingPairs.toTypedArray())
        }
    }

    override fun getAssignedPairs(podName: String): List<String> {
        return redisTemplate.opsForSet().members(podName)?.toList().orEmpty()
    }
}