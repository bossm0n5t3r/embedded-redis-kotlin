package redis.embedded

import com.google.common.io.Files
import redis.embedded.RedisServer.Companion.DEFAULT_REDIS_PORT
import redis.embedded.exceptions.RedisBuildingException
import java.io.File
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets

class RedisServerBuilder {
    private val lineSeparator = System.getProperty("line.separator")
    private val confFilename = "embedded-redis-server"

    private var executable: File? = null
    private var redisExecProvider: RedisExecProvider = RedisExecProvider.defaultProvider()
    private var bind = "127.0.0.1"
    private var port = DEFAULT_REDIS_PORT
    private var tlsPort = 0
    private var slaveOf: InetSocketAddress? = null
    private var redisConf: String? = null

    private var redisConfigBuilder: StringBuilder? = null

    fun redisExecProvider(redisExecProvider: RedisExecProvider): RedisServerBuilder {
        this.redisExecProvider = redisExecProvider
        return this
    }

    fun bind(bind: String): RedisServerBuilder {
        this.bind = bind
        return this
    }

    fun port(port: Int): RedisServerBuilder {
        this.port = port
        return this
    }

    fun tlsPort(tlsPort: Int): RedisServerBuilder {
        this.tlsPort = tlsPort
        return this
    }

    fun slaveOf(hostname: String?, port: Int): RedisServerBuilder {
        slaveOf = InetSocketAddress(hostname, port)
        return this
    }

    fun slaveOf(slaveOf: InetSocketAddress?): RedisServerBuilder {
        this.slaveOf = slaveOf
        return this
    }

    fun configFile(redisConf: String?): RedisServerBuilder {
        if (redisConfigBuilder != null) {
            throw RedisBuildingException("Redis configuration is already partially build using setting(String) method!")
        }
        this.redisConf = redisConf
        return this
    }

    fun setting(configLine: String?): RedisServerBuilder {
        if (redisConf != null) {
            throw RedisBuildingException("Redis configuration is already set using redis conf file!")
        }
        if (redisConfigBuilder == null) {
            redisConfigBuilder = StringBuilder()
        }
        redisConfigBuilder?.append(configLine)
        redisConfigBuilder?.append(lineSeparator)
        return this
    }

    fun build(): RedisServer {
        setting("bind $bind")
        tryResolveConfAndExec()
        val args = buildCommandArgs()
        return RedisServer(args, port, tlsPort)
    }

    fun reset() {
        executable = null
        redisConfigBuilder = null
        slaveOf = null
        redisConf = null
    }

    private fun tryResolveConfAndExec() {
        try {
            resolveConfAndExec()
        } catch (e: IOException) {
            throw RedisBuildingException("Could not build server instance", e)
        }
    }

    @Throws(IOException::class)
    private fun resolveConfAndExec() {
        if (redisConf == null && redisConfigBuilder != null) {
            val redisConfigFile = File.createTempFile(resolveConfigName(), ".conf")
            redisConfigFile.deleteOnExit()
            Files.asCharSink(redisConfigFile, StandardCharsets.UTF_8).write(redisConfigBuilder.toString())
            redisConf = redisConfigFile.absolutePath
        }
        executable = try {
            redisExecProvider.get()
        } catch (e: Exception) {
            throw RedisBuildingException("Failed to resolve executable", e)
        }
    }

    private fun resolveConfigName() = "${confFilename}_$port"

    private fun buildCommandArgs(): MutableList<String> {
        val args = mutableListOf<String>()

        args.add(executable!!.absolutePath)

        if (redisConf.isNullOrBlank().not()) {
            args.add(redisConf!!)
        }

        args.add("--port")
        args.add(port.toString())

        if (tlsPort > 0) {
            args.add("--tls-port")
            args.add(tlsPort.toString())
        }

        if (slaveOf != null) {
            args.add("--slaveOf")
            args.add(slaveOf!!.hostName)
            args.add(slaveOf!!.port.toString())
        }

        return args
    }
}