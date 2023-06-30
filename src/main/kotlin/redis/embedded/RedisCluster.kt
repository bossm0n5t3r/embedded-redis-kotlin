package redis.embedded

import redis.embedded.exceptions.EmbeddedRedisException

@Suppress("TooManyFunctions")
class RedisCluster(
    private val sentinels: List<Redis>,
    private val servers: List<Redis>,
) : Redis {

    @Suppress("ReturnCount")
    override fun isActive(): Boolean {
        for (redis in sentinels) {
            if (!redis.isActive()) {
                return false
            }
        }
        for (redis in servers) {
            if (!redis.isActive()) {
                return false
            }
        }
        return true
    }

    @Throws(EmbeddedRedisException::class)
    override fun start() {
        for (redis in sentinels) {
            redis.start()
        }
        for (redis in servers) {
            redis.start()
        }
    }

    @Throws(EmbeddedRedisException::class)
    override fun stop() {
        for (redis in sentinels) {
            redis.stop()
        }
        for (redis in servers) {
            redis.stop()
        }
    }

    override fun ports(): Set<Int> {
        return (sentinelPorts() + serverPorts()).toSet()
    }

    override fun tlsPorts(): Set<Int> {
        return (sentinelTlsPorts() + serverTlsPorts()).toSet()
    }

    fun sentinels(): List<Redis> {
        return sentinels.toList()
    }

    fun sentinelPorts(): Set<Int> {
        return sentinels.flatMap { it.ports() }.toSet()
    }

    private fun sentinelTlsPorts(): Set<Int> {
        return sentinels.flatMap { it.tlsPorts() }.toSet()
    }

    fun servers(): List<Redis> {
        return servers.toList()
    }

    private fun serverPorts(): List<Int> {
        return servers.flatMap { it.ports() }
    }

    private fun serverTlsPorts(): List<Int> {
        return servers.flatMap { it.tlsPorts() }
    }

    companion object {
        fun builder(): RedisClusterBuilder {
            return RedisClusterBuilder()
        }
    }
}
