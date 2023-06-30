package redis.embedded

import redis.embedded.constants.RedisConstants.Architecture.ARCHITECTURE_NAME_AMD64
import redis.embedded.constants.RedisConstants.Architecture.ARCHITECTURE_NAME_ARM64
import redis.embedded.constants.RedisConstants.OS.Name.OS_NAME_LINUX
import redis.embedded.constants.RedisConstants.OS.Name.OS_NAME_MAC_OSX
import redis.embedded.constants.RedisConstants.REDIS_VERSION
import redis.embedded.constants.RedisConstants.Sentinel.REDIS_SENTINEL
import redis.embedded.constants.RedisConstants.Separator.HYPHEN

class RedisSentinelExecProvider private constructor() : RedisExecProvider() {
    companion object {
        fun defaultProvider(): RedisExecProvider {
            return RedisSentinelExecProvider()
        }
    }

    init {
        initExecutables()
    }

    override fun initExecutables() {
        executables[OsArchitecture.UNIX_AMD64] =
            listOf(
                REDIS_SENTINEL,
                REDIS_VERSION,
                OS_NAME_LINUX,
                ARCHITECTURE_NAME_AMD64,
            ).joinToString(HYPHEN)

        executables[OsArchitecture.UNIX_ARM64] =
            listOf(
                REDIS_SENTINEL,
                REDIS_VERSION,
                OS_NAME_LINUX,
                ARCHITECTURE_NAME_ARM64,
            ).joinToString(HYPHEN)

        executables[OsArchitecture.MAC_OS_X_ARM64] =
            listOf(
                REDIS_SENTINEL,
                REDIS_VERSION,
                OS_NAME_MAC_OSX,
                ARCHITECTURE_NAME_ARM64,
            ).joinToString(HYPHEN)
    }
}
