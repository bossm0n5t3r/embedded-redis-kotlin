package redis.embedded.utils

import redis.embedded.utils.ResourceUtil.getResource
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

object JarUtil {
    private fun extractFileFromJar(path: String): File {
        val tmpDir = Files.createTempDirectory(null).toFile()
        tmpDir.deleteOnExit()

        val file = File(tmpDir, path)
        Files.copy(Path.of(getResource(path).toURI()), file.toPath(), StandardCopyOption.REPLACE_EXISTING)
        file.deleteOnExit()

        return file
    }

    fun extractExecutableFromJar(executable: String): File {
        return extractFileFromJar(executable).apply {
            setExecutable(true)
        }
    }
}
