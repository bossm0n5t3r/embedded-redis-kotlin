package redis.embedded.utils

import java.io.FileNotFoundException
import java.net.URL
import java.nio.file.Paths

object ResourceUtil {
    private val projectDirAbsolutePath = Paths.get("").toAbsolutePath().toString()
    private val resourcesPath = Paths.get(projectDirAbsolutePath, "src/main/resources")

    fun getResource(path: String): URL {
        return Paths.get(resourcesPath.toString(), path).toFile().toURI().toURL()
            ?: throw FileNotFoundException("resource $path not found")
    }
}
