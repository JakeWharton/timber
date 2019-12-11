package timber.gradle

import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import timber.gradle.transform.LogRewritingTransform

fun isTimberClass(name: String) = name.startsWith("timber/")

class TimberPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.plugins.withId("com.android.application") { plugin ->
      val extension = project.extensions.create("timber", TimberExtension::class.java)
      val appPlugin = plugin as AppPlugin

      val transform = LogRewritingTransform(
          extension.variantNameFilter,
          { name -> !isTimberClass(name) && extension.classNameFilter(name) }
      )

      appPlugin.extension.registerTransform(transform)
    }
  }
}
