package redis.embedded

import redis.embedded.constants.RedisConstants.LOCALHOST
import redis.embedded.constants.RedisConstants.Sentinel.DEFAULT_MASTER_PORT
import redis.embedded.constants.RedisConstants.Sentinel.DEFAULT_PORT
import redis.embedded.portProviders.EphemeralPortProvider
import redis.embedded.portProviders.PredefinedPortProvider
import redis.embedded.portProviders.SequencePortProvider

@Suppress("TooManyFunctions")
class RedisClusterBuilder {
    private var sentinelBuilder = RedisSentinelBuilder()
    private var serverBuilder = RedisServerBuilder()
    private var sentinelCount = 1
    private var quorumSize = 1
    private var sentinelPortProvider: PortProvider = SequencePortProvider(DEFAULT_PORT)
    private var replicationGroupPortProvider: PortProvider = SequencePortProvider(DEFAULT_MASTER_PORT)
    private val groups = mutableListOf<ReplicationGroup>()

    fun withSentinelBuilder(sentinelBuilder: RedisSentinelBuilder) = apply {
        this.sentinelBuilder = sentinelBuilder
    }

    fun withServerBuilder(serverBuilder: RedisServerBuilder) = apply {
        this.serverBuilder = serverBuilder
    }

    fun sentinelPorts(ports: Collection<Int>) = apply {
        sentinelPortProvider = PredefinedPortProvider(ports)
        sentinelCount = ports.size
    }

    fun serverPorts(ports: Collection<Int>) = apply {
        replicationGroupPortProvider = PredefinedPortProvider(ports)
    }

    private fun ephemeralSentinels() = apply {
        sentinelPortProvider = EphemeralPortProvider()
    }

    private fun ephemeralServers() = apply {
        replicationGroupPortProvider = EphemeralPortProvider()
    }

    fun ephemeral() = apply {
        ephemeralSentinels()
        ephemeralServers()
    }

    fun sentinelCount(sentinelCount: Int) = apply {
        this.sentinelCount = sentinelCount
    }

    fun sentinelStartingPort(startingPort: Int) = apply {
        sentinelPortProvider = SequencePortProvider(startingPort)
    }

    fun quorumSize(quorumSize: Int) = apply {
        this.quorumSize = quorumSize
    }

    fun replicationGroup(masterName: String, slaveCount: Int) = apply {
        groups.add(ReplicationGroup(masterName, slaveCount, replicationGroupPortProvider))
    }

    fun build(): RedisCluster {
        val sentinels = buildSentinels()
        val servers = buildServers()
        return RedisCluster(sentinels, servers)
    }

    private fun buildServers(): List<Redis> {
        val servers = mutableListOf<Redis>()
        for (g in groups) {
            servers.add(buildMaster(g))
            buildSlaves(servers, g)
        }
        return servers
    }

    private fun buildSlaves(servers: MutableList<Redis>, g: ReplicationGroup) {
        for (slavePort in g.slavePorts) {
            val slave = serverBuilder
                .reset()
                .port(slavePort)
                .slaveOf(LOCALHOST, g.masterPort)
                .build()
            servers.add(slave)
        }
    }

    private fun buildMaster(g: ReplicationGroup): Redis {
        serverBuilder.reset()
        return serverBuilder.port(g.masterPort).build()
    }

    private fun buildSentinels() = (0 until sentinelCount).map { buildSentinel() }

    private fun buildSentinel(): Redis {
        sentinelBuilder
            .reset()
            .port(nextSentinelPort())
        for (g in groups) {
            sentinelBuilder
                .masterName(g.masterName)
                .masterPort(g.masterPort)
                .quorumSize(quorumSize)
                .addDefaultReplicationGroup()
        }
        return sentinelBuilder.build()
    }

    private fun nextSentinelPort(): Int {
        return sentinelPortProvider.next()
    }

    private data class ReplicationGroup(
        val masterName: String,
        val masterPort: Int,
        val slavePorts: List<Int>,
    ) {
        constructor(
            masterName: String,
            slaveCount: Int,
            portProvider: PortProvider,
        ) : this(
            masterName = masterName,
            masterPort = portProvider.next(),
            slavePorts = (0 until slaveCount).map { portProvider.next() },
        )
    }
}
