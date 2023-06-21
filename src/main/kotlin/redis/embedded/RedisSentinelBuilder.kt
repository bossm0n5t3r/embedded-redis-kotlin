package redis.embedded

import redis.embedded.exceptions.RedisBuildingException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets

@Suppress("TooManyFunctions")
class RedisSentinelBuilder {
    companion object {
        private val LINE_SEPARATOR = System.getProperty("line.separator")
        private const val CONF_FILENAME = "embedded-redis-sentinel"
        private const val MASTER_MONITOR_LINE = "sentinel monitor %s 127.0.0.1 %d %d"
        private const val DOWN_AFTER_LINE = "sentinel down-after-milliseconds %s %d"
        private const val FAILOVER_LINE = "sentinel failover-timeout %s %d"
        private const val PARALLEL_SYNCS_LINE = "sentinel parallel-syncs %s %d"
        private const val PORT_LINE = "port %d"

        private const val DEFAULT_PORT = 26379
        private const val DEFAULT_MASTER_PORT = 6379
        private const val DEFAULT_DOWN_AFTER_MILLI_SECONDS = 60000L
        private const val DEFAULT_FAILOVER_TIMEOUT = 180000L
    }

    private lateinit var executable: File
    private var redisExecProvider = RedisExecProvider.defaultProvider()
    private var bind = "127.0.0.1"
    private var port = DEFAULT_PORT
    private var masterPort = DEFAULT_MASTER_PORT
    private var masterName = "mymaster"
    private var downAfterMilliseconds = DEFAULT_DOWN_AFTER_MILLI_SECONDS
    private var failoverTimeout = DEFAULT_FAILOVER_TIMEOUT
    private var parallelSyncs = 1
    private var quorumSize = 1
    private var sentinelConf: String? = null
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

    fun masterPort(masterPort: Int) = apply {
        this.masterPort = masterPort
    }

    fun masterName(masterName: String) = apply {
        this.masterName = masterName
    }

    fun quorumSize(quorumSize: Int) = apply {
        this.quorumSize = quorumSize
    }

    fun downAfterMilliseconds(downAfterMilliseconds: Long) = apply {
        this.downAfterMilliseconds = downAfterMilliseconds
    }

    fun failoverTimeout(failoverTimeout: Long) = apply {
        this.failoverTimeout = failoverTimeout
    }

    fun parallelSyncs(parallelSyncs: Int) = apply {
        this.parallelSyncs = parallelSyncs
    }

    fun configFile(redisConf: String) = apply {
        if (redisConfigBuilder != null) {
            throw RedisBuildingException("Redis configuration is already partially build using setting(String) method!")
        }
        sentinelConf = redisConf
    }

    private fun setting(configLine: String?) = apply {
        if (sentinelConf != null) {
            throw RedisBuildingException("Redis configuration is already set using redis conf file!")
        }
        if (redisConfigBuilder == null) {
            redisConfigBuilder = StringBuilder()
        }
        redisConfigBuilder
            ?.append(configLine)
            ?.append(LINE_SEPARATOR)
    }

    fun build(): RedisSentinel {
        tryResolveConfAndExec()
        val args = buildCommandArgs()
        return RedisSentinel(port, args)
    }

    @Suppress("TooGenericExceptionCaught")
    private fun tryResolveConfAndExec() {
        executable = try {
            if (sentinelConf == null) {
                resolveSentinelConf()
            }
            redisExecProvider.get()
        } catch (e: Exception) {
            throw RedisBuildingException("Could not build sentinel instance", e)
        }
    }

    fun reset() {
        redisConfigBuilder = null
        sentinelConf = null
    }

    fun addDefaultReplicationGroup() {
        setting(String.format(MASTER_MONITOR_LINE, masterName, masterPort, quorumSize))
        setting(String.format(DOWN_AFTER_LINE, masterName, downAfterMilliseconds))
        setting(String.format(FAILOVER_LINE, masterName, failoverTimeout))
        setting(String.format(PARALLEL_SYNCS_LINE, masterName, parallelSyncs))
    }

    @Throws(IOException::class)
    private fun resolveSentinelConf() {
        if (redisConfigBuilder == null) {
            addDefaultReplicationGroup()
        }
        setting("bind $bind")
        setting(String.format(PORT_LINE, port))
        val redisConfigFile = File.createTempFile(resolveConfigName(), ".conf")
        redisConfigFile.deleteOnExit()
        FileOutputStream(redisConfigFile).use {
            it.writer(StandardCharsets.UTF_8)
                .append(redisConfigBuilder.toString())
                .flush()
        }
        sentinelConf = redisConfigFile.absolutePath
    }

    private fun resolveConfigName() = "${CONF_FILENAME}_$port"

    private fun buildCommandArgs(): List<String> {
        requireNotNull(sentinelConf)

        val args = mutableListOf<String>()
        args.add(executable.absolutePath)
        args.add(sentinelConf!!)
        args.add("--sentinel")
        args.add("--port")
        args.add(port.toString())
        return args
    }
}
