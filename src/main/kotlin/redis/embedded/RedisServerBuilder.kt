package redis.embedded

import redis.embedded.constants.RedisConstants.Server.DEFAULT_REDIS_PORT
import redis.embedded.exceptions.RedisBuildingException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets

@Suppress("TooManyFunctions")
class RedisServerBuilder {
    private val lineSeparator = System.getProperty("line.separator")
    private val confFilename = "embedded-redis-server"

    private var executable: File? = null
    private var redisExecProvider: RedisExecProvider = RedisServerExecProvider.defaultProvider()
    private var bind = "127.0.0.1"
    private var port = DEFAULT_REDIS_PORT
    private var tlsPort = 0
    private var slaveOf: InetSocketAddress? = null
    private var redisConf: String? = null

    private var redisConfigBuilder: StringBuilder? = null

    fun redisExecProvider(redisExecProvider: RedisExecProvider) = apply {
        this.redisExecProvider = redisExecProvider
    }

    fun bind(bind: String) = apply {
        this.bind = bind
    }

    fun port(port: Int) = apply {
        this.port = port
    }

    fun tlsPort(tlsPort: Int) = apply {
        this.tlsPort = tlsPort
    }

    fun slaveOf(hostname: String?, port: Int) = apply {
        slaveOf = InetSocketAddress(hostname, port)
    }

    fun slaveOf(slaveOf: InetSocketAddress?) = apply {
        this.slaveOf = slaveOf
    }

    fun configFile(redisConf: String?) = apply {
        if (redisConfigBuilder != null) {
            throw RedisBuildingException("Redis configuration is already partially build using setting(String) method!")
        }
        this.redisConf = redisConf
    }

    fun setting(configLine: String?) = apply {
        if (redisConf != null) {
            throw RedisBuildingException("Redis configuration is already set using redis conf file!")
        }
        if (redisConfigBuilder == null) {
            redisConfigBuilder = StringBuilder()
        }
        redisConfigBuilder
            ?.append(configLine)
            ?.append(lineSeparator)
    }

    fun build(): RedisServer {
        setting("bind $bind")
        tryResolveConfAndExec()
        val args = buildCommandArgs()
        return RedisServer(args, port, tlsPort)
    }

    fun reset() = apply {
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

    @Suppress("TooGenericExceptionCaught")
    @Throws(IOException::class)
    private fun resolveConfAndExec() {
        if (redisConf == null && redisConfigBuilder != null) {
            val redisConfigFile = File.createTempFile(resolveConfigName(), ".conf")
            redisConfigFile.deleteOnExit()
            FileOutputStream(redisConfigFile).use {
                it.writer(StandardCharsets.UTF_8)
                    .append(redisConfigBuilder.toString())
                    .flush()
            }
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
