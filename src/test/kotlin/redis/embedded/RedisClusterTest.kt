package redis.embedded

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.util.concurrent.TimeUnit
import kotlin.test.assertTrue

class RedisClusterTest {
    private val redisServer1 = mockk<RedisServer>()
    private val redisServer2 = mockk<RedisServer>()
    private val redisServer3 = mockk<RedisServer>()

    private val redisClient = mockk<RedisClient>()

    private lateinit var redisCluster: RedisCluster

    @BeforeEach
    fun setup() {
        clearAllMocks()
    }

    @Test
    @Timeout(value = 3000, unit = TimeUnit.MILLISECONDS)
    fun testSimpleRun() {
        justRun {
            redisServer1.start()
            redisServer2.start()
            redisServer3.start()

            redisClient.run()

            redisServer1.stop()
            redisServer2.stop()
            redisServer3.stop()
        }
        val redisServers = listOf(redisServer1, redisServer2, redisServer3)
        redisCluster = RedisCluster(redisServers, redisClient)
        redisCluster.start()

        TimeUnit.SECONDS.sleep(1)

        redisCluster.stop()
    }

    @Test
    fun shouldAllowSubsequentRuns() {
        justRun {
            redisServer1.start()
            redisServer2.start()
            redisServer3.start()

            redisClient.run()

            redisServer1.stop()
            redisServer2.stop()
            redisServer3.stop()
        }
        val redisServers = listOf(redisServer1, redisServer2, redisServer3)
        redisCluster = RedisCluster(redisServers, redisClient)

        redisCluster.start()
        redisCluster.stop()

        redisCluster.start()
        redisCluster.stop()

        redisCluster.start()
        redisCluster.stop()
    }

    @Test
    fun stopShouldStopEntireCluster() {
        // given
        justRun {
            redisServer1.start()
            redisServer2.start()
            redisServer3.start()

            redisClient.run()

            redisServer1.stop()
            redisServer2.stop()
            redisServer3.stop()
        }
        val redisServers = listOf(redisServer1, redisServer2, redisServer3)
        redisCluster = RedisCluster(redisServers, redisClient)

        // when
        redisCluster.stop()

        // then
        for (s in redisServers) {
            verify { s.stop() }
        }
    }

    @Test
    fun startShouldStartEntireCluster() {
        // given
        justRun {
            redisServer1.start()
            redisServer2.start()
            redisServer3.start()

            redisClient.run()

            redisServer1.stop()
            redisServer2.stop()
            redisServer3.stop()
        }
        val redisServers = listOf(redisServer1, redisServer2, redisServer3)
        redisCluster = RedisCluster(redisServers, redisClient)

        // when
        redisCluster.start()

        // then
        for (s in redisServers) {
            verify { s.start() }
        }
    }

    @Test
    fun isActiveShouldCheckEntireClusterIfAllActive() {
        // given
        every { redisServer1.isActive() } returns true
        every { redisServer2.isActive() } returns true
        every { redisServer3.isActive() } returns true

        val redisServers = listOf(redisServer1, redisServer2, redisServer3)
        redisCluster = RedisCluster(redisServers, redisClient)

        // when
        assertTrue { redisCluster.isActive() }

        // then
        for (s in redisServers) {
            verify { s.isActive() }
        }
    }
}
