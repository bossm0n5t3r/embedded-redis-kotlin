package redis.embedded

import redis.embedded.enums.Architecture
import redis.embedded.enums.OS
import redis.embedded.exceptions.OsDetectionException
import java.io.BufferedReader
import java.io.InputStreamReader

object OSDetector {
    fun getOS(): OS {
        val osName = System.getProperty("os.name").lowercase()

        return when {
            osName.contains("win") -> OS.WINDOWS
            osName.contains("nix") || osName.contains("nux") || osName.contains("aix") -> OS.UNIX
            "Mac OS X".equals(osName, ignoreCase = true) -> OS.MAC_OS_X
            else -> throw OsDetectionException("Unrecognized OS: $osName")
        }
    }

    private fun getWindowsArchitecture(): Architecture {
        val arch = System.getenv("PROCESSOR_ARCHITECTURE")
        val wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432")

        return when {
            arch.endsWith("64") || wow64Arch != null && wow64Arch.endsWith("64") -> Architecture.X86_64
            else -> Architecture.X86
        }
    }

    private fun getUnixArchitecture(): Architecture = try {
        val proc = Runtime.getRuntime().exec("uname -m")
        BufferedReader(InputStreamReader(proc.inputStream)).use { input ->
            return when (val machine = input.readLine()) {
                "aarch64" -> Architecture.ARM64
                "x86_64" -> Architecture.X86_64
                else -> throw OsDetectionException("unsupported architecture: $machine")
            }
        }
    } catch (e: Exception) {
        throw OsDetectionException(e)
    }

    private fun getMacOSXArchitecture(): Architecture = try {
        val proc = Runtime.getRuntime().exec("uname -m")
        BufferedReader(InputStreamReader(proc.inputStream)).use { input ->
            return when (val machine = input.readLine()) {
                "arm64" -> Architecture.ARM64
                "x86_64" -> Architecture.X86_64
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
