package redis.embedded.constants

object RedisConstants {
    const val REDIS_VERSION = "7.0.11"
    const val LOCALHOST = "localhost" // 0.0.0.0?

    object Separator {
        const val HYPHEN = "-"
    }

    object SystemProperty {
        const val OS_NAME = "os.name"
        const val PROCESSOR_ARCHITECTURE = "PROCESSOR_ARCHITECTURE"
        const val PROCESSOR_ARCHITEW6432 = "PROCESSOR_ARCHITEW6432"
    }

    object Command {
        const val UNAME_M = "uname -m"
    }

    object OS {
        const val OS_WINDOWS = "win"

        const val OS_NIX = "nix"
        const val OS_NUX = "nux"
        const val OS_AIX = "aix"

        const val OS_MAC_OSX = "Mac OS X"

        object Name {
            const val OS_NAME_MAC_OSX = "darwin"
            const val OS_NAME_LINUX = "linux"
        }
    }

    object Architecture {
        const val SIXTY_FOUR = "64"

        const val ARCHITECTURE_AARCH64 = "aarch64"

        const val ARCHITECTURE_ARM64 = "arm64"

        const val ARCHITECTURE_X86_64 = "x86_64"

        const val ARCHITECTURE_NAME_ARM64 = "arm64"
        const val ARCHITECTURE_NAME_AMD64 = "amd64"
    }

    object Server {
        const val REDIS_SERVER = "redis-server"
        const val DEFAULT_REDIS_HOST = "127.0.0.1" // 0.0.0.0?
        const val DEFAULT_REDIS_PORT = 6379
    }

    object Sentinel {
        const val REDIS_SENTINEL = "redis-sentinel"
        const val DEFAULT_HOST = "127.0.0.1" // 0.0.0.0?
        const val DEFAULT_PORT = 26379
        const val DEFAULT_MASTER_PORT = 6379
        const val DEFAULT_MASTER_NAME = "embedded-master-name"
    }
}
