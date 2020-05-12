package org.jetbrains.completion.full.line

import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

object FilesTest {
    const val TEMP = "TEMP_FILE"

    fun fullPath(filename: String): String {
        return FilesTest::class.java.classLoader.getResource(filename)!!.path
    }

    fun readerContent(reader: BufferedReader): String {
        return StringBuilder().apply {
            reader.lineSequence().forEach {
                append(it).append('\n')
            }
        }.toString()
    }

    fun readFile(filename: String): String {
        val path = fullPath(filename)

        return readerContent(BufferedReader(FileReader(path)))
    }

    fun createTempFile(filename: String): String {
        val path = fullPath(filename)
        val newPath = FilenameUtils.getFullPath(fullPath("black.py")) + TEMP
        FileUtils.copyFile(File(path), File(newPath))
        return newPath
    }

    fun readTempFile(): String {
        return readFile(TEMP)
    }

    fun writeTempFile(code: String) {
        return File(FilenameUtils.getFullPath(fullPath("black.py")) + TEMP).writeText(code)
    }

    fun removeTempFile() {
        val path = fullPath(TEMP)

        if (File(path).exists()) {
            FileUtils.forceDelete(File(path))
        }
    }
}
