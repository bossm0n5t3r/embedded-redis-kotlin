package redis.embedded

import redis.embedded.exceptions.RedisBuildingException

interface PortProvider {
    @Throws(RedisBuildingException::class)
    fun next(): Int
}
