package redis.embedded

import redis.embedded.constants.RedisConstants.Architecture.ARCHITECTURE_AARCH64
import redis.embedded.constants.RedisConstants.Architecture.ARCHITECTURE_ARM64
import redis.embedded.constants.RedisConstants.Architecture.ARCHITECTURE_X86_64
import redis.embedded.constants.RedisConstants.Architecture.SIXTY_FOUR
import redis.embedded.constants.RedisConstants.Command.UNAME_M
import redis.embedded.constants.RedisConstants.OS.OS_AIX
import redis.embedded.constants.RedisConstants.OS.OS_MAC_OSX
import redis.embedded.constants.RedisConstants.OS.OS_NIX
import redis.embedded.constants.RedisConstants.OS.OS_NUX
import redis.embedded.constants.RedisConstants.OS.OS_WINDOWS
import redis.embedded.constants.RedisConstants.SystemProperty.OS_NAME
import redis.embedded.constants.RedisConstants.SystemProperty.PROCESSOR_ARCHITECTURE
import redis.embedded.constants.RedisConstants.SystemProperty.PROCESSOR_ARCHITEW6432
import redis.embedded.enums.Architecture
import redis.embedded.enums.OS
import redis.embedded.exceptions.OsDetectionException
import java.io.BufferedReader
import java.io.InputStreamReader

object OSDetector {
    fun getOS(): OS {
        val osName = System.getProperty(OS_NAME).lowercase()

        return when {
            osName.contains(OS_WINDOWS) -> OS.WINDOWS
            osName.contains(OS_NIX) ||
                osName.contains(OS_NUX) ||
                osName.contains(OS_AIX) -> OS.UNIX
            OS_MAC_OSX.equals(osName, ignoreCase = true) -> OS.MAC_OS_X
            else -> throw OsDetectionException("Unrecognized OS: $osName")
        }
    }

    private fun getWindowsArchitecture(): Architecture {
        val arch = System.getenv(PROCESSOR_ARCHITECTURE)
        val wow64Arch = System.getenv(PROCESSOR_ARCHITEW6432)

        return when {
            arch.endsWith(SIXTY_FOUR) || wow64Arch != null && wow64Arch.endsWith(SIXTY_FOUR) -> Architecture.X86_64
            else -> Architecture.X86
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun getUnixArchitecture(): Architecture = try {
        val proc = Runtime.getRuntime().exec(UNAME_M)
        BufferedReader(InputStreamReader(proc.inputStream)).use { input ->
            return when (val machine = input.readLine()) {
                ARCHITECTURE_AARCH64 -> Architecture.ARM64
                ARCHITECTURE_X86_64 -> Architecture.X86_64
                else -> throw OsDetectionException("unsupported architecture: $machine")
            }
        }
    } catch (e: Exception) {
        throw OsDetectionException(e)
    }

    @Suppress("TooGenericExceptionCaught")
    private fun getMacOSXArchitecture(): Architecture = try {
        val proc = Runtime.getRuntime().exec(UNAME_M)
        BufferedReader(InputStreamReader(proc.inputStream)).use { input ->
            return when (val machine = input.readLine()) {
                ARCHITECTURE_ARM64 -> Architecture.ARM64
                ARCHITECTURE_X86_64 -> Architecture.X86_64
                else -> throw OsDetectionException("unsupported architecture: $machine")
            }
        }
    } catch (e: Exception) {
        throw OsDetectionException(e)
    }

    fun getArchitecture(): Architecture = when (val os = getOS()) {
        OS.WINDOWS -> getWindowsArchitecture()
        OS.UNIX -> getUnixArchitecture()
        OS.MAC_OS_X -> getMacOSXArchitecture()
        else -> throw OsDetectionException("Unrecognized OS: $os")
    }
}
