package redis.embedded

import redis.embedded.enums.Architecture
import redis.embedded.enums.OS

data class OsArchitecture(
    val os: OS,
    val architecture: Architecture,
) {
    companion object {
        val UNIX_X86 = OsArchitecture(OS.UNIX, Architecture.X86)
        val UNIX_X86_64 = OsArchitecture(OS.UNIX, Architecture.X86_64)
        val UNIX_ARM64 = OsArchitecture(OS.UNIX, Architecture.ARM64)

        val MAC_OS_X_X86_64 = OsArchitecture(OS.MAC_OS_X, Architecture.X86_64)
        val MAC_OS_X_ARM64 = OsArchitecture(OS.MAC_OS_X, Architecture.ARM64)

        fun detect() = OsArchitecture(
            os = OSDetector.getOS(),
            architecture = OSDetector.getArchitecture(),
        )
    }
}
