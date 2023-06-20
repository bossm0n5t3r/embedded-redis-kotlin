package redis.embedded.utils

import com.google.common.io.Resources
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.file.Files

object JarUtil {
    fun extractFileFromJar(path: String): File {
        val tmpDir = Files.createTempDirectory(null).toFile()
        tmpDir.deleteOnExit()

        val file = File(tmpDir, path)
        FileUtils.copyURLToFile(Resources.getResource(path), file)
        file.deleteOnExit()

        return file
    }

    fun extractExecutableFromJar(executable: String): File {
        return extractFileFromJar(executable).apply {
            setExecutable(true)
        }
    }
}
