package redis.embedded

import redis.embedded.exceptions.EmbeddedRedisException

@Suppress("TooManyFunctions")
class RedisCluster(
    private val redisServers: List<RedisServer>,
    private val redisClient: RedisClient,
) : IRedisServer {

    @Suppress("ReturnCount")
    override fun isActive(): Boolean {
        return redisServers.all { it.isActive() }
    }

    @Throws(EmbeddedRedisException::class)
    override fun start() {
        redisServers.forEach { it.start() }
        redisClient.run()
    }

    @Throws(EmbeddedRedisException::class)
    override fun stop() {
        redisServers.forEach { it.stop() }
    }

    override fun ports(): Set<Int> {
        return redisServers.flatMap { it.ports() }.toSet()
    }

    companion object {
        fun builder(): RedisClusterBuilder {
            return RedisClusterBuilder()
        }
    }
}
