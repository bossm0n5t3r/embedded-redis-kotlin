package redis.embedded

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertThrows
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import redis.embedded.constants.RedisConstants.LOCALHOST
import redis.embedded.constants.RedisConstants.REDIS_VERSION
import redis.embedded.constants.RedisConstants.Server.DEFAULT_REDIS_PORT
import redis.embedded.enums.Architecture
import redis.embedded.enums.OS
import redis.embedded.exceptions.EmbeddedRedisException
import redis.embedded.exceptions.RedisBuildingException
import redis.embedded.utils.ResourceUtil.getResource
import java.util.concurrent.TimeUnit

class RedisServerTest {
    private lateinit var redisServer: RedisServer

    @Test
    @Timeout(value = 1500L, unit = TimeUnit.MILLISECONDS)
    fun testSimpleRun() {
        redisServer = RedisServer(DEFAULT_REDIS_PORT)
        redisServer.start()
        Thread.sleep(1000L)
        redisServer.stop()
    }

    @Test
    fun shouldNotAllowMultipleRunsWithoutStop() {
        assertThrows<EmbeddedRedisException> {
            try {
                redisServer = RedisServer(DEFAULT_REDIS_PORT)
                redisServer.start()
                redisServer.start()
            } finally {
                redisServer.stop()
            }
        }
    }

    @Test
    fun shouldAllowSubsequentRuns() {
        redisServer = RedisServer(DEFAULT_REDIS_PORT)

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
            pool = JedisPool(LOCALHOST, DEFAULT_REDIS_PORT)
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
        val customProvider = RedisServerExecProvider.defaultProvider()
            .override(
                OS.MAC_OS_X,
                Architecture.ARM64,
                getResource("redis-server-$REDIS_VERSION-darwin-arm64").file,
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
