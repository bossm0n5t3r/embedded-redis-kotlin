package redis.embedded

import redis.embedded.constants.RedisConstants.Architecture.ARCHITECTURE_ARM64
import redis.embedded.constants.RedisConstants.OS.Name.OS_NAME_MAC_OSX
import redis.embedded.constants.RedisConstants.REDIS_VERSION
import redis.embedded.constants.RedisConstants.Sentinel.REDIS_SENTINEL
import redis.embedded.constants.RedisConstants.Separator.HYPHEN

class RedisSentinelExecProvider : RedisExecProvider() {
    companion object {
        fun defaultProvider(): RedisExecProvider {
            return RedisSentinelExecProvider()
        }
    }

    init {
        initExecutables()
    }

    override fun initExecutables() {
        executables[OsArchitecture.MAC_OS_X_ARM64] =
            listOf(
                REDIS_SENTINEL,
                REDIS_VERSION,
                OS_NAME_MAC_OSX,
                ARCHITECTURE_ARM64,
            ).joinToString(HYPHEN)
    }
}
