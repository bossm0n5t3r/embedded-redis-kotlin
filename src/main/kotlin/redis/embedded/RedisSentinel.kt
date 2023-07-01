package redis.embedded

import redis.embedded.constants.RedisConstants.Server.DEFAULT_REDIS_PORT

class RedisSentinel(
    args: List<String>,
    sentinelPort: Int,
) : AbstractRedisServerInstance(
    args = args,
    sentinelPort = sentinelPort,
    masterPort = DEFAULT_REDIS_PORT,
) {
    override fun redisServerReadyPattern(): String = REDIS_READY_PATTERN
    override fun isActive(): Boolean = this.active

    companion object {
        private const val REDIS_READY_PATTERN = ".*Sentinel (runid|ID) is.*"

        fun builder(): RedisSentinelBuilder {
            return RedisSentinelBuilder()
        }
    }
}
