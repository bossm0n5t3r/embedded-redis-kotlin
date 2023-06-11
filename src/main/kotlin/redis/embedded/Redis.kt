package redis.embedded

import redis.embedded.exceptions.EmbeddedRedisException

interface Redis {
    fun isActive(): Boolean

    @Throws(EmbeddedRedisException::class)
    fun start()

    @Throws(EmbeddedRedisException::class)
    fun stop()

    fun ports(): Set<Int>
    fun tlsPorts(): Set<Int>
}
