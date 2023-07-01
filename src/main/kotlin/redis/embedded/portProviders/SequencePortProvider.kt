package redis.embedded.portProviders

import redis.embedded.PortProvider
import redis.embedded.constants.RedisConstants.Sentinel
import java.util.concurrent.atomic.AtomicInteger

class SequencePortProvider(currentPort: Int) : PortProvider {
    @Suppress("MagicNumber")
    private val currentPort = AtomicInteger(Sentinel.DEFAULT_PORT)

    init {
        this.currentPort.set(currentPort)
    }

    fun setCurrentPort(port: Int) {
        currentPort.set(port)
    }

    override operator fun next(): Int {
        return currentPort.getAndIncrement()
    }
}
