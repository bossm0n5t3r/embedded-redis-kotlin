package redis.embedded

abstract class AbstractRedisServerInstance : AbstractRedisInstance, IRedisServer {
    private val ports = mutableSetOf<Int>()
    private val sentinelPorts = mutableSetOf<Int>()
    private val masterPorts = mutableSetOf<Int>()

    constructor(args: List<String>) : super(args)
    constructor(port: Int) : super() {
        ports.add(port)
    }
    constructor(args: List<String>, port: Int) : super(args) {
        ports.add(port)
    }
    constructor(sentinelPort: Int, masterPort: Int) : super() {
        ports.add(sentinelPort)
        ports.add(masterPort)
        sentinelPorts.add(sentinelPort)
        masterPorts.add(masterPort)
    }
    constructor(args: List<String>, sentinelPort: Int, masterPort: Int) : super(args) {
        ports.add(sentinelPort)
        ports.add(masterPort)
        sentinelPorts.add(sentinelPort)
        masterPorts.add(masterPort)
    }

    @Synchronized
    override fun start() {
        doStart()
    }

    @Synchronized
    override fun stop() {
        doStop()
    }

    protected abstract fun redisServerReadyPattern(): String

    override fun redisReadyPattern(): String = redisServerReadyPattern()

    override fun ports(): Set<Int> = ports

    fun sentinelPorts(): Set<Int> = sentinelPorts
    fun masterPorts(): Set<Int> = masterPorts
}
