package com.ndlabs.binance.sharding

import com.ndlabs.binance.configuration.AppConfiguration
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
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
): TradingPairDistributionService {

    private val logger = KotlinLogging.logger { }

    private val shardAssignments = ConcurrentHashMap<String, String>()

    companion object {
        const val SHARD_KEY = "sharding:assignments"
        const val LEADER_LOCK_KEY = "leader_lock"
        const val LOCK_TIMEOUT_SECONDS = 20L
    }

    override fun distribute() {
        val pods = shardingService.getActiveInstanceNames()
        rebalanceShards(pods)
    }

    private fun rebalanceShards(pods: List<String>) {

        val lockAcquired = redisTemplate.opsForValue()
            .setIfAbsent(LEADER_LOCK_KEY, "locked", LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS)

        if (lockAcquired == true) {
            if (pods.isEmpty()) {
                logger.warn { "No active pods found, cannot rebalance shards" }
                return
            }
            redisTemplate.delete(SHARD_KEY)
            shardAssignments.clear()
            binanceConfig.tradingPairs.forEachIndexed { index, pair ->
                val podIndex = index % pods.size
                val podName = pods[podIndex]
                shardAssignments[pair] = podName
                redisTemplate.opsForHash<String, String>().put(SHARD_KEY, pair, podName)
                logger.info { "Assigned pair $pair to pod $podName" }
            }

        } else {
            logger.info { "Another pod is currently the leader, skipping rebalance" }
        }
    }

    override fun getAssignedPairs(podName: String): List<String> {
        return shardAssignments.filter { it.value == podName }.keys.toList()
    }
}