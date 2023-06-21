package redis.embedded.utils

import redis.embedded.Redis
import redis.embedded.RedisCluster
import redis.embedded.constants.RedisConstants.LOCALHOST

object JedisUtil {
    fun jedisHosts(redis: Redis): Set<String> {
        val ports = redis.ports()
        return portsToJedisHosts(ports)
    }

    fun sentinelHosts(cluster: RedisCluster): Set<String> {
        val ports = cluster.sentinelPorts()
        return portsToJedisHosts(ports)
    }

    private fun portsToJedisHosts(ports: Collection<Int>): Set<String> {
        return ports.map { "$LOCALHOST:$it" }.toSet()
    }
}
