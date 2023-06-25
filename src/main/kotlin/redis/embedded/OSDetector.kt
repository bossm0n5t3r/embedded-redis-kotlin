package redis.embedded

import redis.embedded.constants.RedisConstants.Architecture.ARCHITECTURE_AARCH64
import redis.embedded.constants.RedisConstants.Architecture.ARCHITECTURE_ARM64
import redis.embedded.constants.RedisConstants.Architecture.ARCHITECTURE_X86_64
import redis.embedded.constants.RedisConstants.Command.UNAME_M
import redis.embedded.constants.RedisConstants.OS.OS_AIX
import redis.embedded.constants.RedisConstants.OS.OS_MAC_OSX
import redis.embedded.constants.RedisConstants.OS.OS_NIX
import redis.embedded.constants.RedisConstants.OS.OS_NUX
import redis.embedded.constants.RedisConstants.SystemProperty.OS_NAME
import redis.embedded.enums.Architecture
import redis.embedded.enums.OS
import redis.embedded.exceptions.OsDetectionException
import java.io.BufferedReader
import java.io.InputStreamReader

object OSDetector {
    fun getOS(): OS {
        val osName = System.getProperty(OS_NAME).lowercase()

        return when {
            osName.contains(OS_NIX) ||
                osName.contains(OS_NUX) ||
                osName.contains(OS_AIX) -> OS.UNIX
            OS_MAC_OSX.equals(osName, ignoreCase = true) -> OS.MAC_OS_X
            else -> throw OsDetectionException("Unrecognized OS: $osName")
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun detectArchitecture(): Architecture = try {
        val proc = Runtime.getRuntime().exec(UNAME_M)
        BufferedReader(InputStreamReader(proc.inputStream)).use { input ->
            return when (val machine = input.readLine()) {
                ARCHITECTURE_AARCH64, ARCHITECTURE_ARM64 -> Architecture.ARM64
                ARCHITECTURE_X86_64 -> Architecture.AMD64
                else -> throw OsDetectionException("unsupported architecture: $machine")
            }
        }
    } catch (e: Exception) {
        throw OsDetectionException(e)
    }

    fun getArchitecture(): Architecture = when (getOS()) {
        OS.UNIX,
        OS.MAC_OS_X,
        -> detectArchitecture()
    }
}
