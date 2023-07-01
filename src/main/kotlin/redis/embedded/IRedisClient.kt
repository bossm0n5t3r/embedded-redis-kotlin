package redis.embedded

import redis.embedded.exceptions.EmbeddedRedisException

interface IRedisClient {
    @Throws(EmbeddedRedisException::class)
    fun run()
}
