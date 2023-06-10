package redis.embedded.portProviders

import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class EphemeralPortProviderTest {
    @Test
    fun nextShouldGiveNextFreeEphemeralPort() {
        // given
        val portCount = 20
        val provider = EphemeralPortProvider()

        // when
        val ports = (0 until portCount).map { provider.next() }

        // then
        println(ports)
        assertTrue { ports.size == portCount }
    }
}
