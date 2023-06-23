package redis.embedded

import redis.embedded.constants.RedisConstants.Server.DEFAULT_REDIS_PORT
import java.io.File

class RedisServer : AbstractRedisInstance {
    @JvmOverloads
    constructor(port: Int = DEFAULT_REDIS_PORT) : super(port) {
        args = builder().port(port).build().args
    }

    constructor(port: Int, tlsPort: Int) : super(port, tlsPort) {
        args = builder().port(port).tlsPort(tlsPort).build().args
    }

    constructor(executable: File, port: Int) : super(port) {
        args = mutableListOf(
            executable.absolutePath,
            "--port",
            port.toString(),
        )
    }

    constructor(redisExecProvider: RedisExecProvider, port: Int) : super(port) {
        args = mutableListOf(
            redisExecProvider.get().absolutePath,
            "--port",
            port.toString(),
        )
    }

    internal constructor(args: List<String>, port: Int, tlsPort: Int) : super(port, tlsPort) {
        this.args = args.toMutableList()
    }

    override fun redisReadyPattern(): String {
        return REDIS_READY_PATTERN
    }

    companion object {
        private const val REDIS_READY_PATTERN = ".*(R|r)eady to accept connections.*"

        fun builder(): RedisServerBuilder {
            return RedisServerBuilder()
        }
    }
}
