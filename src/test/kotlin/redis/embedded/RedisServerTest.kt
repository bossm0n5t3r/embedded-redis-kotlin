package redis.embedded

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import redis.embedded.constants.RedisConstants
import redis.embedded.constants.RedisConstants.REDIS_VERSION
import redis.embedded.constants.RedisConstants.Server.DEFAULT_REDIS_PORT
import redis.embedded.enums.Architecture
import redis.embedded.enums.OS
import redis.embedded.exceptions.EmbeddedRedisException
import redis.embedded.exceptions.RedisBuildingException
import redis.embedded.utils.ResourceUtil.getResource
import redis.embedded.utils.generateRandomPort
import java.util.concurrent.TimeUnit

class RedisServerTest {
    private lateinit var redisServer: RedisServer

    @AfterEach
    fun cleanUp() {
        if (this::redisServer.isInitialized) {
            redisServer.stop()
        }
    }

    @Test
    @Timeout(value = 1500L, unit = TimeUnit.MILLISECONDS)
    fun testSimpleRun() {
        redisServer = RedisServer.builder().port(DEFAULT_REDIS_PORT).build()
        redisServer.start()
        Thread.sleep(1000L)
        redisServer.stop()
    }

    @Test
    fun shouldNotAllowMultipleRunsWithoutStop() {
        assertThrows<EmbeddedRedisException> {
            redisServer = RedisServer.builder().port(DEFAULT_REDIS_PORT).build()
            redisServer.start()
            redisServer.start()
            redisServer.stop()
        }
    }

    @Test
    fun shouldAllowMultipleStops() {
        assertDoesNotThrow {
            redisServer = RedisServer.builder().port(DEFAULT_REDIS_PORT).build()

            redisServer.stop()

            redisServer.start()
            redisServer.stop()
            redisServer.stop()
        }
    }

    @Test
    fun shouldAllowSubsequentRuns() {
        redisServer = RedisServer.builder().port(DEFAULT_REDIS_PORT).build()

        redisServer.start()
        redisServer.stop()

        redisServer.start()
        redisServer.stop()

        redisServer.start()
        redisServer.stop()
    }

    @Test
    fun shouldIndicateInactiveBeforeStart() {
        redisServer = RedisServer.builder().port(DEFAULT_REDIS_PORT).build()
        assertFalse(redisServer.isActive())
    }

    @Test
    fun testPorts() {
        val randomPort = generateRandomPort()
        redisServer = RedisServer.builder().port(randomPort).build()
        assertEquals(redisServer.ports(), setOf(randomPort))
    }

    @Test
    fun shouldIndicateActiveAfterStart() {
        redisServer = RedisServer.builder().port(DEFAULT_REDIS_PORT).build()
        redisServer.start()
        assertTrue(redisServer.isActive())
        redisServer.stop()
    }

    @Test
    fun shouldIndicateInactiveAfterStop() {
        redisServer = RedisServer.builder().port(DEFAULT_REDIS_PORT).build()
        redisServer.start()
        redisServer.stop()
        assertFalse(redisServer.isActive())
    }

    @Test
    fun shouldOverrideDefaultExecutable() {
        val customProvider = RedisServerExecProvider.defaultProvider()
            .override(
                OS.UNIX,
                Architecture.AMD64,
                getResource(
                    listOf(
                        RedisConstants.Server.REDIS_SERVER,
                        REDIS_VERSION,
                        RedisConstants.OS.Name.OS_NAME_LINUX,
                        RedisConstants.Architecture.ARCHITECTURE_NAME_AMD64,
                    ).joinToString(RedisConstants.Separator.HYPHEN),
                ).file,
            )
            .override(
                OS.UNIX,
                Architecture.ARM64,
                getResource(
                    listOf(
                        RedisConstants.Server.REDIS_SERVER,
                        REDIS_VERSION,
                        RedisConstants.OS.Name.OS_NAME_LINUX,
                        RedisConstants.Architecture.ARCHITECTURE_NAME_ARM64,
                    ).joinToString(RedisConstants.Separator.HYPHEN),
                ).file,
            )
            .override(
                OS.MAC_OS_X,
                Architecture.ARM64,
                getResource(
                    listOf(
                        RedisConstants.Server.REDIS_SERVER,
                        REDIS_VERSION,
                        RedisConstants.OS.Name.OS_NAME_MAC_OSX,
                        RedisConstants.Architecture.ARCHITECTURE_NAME_ARM64,
                    ).joinToString(RedisConstants.Separator.HYPHEN),
                ).file,
            )
        redisServer = RedisServerBuilder()
            .redisExecProvider(customProvider)
            .build()
    }

    @Test
    fun shouldFailWhenBadExecutableGiven() {
        val buggyProvider = RedisServerExecProvider.defaultProvider()
            .override(OS.UNIX, "some")
            .override(OS.MAC_OS_X, "some")

        assertThrows<RedisBuildingException> {
            redisServer = RedisServerBuilder()
                .redisExecProvider(buggyProvider)
                .build()
        }
    }
}
