package redis.embedded.exceptions

class EmbeddedRedisException(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {
    constructor(cause: Throwable) : this(null, cause)
}
