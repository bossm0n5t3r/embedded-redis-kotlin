package redis.embedded

import com.google.common.io.Resources
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertThrows
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import redis.embedded.RedisServer.Companion.DEFAULT_REDIS_PORT
import redis.embedded.enums.Architecture
import redis.embedded.enums.OS
import redis.embedded.exceptions.EmbeddedRedisException
import redis.embedded.exceptions.RedisBuildingException
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class RedisServerTest {
    private lateinit var redisServer: RedisServer

    @Test
    @Timeout(1500L)
    fun testSimpleRun() {
        redisServer = RedisServer(6379)
        redisServer.start()
        Thread.sleep(1000L)
        redisServer.stop()
    }

    @Test
    fun shouldNotAllowMultipleRunsWithoutStop() {
        assertThrows<EmbeddedRedisException> {
            try {
                redisServer = RedisServer(6379)
                redisServer.start()
                redisServer.start()
            } finally {
                redisServer.stop()
            }
        }
    }

    @Test
    fun shouldAllowSubsequentRuns() {
        redisServer = RedisServer(6379)

        redisServer.start()
        redisServer.stop()

        redisServer.start()
        redisServer.stop()

        redisServer.start()
        redisServer.stop()
    }

    @Test
    fun testSimpleOperationsAfterRun() {
        redisServer = RedisServer(DEFAULT_REDIS_PORT)
        redisServer.start()

        var pool: JedisPool? = null
        var jedis: Jedis? = null

        try {
            pool = JedisPool("localhost", 6379)
            jedis = pool.resource
            jedis.mset("abc", "1", "def", "2")

            assertEquals("1", jedis.mget("abc")[0])
            assertEquals("2", jedis.mget("def")[0])

            assertNull(jedis.mget("xyz")[0])
        } finally {
            if (jedis != null) pool?.returnResource(jedis)
            redisServer.stop()
        }
    }

    @Test
    fun shouldIndicateInactiveBeforeStart() {
        redisServer = RedisServer(DEFAULT_REDIS_PORT)
        assertFalse(redisServer.isActive())
    }

    @Test
    fun shouldIndicateActiveAfterStart() {
        redisServer = RedisServer(DEFAULT_REDIS_PORT)
        redisServer.start()
        assertTrue(redisServer.isActive())
        redisServer.stop()
    }

    @Test
    fun shouldIndicateInactiveAfterStop() {
        redisServer = RedisServer(DEFAULT_REDIS_PORT)
        redisServer.start()
        redisServer.stop()
        assertFalse(redisServer.isActive())
    }

    @Test
    fun shouldOverrideDefaultExecutable() {
        val customProvider = RedisExecProvider.defaultProvider()
            .override(
                OS.UNIX,
                Architecture.X86,
                Resources.getResource("redis-server-" + RedisExecProvider.REDIS_VERSION + "-linux-386").file,
            )
            .override(
                OS.UNIX,
                Architecture.X86_64,
                Resources.getResource("redis-server-" + RedisExecProvider.REDIS_VERSION + "-linux-amd64").file,
            )
            .override(
                OS.UNIX,
                Architecture.ARM64,
                Resources.getResource("redis-server-" + RedisExecProvider.REDIS_VERSION + "-linux-arm64").file,
            )
            .override(
                OS.MAC_OS_X,
                Architecture.X86_64,
                Resources.getResource("redis-server-" + RedisExecProvider.REDIS_VERSION + "-darwin-amd64").file,
            )
            .override(
                OS.MAC_OS_X,
                Architecture.ARM64,
                Resources.getResource("redis-server-" + RedisExecProvider.REDIS_VERSION + "-darwin-arm64").file,
            )
        redisServer = RedisServerBuilder()
            .redisExecProvider(customProvider)
            .build()
    }

    @Test
    fun shouldFailWhenBadExecutableGiven() {
        val buggyProvider = RedisExecProvider.defaultProvider()
            .override(OS.UNIX, "some")
            .override(OS.MAC_OS_X, "some")

        assertThrows<RedisBuildingException> {
            redisServer = RedisServerBuilder()
                .redisExecProvider(buggyProvider)
                .build()
        }
    }

    @Test
    fun testAwaitRedisServerReady() {
        val readyPattern = RedisServer.builder().build().redisReadyPattern()
        assertReadyPattern(
            javaClass
                .classLoader
                .getResourceAsStream("redis-2.x-standalone-startup-output.txt"),
            readyPattern,
        )
        assertReadyPattern(
            javaClass
                .classLoader
                .getResourceAsStream("redis-3.x-standalone-startup-output.txt"),
            readyPattern,
        )
        assertReadyPattern(
            javaClass
                .classLoader
                .getResourceAsStream("redis-4.x-standalone-startup-output.txt"),
            readyPattern,
        )
        assertReadyPattern(
            javaClass
                .classLoader
                .getResourceAsStream("redis-6.x-standalone-startup-output.txt"),
            readyPattern,
        )
    }

    private fun assertReadyPattern(inputStream: InputStream?, readyPattern: String) {
        requireNotNull(inputStream)
        val reader = BufferedReader(InputStreamReader(inputStream))

        var outputLine: String
        do {
            outputLine = reader.readLine()
            assertNotNull(outputLine)
        } while (!outputLine.matches(readyPattern.toRegex()))
    }
}
