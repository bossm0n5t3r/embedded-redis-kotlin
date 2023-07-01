package redis.embedded.utils

import redis.embedded.RedisSentinel
import redis.embedded.constants.RedisConstants.Server.DEFAULT_REDIS_HOST

object JedisUtil {
    fun sentinelJedisHosts(redisSentinel: RedisSentinel): Set<String> {
        val ports = redisSentinel.ports()
        return portsToJedisHosts(ports)
    }

    private fun portsToJedisHosts(ports: Set<Int>): Set<String> {
        return ports.map { "$DEFAULT_REDIS_HOST:$it" }.toSet()
    }
}
