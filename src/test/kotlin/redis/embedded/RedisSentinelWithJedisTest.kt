package redis.embedded

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import redis.clients.jedis.JedisSentinelPool
import redis.embedded.constants.RedisConstants.Server.DEFAULT_REDIS_HOST
import redis.embedded.utils.JedisUtil
import redis.embedded.utils.generateRandomString
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class RedisSentinelWithJedisTest : AbstractJedisTest() {
    private lateinit var redisSentinel: RedisSentinel
    private lateinit var sentinelPool: JedisSentinelPool

    private lateinit var masterServer: RedisServer
    private lateinit var slaveServer: RedisServer

    private lateinit var masterHost: String
    private var masterPort = -1

    private lateinit var slaveHost: String
    private var slavePort = -1

    private lateinit var sentinelHost: String
    private var sentinelPort = -1

    private lateinit var masterName: String

    @BeforeEach
    fun setup() {
        masterHost = DEFAULT_REDIS_HOST
        masterPort = Random.nextInt(10001, 11000)

        slaveHost = DEFAULT_REDIS_HOST
        slavePort = Random.nextInt(11001, 12000)

        sentinelHost = DEFAULT_REDIS_HOST
        sentinelPort = Random.nextInt(12001, 13000)

        masterName = generateRandomString(50, 100)
    }

    @AfterEach
    fun cleanUp() {
        TimeUnit.SECONDS.sleep(5)

        if (this::sentinelPool.isInitialized) {
            sentinelPool.close()
            sentinelPool.destroy()
        }

        if (this::slaveServer.isInitialized) {
            slaveServer.stop()
        }

        if (this::masterServer.isInitialized) {
            masterServer.stop()
        }

        if (this::redisSentinel.isInitialized) {
            redisSentinel.stop()
        }

        TimeUnit.SECONDS.sleep(5)
    }

    @Test
    fun testOperate() {
        masterServer = RedisServer.builder().port(masterPort).build()
        slaveServer = RedisServer.builder().port(slavePort).replicaOf(masterPort).build()

        masterServer.start()
        slaveServer.start()

        redisSentinel = RedisSentinel.builder()
            .sentinelPort(sentinelPort)
            .masterPort(masterPort)
            .masterName(masterName)
            .downAfterMilliseconds(1000L)
            .failoverTimeout(1000L)
            .quorumSize(1)
            .build()

        redisSentinel.start()

        val sentinelJedisHosts = JedisUtil.sentinelJedisHosts(redisSentinel)
        sentinelPool = JedisSentinelPool(masterName, sentinelJedisHosts)

        val sentinelJedis = sentinelPool.resource

        assertDoesNotThrow { writeAndReadSuccess(sentinelJedis) }

        sentinelPool.close()
        redisSentinel.stop()
        masterServer.stop()
        slaveServer.stop()
    }

    @Test
    fun testOperateThenMasterDown() {
        masterServer = RedisServer.builder().port(masterPort).build()
        slaveServer = RedisServer.builder().port(slavePort).replicaOf(masterPort).build()

        masterServer.start()
        slaveServer.start()

        redisSentinel = RedisSentinel.builder()
            .sentinelPort(sentinelPort)
            .masterPort(masterPort)
            .masterName(masterName)
            .downAfterMilliseconds(1000L)
            .failoverTimeout(1000L)
            .quorumSize(1)
            .build()

        redisSentinel.start()

        val sentinelJedisHosts = JedisUtil.sentinelJedisHosts(redisSentinel)
        sentinelPool = JedisSentinelPool(masterName, sentinelJedisHosts)

        val sentinelJedis = sentinelPool.resource

        assertDoesNotThrow { writeAndReadSuccess(sentinelJedis) }

        masterServer.stop()
        TimeUnit.SECONDS.sleep(10)

        val newSentinelJedis = sentinelPool.resource

        assertDoesNotThrow { writeAndReadSuccess(newSentinelJedis) }

        sentinelPool.close()
        slaveServer.stop()
        masterServer.stop()
        redisSentinel.stop()
    }

    @Test
    fun testOperateThenMasterDownUp() {
        masterServer = RedisServer.builder().port(masterPort).build()
        slaveServer = RedisServer.builder().port(slavePort).replicaOf(masterPort).build()

        masterServer.start()
        slaveServer.start()

        redisSentinel = RedisSentinel.builder()
            .sentinelPort(sentinelPort)
            .masterPort(masterPort)
            .masterName(masterName)
            .downAfterMilliseconds(1000L)
            .failoverTimeout(1000L)
            .quorumSize(1)
            .build()

        redisSentinel.start()

        val sentinelJedisHosts = JedisUtil.sentinelJedisHosts(redisSentinel)
        sentinelPool = JedisSentinelPool(masterName, sentinelJedisHosts)

        val sentinelJedis = sentinelPool.resource

        assertDoesNotThrow { writeAndReadSuccess(sentinelJedis) }

        masterServer.stop()
        TimeUnit.SECONDS.sleep(10)

        masterServer.start()
        TimeUnit.SECONDS.sleep(5)

        val newSentinelJedis = sentinelPool.resource

        assertDoesNotThrow { writeAndReadSuccess(newSentinelJedis) }

        sentinelPool.close()
        slaveServer.stop()
        masterServer.stop()
        redisSentinel.stop()
    }

    @Test
    fun testOperateThenSlaveDown() {
        masterServer = RedisServer.builder().port(masterPort).build()
        slaveServer = RedisServer.builder().port(slavePort).replicaOf(masterPort).build()

        masterServer.start()
        slaveServer.start()

        redisSentinel = RedisSentinel.builder()
            .sentinelPort(sentinelPort)
            .masterPort(masterPort)
            .masterName(masterName)
            .downAfterMilliseconds(1000L)
            .failoverTimeout(1000L)
            .quorumSize(1)
            .build()

        redisSentinel.start()

        val sentinelJedisHosts = JedisUtil.sentinelJedisHosts(redisSentinel)
        sentinelPool = JedisSentinelPool(masterName, sentinelJedisHosts)

        val sentinelJedis = sentinelPool.resource

        assertDoesNotThrow { writeAndReadSuccess(sentinelJedis) }

        slaveServer.stop()
        TimeUnit.SECONDS.sleep(5)

        val newSentinelJedis = sentinelPool.resource

        assertDoesNotThrow { writeAndReadSuccess(newSentinelJedis) }

        sentinelPool.close()
        slaveServer.stop()
        masterServer.stop()
        redisSentinel.stop()
    }

    @Test
    fun testOperateThenSlaveDownUp() {
        masterServer = RedisServer.builder().port(masterPort).build()
        slaveServer = RedisServer.builder().port(slavePort).replicaOf(masterPort).build()

        masterServer.start()
        slaveServer.start()

        redisSentinel = RedisSentinel.builder()
            .sentinelPort(sentinelPort)
            .masterPort(masterPort)
            .masterName(masterName)
            .downAfterMilliseconds(1000L)
            .failoverTimeout(1000L)
            .quorumSize(1)
            .build()

        redisSentinel.start()

        val sentinelJedisHosts = JedisUtil.sentinelJedisHosts(redisSentinel)
        sentinelPool = JedisSentinelPool(masterName, sentinelJedisHosts)

        val sentinelJedis = sentinelPool.resource

        assertDoesNotThrow { writeAndReadSuccess(sentinelJedis) }

        slaveServer.stop()
        TimeUnit.SECONDS.sleep(5)

        slaveServer.start()
        TimeUnit.SECONDS.sleep(5)

        val newSentinelJedis = sentinelPool.resource

        assertDoesNotThrow { writeAndReadSuccess(newSentinelJedis) }

        sentinelPool.close()
        slaveServer.stop()
        masterServer.stop()
        redisSentinel.stop()
    }

    @Test
    fun testOperateThenMasterDownAndSlaveDown() {
        masterServer = RedisServer.builder().port(masterPort).build()
        slaveServer = RedisServer.builder().port(slavePort).replicaOf(masterPort).build()

        masterServer.start()
        slaveServer.start()

        redisSentinel = RedisSentinel.builder()
            .sentinelPort(sentinelPort)
            .masterPort(masterPort)
            .masterName(masterName)
            .downAfterMilliseconds(1000L)
            .failoverTimeout(1000L)
            .quorumSize(1)
            .build()

        redisSentinel.start()

        val sentinelJedisHosts = JedisUtil.sentinelJedisHosts(redisSentinel)
        sentinelPool = JedisSentinelPool(masterName, sentinelJedisHosts)

        val sentinelJedis = sentinelPool.resource

        assertDoesNotThrow { writeAndReadSuccess(sentinelJedis) }

        masterServer.stop()
        TimeUnit.SECONDS.sleep(5)

        slaveServer.stop()
        TimeUnit.SECONDS.sleep(5)

        assertThrows<Exception> {
            sentinelPool.resource
        }

        sentinelPool.close()
        slaveServer.stop()
        masterServer.stop()
        redisSentinel.stop()
    }

    @Test
    fun testOperateThenMasterDownUpAndSlaveDownUp() {
        masterServer = RedisServer.builder().port(masterPort).build()
        slaveServer = RedisServer.builder().port(slavePort).replicaOf(masterPort).build()

        masterServer.start()
        slaveServer.start()

        redisSentinel = RedisSentinel.builder()
            .sentinelPort(sentinelPort)
            .masterPort(masterPort)
            .masterName(masterName)
            .downAfterMilliseconds(1000L)
            .failoverTimeout(1000L)
            .quorumSize(1)
            .build()

        redisSentinel.start()

        val sentinelJedisHosts = JedisUtil.sentinelJedisHosts(redisSentinel)
        sentinelPool = JedisSentinelPool(masterName, sentinelJedisHosts)

        val sentinelJedis = sentinelPool.resource

        assertDoesNotThrow { writeAndReadSuccess(sentinelJedis) }

        masterServer.stop()
        TimeUnit.SECONDS.sleep(5)

        slaveServer.stop()
        TimeUnit.SECONDS.sleep(5)

        masterServer.start()
        TimeUnit.SECONDS.sleep(5)

        slaveServer.start()
        TimeUnit.SECONDS.sleep(5)

        val newSentinelJedis = sentinelPool.resource

        assertDoesNotThrow { writeAndReadSuccess(newSentinelJedis) }

        sentinelPool.close()
        slaveServer.stop()
        masterServer.stop()
        redisSentinel.stop()
    }

    @Test
    fun testOperateThenMasterDownAndSlaveDownUp() {
        masterServer = RedisServer.builder().port(masterPort).build()
        slaveServer = RedisServer.builder().port(slavePort).replicaOf(masterPort).build()

        masterServer.start()
        slaveServer.start()

        redisSentinel = RedisSentinel.builder()
            .sentinelPort(sentinelPort)
            .masterPort(masterPort)
            .masterName(masterName)
            .downAfterMilliseconds(1000L)
            .failoverTimeout(1000L)
            .quorumSize(1)
            .build()

        redisSentinel.start()

        val sentinelJedisHosts = JedisUtil.sentinelJedisHosts(redisSentinel)
        sentinelPool = JedisSentinelPool(masterName, sentinelJedisHosts)

        val sentinelJedis = sentinelPool.resource

        assertDoesNotThrow { writeAndReadSuccess(sentinelJedis) }

        masterServer.stop()
        TimeUnit.SECONDS.sleep(5)

        slaveServer.stop()
        TimeUnit.SECONDS.sleep(5)

        slaveServer.start()
        TimeUnit.SECONDS.sleep(5)

        val newSentinelJedis = sentinelPool.resource

        assertThrows<Exception> {
            msetAll(newSentinelJedis)
        }

        assertThrows<Exception> {
            setAll(newSentinelJedis)
        }

        assertDoesNotThrow {
            mgetNothing(newSentinelJedis)
            getNothing(newSentinelJedis)
        }

        sentinelPool.close()
        slaveServer.stop()
        masterServer.stop()
        redisSentinel.stop()
    }

    @Test
    fun testOperateThenMasterDownUpAndSlaveDown() {
        masterServer = RedisServer.builder().port(masterPort).build()
        slaveServer = RedisServer.builder().port(slavePort).replicaOf(masterPort).build()

        masterServer.start()
        slaveServer.start()

        redisSentinel = RedisSentinel.builder()
            .sentinelPort(sentinelPort)
            .masterPort(masterPort)
            .masterName(masterName)
            .downAfterMilliseconds(1000L)
            .failoverTimeout(1000L)
            .quorumSize(1)
            .build()

        redisSentinel.start()

        val sentinelJedisHosts = JedisUtil.sentinelJedisHosts(redisSentinel)
        sentinelPool = JedisSentinelPool(masterName, sentinelJedisHosts)

        val sentinelJedis = sentinelPool.resource

        assertDoesNotThrow { writeAndReadSuccess(sentinelJedis) }

        masterServer.stop()
        TimeUnit.SECONDS.sleep(5)

        slaveServer.stop()
        TimeUnit.SECONDS.sleep(5)

        masterServer.start()
        TimeUnit.SECONDS.sleep(5)

        val newSentinelJedis = sentinelPool.resource

        assertDoesNotThrow { writeAndReadSuccess(newSentinelJedis) }

        sentinelPool.close()
        slaveServer.stop()
        masterServer.stop()
        redisSentinel.stop()
    }

    @Test
    fun testOperateThenSentinelDown() {
        masterServer = RedisServer.builder().port(masterPort).build()
        slaveServer = RedisServer.builder().port(slavePort).replicaOf(masterPort).build()

        masterServer.start()
        slaveServer.start()

        redisSentinel = RedisSentinel.builder()
            .sentinelPort(sentinelPort)
            .masterPort(masterPort)
            .masterName(masterName)
            .downAfterMilliseconds(1000L)
            .failoverTimeout(1000L)
            .quorumSize(1)
            .build()

        redisSentinel.start()

        val sentinelJedisHosts = JedisUtil.sentinelJedisHosts(redisSentinel)
        sentinelPool = JedisSentinelPool(masterName, sentinelJedisHosts)

        val sentinelJedis = sentinelPool.resource

        assertDoesNotThrow { writeAndReadSuccess(sentinelJedis) }

        redisSentinel.stop()
        TimeUnit.SECONDS.sleep(5)

        val newSentinelJedis = sentinelPool.resource

        assertDoesNotThrow { writeAndReadSuccess(newSentinelJedis) }

        sentinelPool.close()
        slaveServer.stop()
        masterServer.stop()
        redisSentinel.stop()
    }

    @Test
    fun testOperateThenSentinelDownUp() {
        masterServer = RedisServer.builder().port(masterPort).build()
        slaveServer = RedisServer.builder().port(slavePort).replicaOf(masterPort).build()

        masterServer.start()
        slaveServer.start()

        redisSentinel = RedisSentinel.builder()
            .sentinelPort(sentinelPort)
            .masterPort(masterPort)
            .masterName(masterName)
            .downAfterMilliseconds(1000L)
            .failoverTimeout(1000L)
            .quorumSize(1)
            .build()

        redisSentinel.start()

        val sentinelJedisHosts = JedisUtil.sentinelJedisHosts(redisSentinel)
        sentinelPool = JedisSentinelPool(masterName, sentinelJedisHosts)

        val sentinelJedis = sentinelPool.resource

        assertDoesNotThrow { writeAndReadSuccess(sentinelJedis) }

        redisSentinel.stop()
        TimeUnit.SECONDS.sleep(5)

        redisSentinel.start()
        TimeUnit.SECONDS.sleep(5)

        val newSentinelJedis = sentinelPool.resource

        assertDoesNotThrow { writeAndReadSuccess(newSentinelJedis) }

        sentinelPool.close()
        slaveServer.stop()
        masterServer.stop()
        redisSentinel.stop()
    }
}
