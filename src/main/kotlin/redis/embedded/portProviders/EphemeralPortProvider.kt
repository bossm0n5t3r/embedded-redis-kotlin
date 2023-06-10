package redis.embedded.portProviders

import redis.embedded.PortProvider
import redis.embedded.exceptions.RedisBuildingException
import java.io.IOException
import java.net.ServerSocket

class EphemeralPortProvider : PortProvider {
    override operator fun next(): Int {
        return try {
            val socket = ServerSocket(0).apply {
                reuseAddress = false
            }
            val port = socket.localPort
            socket.close()
            port
        } catch (e: IOException) {
            // should not ever happen
            throw RedisBuildingException("Could not provide ephemeral port", e)
        }
    }
}
