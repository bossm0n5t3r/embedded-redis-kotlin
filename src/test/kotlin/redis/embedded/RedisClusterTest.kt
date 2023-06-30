package redis.embedded

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisSentinelPool
import redis.embedded.utils.JedisUtil
import redis.embedded.utils.generateRandomPort
import java.io.Closeable
import java.io.IOException

class RedisClusterTest {
    private val sentinelBuilder = RedisSentinel.builder()
    private lateinit var sentinelPortAndMasterPort: Pair<Int, Int>

    private val sentinel1 = mockk<Redis>()
    private val sentinel2 = mockk<Redis>()
    private val master1 = mockk<Redis>()
    private val master2 = mockk<Redis>()

    private lateinit var instance: RedisCluster

    @BeforeEach
    fun setup() {
        clearAllMocks()

        sentinelPortAndMasterPort = generateRandomPort() to generateRandomPort()
        sentinelBuilder
            .port(sentinelPortAndMasterPort.first)
            .masterPort(sentinelPortAndMasterPort.second)
    }

    @Test
    fun stopShouldStopEntireCluster() {
        // given
        val sentinels = listOf(sentinel1, sentinel2)
        val servers = listOf(master1, master2)
        justRun { sentinel1.stop() }
        justRun { sentinel2.stop() }
        justRun { master1.stop() }
        justRun { master2.stop() }
        instance = RedisCluster(sentinels, servers)

        // when
        instance.stop()

        // then
        for (s in sentinels) {
            verify { s.stop() }
        }
        for (s in servers) {
            verify { s.stop() }
        }
    }

    @Test
    fun startShouldStartEntireCluster() {
        // given
        val sentinels = listOf(sentinel1, sentinel2)
        val servers = listOf(master1, master2)
        justRun { sentinel1.start() }
        justRun { sentinel2.start() }
        justRun { master1.start() }
        justRun { master2.start() }
        instance = RedisCluster(sentinels, servers)

        // when
        instance.start()

        // then
        for (s in sentinels) {
            verify { s.start() }
        }
        for (s in servers) {
            verify { s.start() }
        }
    }

    @Test
    fun isActiveShouldCheckEntireClusterIfAllActive() {
        // given
        every { sentinel1.isActive() } returns true
        every { sentinel2.isActive() } returns true
        every { master1.isActive() } returns true
        every { master2.isActive() } returns true

        val sentinels = listOf(sentinel1, sentinel2)
        val servers = listOf(master1, master2)
        instance = RedisCluster(sentinels, servers)

        // when
        instance.isActive()

        // then
        for (s in sentinels) {
            verify { s.isActive() }
        }
        for (s in servers) {
            verify { s.isActive() }
        }
    }

    private fun testPool(pool: JedisSentinelPool): Jedis {
        val jedis = pool.resource
        jedis.mset("abc", "1", "def", "2")

        // then
        assertEquals("1", jedis.mget("abc")[0])
        assertEquals("2", jedis.mget("def")[0])
        assertEquals(null, jedis.mget("xyz")[0])
        return jedis
    }

    private fun closeQuietly(vararg closeables: Closeable?) {
        for (closeable in closeables) {
            if (closeable != null) {
                try {
                    closeable.close()
                } catch (_: IOException) {
                }
            }
        }
    }

    @Test
    fun testSimpleOperationsAfterRunWithSingleMasterNoSlavesCluster() {
        // given
        val cluster = RedisCluster
            .builder()
            .withSentinelBuilder(sentinelBuilder)
            .sentinelCount(1)
            .replicationGroup("ourmaster", 0)
            .build()
        cluster.start()

        // when, then
        var pool: JedisSentinelPool? = null
        var jedis: Jedis? = null
        try {
            pool = JedisSentinelPool("ourmaster", setOf("localhost:26379"))
            jedis = testPool(pool)
        } finally {
            closeQuietly(jedis, pool)
            cluster.stop()
        }
    }

    @Test
    fun testSimpleOperationsAfterRunWithSingleMasterAndOneSlave() {
        // given
        val cluster = RedisCluster
            .builder()
            .withSentinelBuilder(sentinelBuilder)
            .sentinelCount(1)
            .replicationGroup("ourmaster", 1)
            .build()
        cluster.start()

        // when
        var pool: JedisSentinelPool? = null
        var jedis: Jedis? = null
        try {
            pool = JedisSentinelPool("ourmaster", setOf("localhost:26379"))
            jedis = testPool(pool)
        } finally {
            closeQuietly(jedis, pool)
            cluster.stop()
        }
    }

    @Test
    fun testSimpleOperationsAfterRunWithSingleMasterMultipleSlaves() {
        // given
        val cluster = RedisCluster
            .builder()
            .withSentinelBuilder(sentinelBuilder)
            .sentinelCount(1)
            .replicationGroup("ourmaster", 2)
            .build()
        cluster.start()

        // when
        var pool: JedisSentinelPool? = null
        var jedis: Jedis? = null
        try {
            pool = JedisSentinelPool("ourmaster", setOf("localhost:26379"))
            jedis = testPool(pool)
        } finally {
            closeQuietly(jedis, pool)
            cluster.stop()
        }
    }

    @Test
    @Throws(Exception::class)
    fun testSimpleOperationsAfterRunWithTwoSentinelsSingleMasterMultipleSlaves() {
        // given
        val cluster = RedisCluster
            .builder()
            .withSentinelBuilder(sentinelBuilder)
            .sentinelCount(2)
            .replicationGroup("ourmaster", 2)
            .build()
        cluster.start()

        // when
        var pool: JedisSentinelPool? = null
        var jedis: Jedis? = null
        try {
            pool = JedisSentinelPool("ourmaster", setOf("localhost:26379", "localhost:26380"))
            jedis = testPool(pool)
        } finally {
            closeQuietly(jedis, pool)
            cluster.stop()
        }
    }

    @Test
    fun testSimpleOperationsAfterRunWithTwoPredefinedSentinelsSingleMasterMultipleSlaves() {
        // given
        val sentinelPorts = mutableSetOf(26381, 26382)
        val cluster = RedisCluster
            .builder()
            .withSentinelBuilder(sentinelBuilder)
            .sentinelPorts(sentinelPorts)
            .replicationGroup("ourmaster", 2)
            .build()
        cluster.start()
        val sentinelHosts = JedisUtil.portsToJedisHosts(sentinelPorts)

        // when
        var pool: JedisSentinelPool? = null
        var jedis: Jedis? = null
        try {
            pool = JedisSentinelPool("ourmaster", sentinelHosts)
            jedis = testPool(pool)
        } finally {
            closeQuietly(jedis, pool)
            cluster.stop()
        }
    }

    @Test
    fun testSimpleOperationsAfterRunWithThreeSentinelsThreeMastersOneSlavePerMasterCluster() {
        // given
        val master1 = "master1"
        val master2 = "master2"
        val master3 = "master3"
        val cluster = RedisCluster
            .builder()
            .withSentinelBuilder(sentinelBuilder)
            .sentinelCount(3)
            .quorumSize(2)
            .replicationGroup(master1, 1)
            .replicationGroup(master2, 1)
            .replicationGroup(master3, 1)
            .build()
        cluster.start()

        // when
        var pool1: JedisSentinelPool? = null
        var pool2: JedisSentinelPool? = null
        var pool3: JedisSentinelPool? = null
        var jedis1: Jedis? = null
        var jedis2: Jedis? = null
        var jedis3: Jedis? = null
        try {
            pool1 = JedisSentinelPool(master1, setOf("localhost:26379", "localhost:26380", "localhost:26381"))
            pool2 = JedisSentinelPool(master2, setOf("localhost:26379", "localhost:26380", "localhost:26381"))
            pool3 = JedisSentinelPool(master3, setOf("localhost:26379", "localhost:26380", "localhost:26381"))
            jedis1 = testPool(pool1)
            jedis2 = testPool(pool2)
            jedis3 = testPool(pool3)
        } finally {
            closeQuietly(jedis1, pool1, jedis2, pool2, jedis3, pool3)
            cluster.stop()
        }
    }

    @Test
    fun testSimpleOperationsAfterRunWithThreeSentinelsThreeMastersOneSlavePerMasterEphemeralCluster() {
        // given
        val master1 = "master1"
        val master2 = "master2"
        val master3 = "master3"
        val cluster = RedisCluster
            .builder()
            .withSentinelBuilder(sentinelBuilder)
            .ephemeral()
            .sentinelCount(3)
            .quorumSize(2)
            .replicationGroup(master1, 1)
            .replicationGroup(master2, 1)
            .replicationGroup(master3, 1)
            .build()
        cluster.start()
        val sentinelHosts = JedisUtil.sentinelHosts(cluster)

        // when
        var pool1: JedisSentinelPool? = null
        var pool2: JedisSentinelPool? = null
        var pool3: JedisSentinelPool? = null
        var jedis1: Jedis? = null
        var jedis2: Jedis? = null
        var jedis3: Jedis? = null
        try {
            pool1 = JedisSentinelPool(master1, sentinelHosts)
            pool2 = JedisSentinelPool(master2, sentinelHosts)
            pool3 = JedisSentinelPool(master3, sentinelHosts)
            jedis1 = testPool(pool1)
            jedis2 = testPool(pool2)
            jedis3 = testPool(pool3)
        } finally {
            closeQuietly(jedis1, pool1, jedis2, pool2, jedis3, pool3)
            cluster.stop()
        }
    }
}
