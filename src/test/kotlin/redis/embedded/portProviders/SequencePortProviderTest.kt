package redis.embedded.portProviders

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SequencePortProviderTest {
    @Test
    fun nextShouldIncrementPorts() {
        // given
        val startPort = 10
        val portCount = 101
        val portProvider = SequencePortProvider(startPort)

        // when
        var max = 0
        repeat(portCount) {
            val port = portProvider.next()
            if (port > max) max = port
        }

        // then
        assertEquals(startPort + portCount - 1, max)
    }
}
