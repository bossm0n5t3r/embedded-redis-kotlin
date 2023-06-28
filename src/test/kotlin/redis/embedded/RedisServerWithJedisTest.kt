package redis.embedded

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import redis.embedded.constants.RedisConstants.LOCALHOST
import redis.embedded.utils.generateRandomPort

class RedisServerWithJedisTest : AbstractJedisTest() {
    private lateinit var redisServer: RedisServer
    private lateinit var host: String
    private var port = -1
    private lateinit var jedisPool: JedisPool
    private lateinit var jedis: Jedis

    @BeforeEach
    fun setup() {
        host = LOCALHOST
        port = generateRandomPort()
    }

    @AfterEach
    fun cleanUp() {
        if (this::jedisPool.isInitialized) {
            jedisPool.close()
        }

        if (this::redisServer.isInitialized) {
            redisServer.stop()
        }
    }

    @Test
    fun testOperate() {
        redisServer = RedisServer(port)
        redisServer.start()

        jedisPool = JedisPool(host, port)
        jedis = jedisPool.resource

        assertDoesNotThrow { writeAndReadSuccess(jedis) }

        jedisPool.close()
        redisServer.stop()
    }

    @Test
    fun testOperateThenStandaloneDown() {
        redisServer = RedisServer(port)
        redisServer.start()

        jedisPool = JedisPool(host, port)
        jedis = jedisPool.resource

        assertDoesNotThrow { writeAndReadSuccess(jedis) }

        redisServer.stop()

        assertThrows<Exception> {
            jedisPool.resource
        }

        jedisPool.close()
        redisServer.stop()
    }

    @Test
    fun testOperateThenStandaloneDownUp() {
        redisServer = RedisServer(port)
        redisServer.start()

        jedisPool = JedisPool(host, port)
        jedis = jedisPool.resource

        assertDoesNotThrow { writeAndReadSuccess(jedis) }

        redisServer.stop()
        redisServer.start()

        val newJedis = jedisPool.resource

        assertDoesNotThrow { writeAndReadSuccess(newJedis) }

        jedisPool.close()
        redisServer.stop()
    }
}
