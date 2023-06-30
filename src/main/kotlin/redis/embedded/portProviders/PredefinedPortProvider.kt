package redis.embedded.portProviders

import redis.embedded.PortProvider
import redis.embedded.exceptions.RedisBuildingException

class PredefinedPortProvider(ports: Set<Int>) : PortProvider {
    private val current: Iterator<Int> = ports.iterator()

    @Synchronized
    override operator fun next(): Int {
        if (!current.hasNext()) {
            throw RedisBuildingException("Run out of Redis ports!")
        }
        return current.next()
    }
}
