package timber.gradle.transform

import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Status
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class LogRewritingTransform(
    private val variantNameFilter: (String) -> Boolean,
    private val classNameFilter: (String) -> Boolean
) : Transform() {
  override fun getName() = "logcat-to-timber"
  override fun getInputTypes() = setOf(QualifiedContent.DefaultContentType.CLASSES)
  override fun isIncremental() = true
  override fun getScopes() = mutableSetOf(QualifiedContent.Scope.EXTERNAL_LIBRARIES)

  override fun transform(invoke: TransformInvocation) {
    super.transform(invoke)

    val incremental = invoke.isIncremental
    val rewrite = variantNameFilter(invoke.context.variantName)

    if (!incremental) {
      invoke.outputProvider.deleteAll()
    }

    for (input in invoke.inputs) {
      for (dirInput in input.directoryInputs) {

        val output = invoke.outputProvider.getContentLocation(
            "timbered-${dirInput.name}",
            dirInput.contentTypes, dirInput.scopes,
            Format.DIRECTORY)

        val files = if (incremental) {
          dirInput.changedFiles
        } else {
          dirInput.file.walk().filter { it.isFile }.map { it to Status.ADDED }.toMap()
        }

        files.forEach { (file, status) ->
          val relative = file.toRelativeString(dirInput.file)
          val target = output.resolve(relative)
          when (status!!) {
            Status.ADDED, Status.CHANGED -> {
              processFile(file, target, rewrite)
            }
            Status.REMOVED -> {
              target.delete()
            }
            Status.NOTCHANGED -> { /* do nothing */  }
          }
        }
      }
      for (jarInput in input.jarInputs) {
        val target = invoke.outputProvider.getContentLocation("timbered-${jarInput.name}", jarInput.contentTypes, jarInput.scopes, Format.JAR)
        val status = if (incremental) jarInput.status!! else Status.ADDED
        when (status) {
          Status.ADDED, Status.CHANGED -> {
            processJar(jarInput.file, target, rewrite)
          }
          Status.REMOVED -> {
            target.delete()
          }
          Status.NOTCHANGED -> { /* do nothing */ }
        }
      }
    }
  }

  private fun processFile(input: File, output: File, rewrite: Boolean) {
    output.parentFile.mkdirs()
    if (rewrite) {
      input.inputStream().use { inputStream ->
        output.writeBytes(transformClass(inputStream.readBytes()))
      }
    } else {
      input.copyTo(output, overwrite = true)
    }
  }

  private fun processJar(input: File, output: File, rewrite: Boolean) {
    output.parentFile.mkdirs()
    if (rewrite) {
      ZipInputStream(input.inputStream().buffered()).use { zipIn  ->
        ZipOutputStream(output.outputStream().buffered()).use { zipOut ->
          while (true) {
            val entry = zipIn.nextEntry ?: break
            val outEntry = ZipEntry(entry.name)
            val inputBytes = zipIn.readBytes()
            val processedBytes = if (entry.name.endsWith(".class")) {
               transformClass(inputBytes)
            } else {
              inputBytes
            }
            outEntry.size = processedBytes.size.toLong()
            zipOut.putNextEntry(outEntry)
            zipOut.write(processedBytes)
          }
        }
      }
    } else {
      input.copyTo(output, overwrite = true)
    }
  }

  private fun transformClass(input: ByteArray): ByteArray {
    val cr = ClassReader(input)
    if (classNameFilter(cr.className)) {
      val cw = ClassWriter(0)
      cr.accept(ClassRewriter(cw), ClassReader.EXPAND_FRAMES)
      return cw.toByteArray()
    } else {
      return input
    }
  }
}