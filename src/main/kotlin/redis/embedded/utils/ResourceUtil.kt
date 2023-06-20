package redis.embedded.utils

import java.io.FileNotFoundException
import java.net.URL

object ResourceUtil {
    fun getResource(path: String): URL {
        return this.javaClass.classLoader.getResource(path)
            ?: throw FileNotFoundException("resource $path not found")
    }
}
