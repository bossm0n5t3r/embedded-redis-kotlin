package redis.embedded

import java.io.File

class RedisServer : AbstractRedisServerInstance {
    constructor(executable: File, port: Int) : super(
        args = listOf(
            executable.absolutePath,
            "--port",
            port.toString(),
        ),
        port = port,
    )

    constructor(redisExecProvider: RedisExecProvider, port: Int) : super(
        args = listOf(
            redisExecProvider.get().absolutePath,
            "--port",
            port.toString(),
        ),
        port = port,
    )

    constructor(args: List<String>, port: Int) : super(args, port)

    override fun redisServerReadyPattern(): String = REDIS_SERVER_READY_PATTERN

    override fun isActive(): Boolean = this.active

    companion object {
        private const val REDIS_SERVER_READY_PATTERN = ".*(R|r)eady to accept connections.*"

        fun builder(): RedisServerBuilder {
            return RedisServerBuilder()
        }
    }
}
