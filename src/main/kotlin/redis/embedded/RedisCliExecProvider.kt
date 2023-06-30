package redis.embedded

import redis.embedded.constants.RedisConstants

class RedisCliExecProvider private constructor() : RedisExecProvider() {
    companion object {
        fun defaultProvider(): RedisCliExecProvider {
            return RedisCliExecProvider()
        }
    }

    init {
        initExecutables()
    }

    override fun initExecutables() {
        executables[OsArchitecture.UNIX_AMD64] =
            listOf(
                RedisConstants.Cli.REDIS_CLI,
                RedisConstants.REDIS_VERSION,
                RedisConstants.OS.Name.OS_NAME_LINUX,
                RedisConstants.Architecture.ARCHITECTURE_NAME_AMD64,
            ).joinToString(RedisConstants.Separator.HYPHEN)

        executables[OsArchitecture.UNIX_ARM64] =
            listOf(
                RedisConstants.Cli.REDIS_CLI,
                RedisConstants.REDIS_VERSION,
                RedisConstants.OS.Name.OS_NAME_LINUX,
                RedisConstants.Architecture.ARCHITECTURE_NAME_ARM64,
            ).joinToString(RedisConstants.Separator.HYPHEN)

        executables[OsArchitecture.MAC_OS_X_ARM64] =
            listOf(
                RedisConstants.Cli.REDIS_CLI,
                RedisConstants.REDIS_VERSION,
                RedisConstants.OS.Name.OS_NAME_MAC_OSX,
                RedisConstants.Architecture.ARCHITECTURE_NAME_ARM64,
            ).joinToString(RedisConstants.Separator.HYPHEN)
    }
}
