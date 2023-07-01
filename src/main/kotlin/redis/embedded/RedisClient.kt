package redis.embedded

class RedisClient(
    private val args: List<String>,
) : AbstractRedisClientInstance(args) {
    override fun redisClientReadyPattern(): String = REDIS_CLIENT_READY_PATTERN

    companion object {
        private const val REDIS_CLIENT_READY_PATTERN = ".*All 16384 slots covered.*"

        fun builder() = RedisClientBuilder()
    }
}
