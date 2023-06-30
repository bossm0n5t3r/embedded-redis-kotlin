package redis.embedded.utils

import redis.embedded.RedisSentinel
import redis.embedded.constants.RedisConstants.LOCALHOST

object JedisUtil {
    fun sentinelJedisHosts(redisSentinel: RedisSentinel): Set<String> {
        val ports = redisSentinel.ports()
        return portsToJedisHosts(ports)
    }

    private fun portsToJedisHosts(ports: Set<Int>): Set<String> {
        return ports.map { "$LOCALHOST:$it" }.toSet()
    }
}
