package redis.embedded

import redis.embedded.constants.RedisConstants.Architecture.ARCHITECTURE_ARM64
import redis.embedded.constants.RedisConstants.OS.Name.OS_NAME_MAC_OSX
import redis.embedded.constants.RedisConstants.REDIS_VERSION
import redis.embedded.constants.RedisConstants.Separator.HYPHEN
import redis.embedded.constants.RedisConstants.Server.REDIS_SERVER

class RedisServerExecProvider : RedisExecProvider() {
    companion object {
        fun defaultProvider(): RedisExecProvider {
            return RedisServerExecProvider()
        }
    }

    init {
        initExecutables()
    }

    override fun initExecutables() {
        executables[OsArchitecture.MAC_OS_X_ARM64] =
            listOf(
                REDIS_SERVER,
                REDIS_VERSION,
                OS_NAME_MAC_OSX,
                ARCHITECTURE_ARM64,
            ).joinToString(HYPHEN)
    }
}
