package redis.embedded

class RedisSentinel(port: Int, args: List<String>) : AbstractRedisInstance(port) {
    init {
        this.args = args.toMutableList()
    }

    override fun redisReadyPattern(): String {
        return REDIS_READY_PATTERN
    }

    companion object {
        private const val REDIS_READY_PATTERN = ".*Sentinel (runid|ID) is.*"

        fun builder(): RedisSentinelBuilder {
            return RedisSentinelBuilder()
        }
    }
}
