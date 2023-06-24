package redis.embedded.constants

object RedisConstants {
    const val REDIS_VERSION = "7.0.11"

    const val LOCALHOST = "localhost"

    object Server {
        const val DEFAULT_REDIS_PORT = 6379
    }

    object Sentinel {
        const val DEFAULT_PORT = 26379
        const val DEFAULT_MASTER_PORT = 6379
        const val DEFAULT_MASTER_NAME = "embedded-master-name"
    }
}
