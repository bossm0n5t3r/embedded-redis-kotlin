package redis.embedded.portProviders

import redis.embedded.PortProvider
import java.util.concurrent.atomic.AtomicInteger

class SequencePortProvider(currentPort: Int) : PortProvider {
    @Suppress("MagicNumber")
    private val currentPort = AtomicInteger(26379)

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
