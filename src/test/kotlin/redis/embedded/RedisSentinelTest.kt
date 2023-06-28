package redis.embedded

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import redis.embedded.utils.generateRandomPort
import java.util.concurrent.TimeUnit

class RedisSentinelTest {
    private lateinit var sentinel: RedisSentinel
    private var sentinelPort = -1
    private var masterPort = -1

    @BeforeEach
    fun setup() {
        sentinelPort = generateRandomPort()
        masterPort = generateRandomPort()

        sentinel = RedisSentinel
            .builder()
            .port(sentinelPort)
            .masterPort(masterPort)
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
}
