package redis.embedded

import redis.embedded.constants.RedisConstants.REDIS_VERSION

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
        executables[OsArchitecture.MAC_OS_X_ARM64] = "redis-server-$REDIS_VERSION-darwin-arm64"
    }
}
