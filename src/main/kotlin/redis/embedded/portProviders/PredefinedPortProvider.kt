package redis.embedded.portProviders

import redis.embedded.PortProvider
import redis.embedded.exceptions.RedisBuildingException
import java.util.LinkedList

class PredefinedPortProvider(ports: Collection<Int>) : PortProvider {
    private val current: Iterator<Int> = LinkedList(ports).iterator()

    @Synchronized
    override operator fun next(): Int {
        if (!current.hasNext()) {
            throw RedisBuildingException("Run out of Redis ports!")
        }
        return current.next()
    }
}
