package redis.embedded

import redis.embedded.constants.RedisConstants.REDIS_VERSION
import redis.embedded.enums.Architecture
import redis.embedded.enums.OS
import redis.embedded.utils.JarUtil
import java.io.File
import java.io.IOException

class RedisExecProvider {
    companion object {
        fun defaultProvider(): RedisExecProvider {
            return RedisExecProvider()
        }
    }
    private val executables = mutableMapOf<OsArchitecture, String>()

    init {
        initExecutables()
    }

    private fun initExecutables() {
        executables[OsArchitecture.MAC_OS_X_ARM64] = "redis-server-$REDIS_VERSION-darwin-arm64"
    }

    fun override(os: OS, executable: String): RedisExecProvider {
        for (arch in Architecture.values()) {
            override(os, arch, executable)
        }
        return this
    }

    fun override(os: OS, arch: Architecture, executable: String): RedisExecProvider {
        executables[OsArchitecture(os, arch)] = executable
        return this
    }

    @Throws(IOException::class)
    fun get(): File {
        val osArch = OsArchitecture.detect()
        require(executables.containsKey(osArch)) { "No Redis executable found for $osArch" }
        val executablePath = executables[osArch]
        requireNotNull(executablePath) { "No Redis executablePath found for $osArch" }
        return if (fileExists(executablePath)) {
            File(executablePath)
        } else {
            JarUtil.extractExecutableFromJar(executablePath)
        }
    }

    private fun fileExists(executablePath: String): Boolean {
        return File(executablePath).exists()
    }
}
