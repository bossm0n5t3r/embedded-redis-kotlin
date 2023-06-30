package redis.embedded

import redis.embedded.constants.RedisConstants.Server.DEFAULT_REDIS_HOST
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
    private var bind = DEFAULT_REDIS_HOST
    private var port = DEFAULT_REDIS_PORT

    private var clusterEnable = false
    private var replicaOf: InetSocketAddress? = null
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

    fun clusterEnable(enable: Boolean) = apply {
        this.clusterEnable = enable
    }

    fun replicaOf(port: Int) = apply {
        replicaOf = InetSocketAddress(DEFAULT_REDIS_HOST, port)
    }

    fun replicaOf(slaveOf: InetSocketAddress?) = apply {
        this.replicaOf = slaveOf
    }

    fun configFile(redisConf: String?) = apply {
        if (redisConfigBuilder != null) {
            throw RedisBuildingException("Redis configuration is already partially build using setting(String) method!")
        }
        this.redisConf = redisConf
    }

    private fun setting(configLine: String?) = apply {
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
        return RedisServer(args, port)
    }

    fun reset() = apply {
        this.executable = null
        this.redisConfigBuilder = null
        this.replicaOf = null
        this.redisConf = null
        this.clusterEnable = false
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

        if (replicaOf != null) {
            args.add("--replicaof")
            args.add(replicaOf!!.hostName)
            args.add(replicaOf!!.port.toString())
        }

        if (clusterEnable) {
            args.add("--cluster-enabled")
            args.add("yes")
        }

        args.add("--loglevel")
        args.add("debug")

        args.add("--daemonize")
        args.add("no")

        args.add("--protected-mode")
        args.add("no")

        return args
    }
}
