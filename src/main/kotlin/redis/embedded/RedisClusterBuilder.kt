package redis.embedded

@Suppress("TooManyFunctions")
class RedisClusterBuilder {
    private val nodePorts = mutableSetOf<Int>()
    private var serverBuilder = RedisServerBuilder()
    private var clientBuilder = RedisClientBuilder()
    private var clusterReplicas = 0

    fun withServerBuilder(serverBuilder: RedisServerBuilder) = apply {
        this.serverBuilder = serverBuilder
    }

    fun withClientBuilder(clientBuilder: RedisClientBuilder) = apply {
        this.clientBuilder = clientBuilder
    }

    fun nodePorts(ports: Set<Int>) = apply {
        nodePorts.addAll(ports)
    }

    fun clusterReplicas(clusterReplicas: Int) = apply {
        this.clusterReplicas = clusterReplicas
    }

    fun build(): RedisCluster {
        val servers = buildServers()
        val client = buildClient()
        return RedisCluster(servers, client)
    }

    private fun buildServers(): List<RedisServer> {
        return nodePorts.map { nodePort: Int ->
            serverBuilder
                .reset()
                .port(nodePort)
                .clusterEnable(true)
                .build()
        }
    }

    private fun buildClient(): RedisClient {
        return clientBuilder
            .reset()
            .ports(nodePorts)
            .clusterReplicas(clusterReplicas)
            .build()
    }
}
