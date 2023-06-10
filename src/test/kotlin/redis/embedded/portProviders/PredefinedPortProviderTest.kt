package redis.embedded.portProviders

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import redis.embedded.exceptions.RedisBuildingException
import kotlin.random.Random
import kotlin.test.assertEquals

class PredefinedPortProviderTest {
    @Test
    fun nextShouldGiveNextPortFromAssignedList() {
        val portSize = Random.nextInt(10, 20)
        val ports = (0 until portSize).map {
            Random.nextInt(0, UShort.MAX_VALUE.toInt() + 1)
        }
        val portProvider = PredefinedPortProvider(ports)

        val returnedPorts = (0 until portSize).map { portProvider.next() }

        assertEquals(ports, returnedPorts)
    }

    @Test
    fun nextShouldThrowExceptionWhenRunOutsOfPorts() {
        val portSize = Random.nextInt(10, 20)
        val ports = (0 until portSize).map {
            Random.nextInt(0, UShort.MAX_VALUE.toInt() + 1)
        }
        val portProvider = PredefinedPortProvider(ports)

        repeat(portSize) { portProvider.next() }

        assertThrows<RedisBuildingException> { portProvider.next() }
    }
}
