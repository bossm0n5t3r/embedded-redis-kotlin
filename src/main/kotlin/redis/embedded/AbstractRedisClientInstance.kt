package redis.embedded

abstract class AbstractRedisClientInstance(
    args: List<String>,
) : AbstractRedisInstance(args), IRedisClient {
    @Synchronized
    override fun run() {
        doStart()
    }

    protected abstract fun redisClientReadyPattern(): String

    override fun redisReadyPattern(): String = redisClientReadyPattern()
}
