package redis.embedded

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisSentinelPool
import redis.embedded.constants.RedisConstants.LOCALHOST
import redis.embedded.constants.RedisConstants.Sentinel.DEFAULT_MASTER_NAME
import redis.embedded.utils.generateRandomPort
import java.util.concurrent.TimeUnit

class RedisSentinelTest {
    private lateinit var sentinel: RedisSentinel
    private lateinit var sentinelPortAndMasterPort: Pair<Int, Int>

    @BeforeEach
    fun setup() {
        sentinelPortAndMasterPort = generateRandomPort() to generateRandomPort()
        sentinel = RedisSentinel
            .builder()
            .port(sentinelPortAndMasterPort.first)
            .masterPort(sentinelPortAndMasterPort.second)
            .build()
    }

    @Test
    @Timeout(value = 3000L, unit = TimeUnit.MILLISECONDS)
    fun testSimpleRun() {
        sentinel.start()

        TimeUnit.SECONDS.sleep(1)

        sentinel.stop()
    }

    @Test
    fun shouldAllowSubsequentRuns() {
        sentinel.start()
        sentinel.stop()
        sentinel.start()
        sentinel.stop()
        sentinel.start()
        sentinel.stop()
    }

    @Test
    fun testSimpleOperationsAfterRun() {
        // given
        val redisServer = RedisServer(sentinelPortAndMasterPort.second)
        redisServer.start()
        sentinel.start()
        TimeUnit.SECONDS.sleep(1)

        // when
        var pool: JedisSentinelPool? = null
        var jedis: Jedis? = null
        try {
            pool = JedisSentinelPool(DEFAULT_MASTER_NAME, setOf("$LOCALHOST:${sentinelPortAndMasterPort.first}"))
            jedis = pool.resource
            jedis.mset("abc", "1", "def", "2")

            // then
            assertEquals("1", jedis.mget("abc")[0])
            assertEquals("2", jedis.mget("def")[0])
            assertNull(jedis.mget("xyz")[0])
        } finally {
            if (jedis != null) pool?.returnResource(jedis)
            sentinel.stop()
            redisServer.stop()
        }
    }
}
