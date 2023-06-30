package redis.embedded

import redis.embedded.exceptions.EmbeddedRedisException
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.lang.RuntimeException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Suppress("TooManyFunctions")
abstract class AbstractRedisInstance(
    private val args: List<String> = emptyList(),
) {
    @Volatile
    protected var active = false
    private lateinit var redisProcess: Process
    private lateinit var executor: ExecutorService

    @Throws(EmbeddedRedisException::class)
    fun doStart() {
        if (active) {
            throw EmbeddedRedisException("This redis server instance is already running...")
        }
        try {
            redisProcess = createRedisProcessBuilder().start()
            installExitHook()
            logErrors()
            awaitRedisServerReady()
            active = true
        } catch (e: IOException) {
            throw EmbeddedRedisException("Failed to start Redis instance", e)
        }
    }

    private fun installExitHook() {
        Runtime.getRuntime().addShutdownHook(Thread({ doStop() }, "RedisInstanceCleaner"))
    }

    private fun logErrors() {
        val errorStream = redisProcess.errorStream
        val reader = BufferedReader(InputStreamReader(errorStream))
        val printReaderTask: Runnable = PrintReaderRunnable(reader)
        executor = Executors.newSingleThreadExecutor()
        executor.submit(printReaderTask)
    }

    @Throws(IOException::class)
    private fun awaitRedisServerReady() {
        val reader = BufferedReader(InputStreamReader(redisProcess.inputStream))
        try {
            val outputStringBuffer = StringBuffer()
            var outputLine: String?
            do {
                outputLine = reader.readLine()
                if (outputLine == null) {
                    // Something goes wrong. Stream is ended before server was activated.
                    @Suppress("TooGenericExceptionThrown")
                    throw RuntimeException(
                        "Can't start redis server. Check logs for details. Redis process log: $outputStringBuffer",
                    )
                } else {
                    outputStringBuffer.append("\n")
                    outputStringBuffer.append(outputLine)
                }
            } while (outputLine?.matches(redisReadyPattern().toRegex()) == false)
        } finally {
            try {
                reader.close()
            } catch (_: IOException) {}
        }
    }

    protected abstract fun redisReadyPattern(): String

    private fun createRedisProcessBuilder(): ProcessBuilder {
        val executable = File(args[0])
        val pb = ProcessBuilder(args)
        pb.directory(executable.parentFile)
        return pb
    }

    @Synchronized
    @Throws(EmbeddedRedisException::class)
    fun doStop() {
        if (active) {
            if (!executor.isShutdown) {
                executor.shutdown()
            }
            redisProcess.destroy()
            tryWaitFor()
            active = false
        }
    }

    private fun tryWaitFor() {
        try {
            redisProcess.waitFor()
        } catch (e: InterruptedException) {
            throw EmbeddedRedisException("Failed to stop redis instance", e)
        }
    }

    private class PrintReaderRunnable(private val reader: BufferedReader) : Runnable {
        override fun run() {
            try {
                readLines()
            } finally {
                try {
                    reader.close()
                } catch (_: IOException) {}
            }
        }

        fun readLines() {
            try {
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    println(line)
                }
            } catch (e: IOException) {
                println(e.stackTraceToString())
            }
        }
    }
}
