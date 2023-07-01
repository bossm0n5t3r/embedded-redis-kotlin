package redis.embedded

import redis.embedded.constants.RedisConstants.Server.DEFAULT_REDIS_HOST
import redis.embedded.exceptions.RedisBuildingException
import java.io.File
import java.io.IOException

class RedisClientBuilder {
    private val ports = mutableSetOf<Int>()
    private var executable: File? = null
    private var redisExecProvider: RedisExecProvider = RedisCliExecProvider.defaultProvider()
    private var clusterReplicas = 0

    fun redisExecProvider(redisExecProvider: RedisExecProvider) = apply {
        this.redisExecProvider = redisExecProvider
    }

    fun ports(ports: Set<Int>) = apply {
        this.ports.addAll(ports)
    }

    fun clusterReplicas(clusterReplicas: Int) = apply {
        this.clusterReplicas = clusterReplicas
    }

    fun build(): RedisClient {
        tryResolveConfAndExec()
        val args = buildCommandArgs()
        return RedisClient(args)
    }

    fun reset() = apply {
        ports.clear()
        executable = null
        clusterReplicas = 0
    }

    private fun tryResolveConfAndExec() {
        try {
            resolveConfAndExec()
        } catch (e: IOException) {
            throw RedisBuildingException("Could not build client instance. exception: ${e.message}", e)
        }
    }

    @Throws(IOException::class)
    private fun resolveConfAndExec() {
        executable = try {
            redisExecProvider.get()
        } catch (e: Exception) {
            throw RedisBuildingException("Failed to resolve executable. exception: ${e.message}", e)
        }
    }

    private fun buildCommandArgs(): List<String> {
        val args = mutableListOf<String>()
        args.add(executable!!.absolutePath)

        args.add("--cluster")
        args.add("create")

        ports.forEach { port: Int ->
            args.add("$DEFAULT_REDIS_HOST:$port")
        }

        if (clusterReplicas > 0) {
            args.add("--cluster-replicas")
            args.add(clusterReplicas.toString())
        }

        args.add("--cluster-yes")

        return args
    }
}
