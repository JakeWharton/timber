package timber.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class PlantATreeDetectorTest {

  private val timber = kotlin("timber/log/Timber.kt", """
    package timber.log
    class Timber private constructor() {
      companion object Forest {
        fun e(message: String?, vararg args: Any?) {

        }
        fun w(message: String?, vararg args: Any?) {

        }
        fun i(message: String?, vararg args: Any?) {

        }
        fun d(message: String?, vararg args: Any?) {

        }
        fun v(message: String?, vararg args: Any?) {

        }
        fun plant(tree: Tree) {

        }
      }

      open class Tree {
        // A Tree Stub
      }
    }
  """).indented().within("src")

  @Test
  fun testNoTimberLoggingApisAreUsed() {
    val application = kotlin("com/example/App.kt", """
      package com.example

      import timber.log.Timber

      class App {
        fun onCreate() {
        }
      }
    """).indented().within("src")

    lint()
        .files(timber, application)
        .issues(PlantATreeDetector.ISSUE)
        .run()
        .expectClean()
  }

  @Test
  fun testWhenTimberApisAreUsed() {
    val application = kotlin("com/example/App.kt", """
      package com.example

      import timber.log.Timber

      class App {
        fun onCreate() {
          Timber.d("Log something")
        }
      }
    """).indented().within("src")

    lint()
        .files(timber, application)
        .issues(PlantATreeDetector.ISSUE)
        .run()
        .expect("""
            src/com/example/App.kt:7: Error: A Tree must be planted for at least a single variant of the application. [MustPlantATimberTree]
                Timber.d("Log something")
                ~~~~~~~~~~~~~~~~~~~~~~~~~
            1 errors, 0 warnings
        """.trimIndent())
  }

  @Test
  fun testWhenTimberApisAreUsedAndTreeIsPlanted() {
    val application = kotlin("com/example/App.kt", """
      package com.example

      import timber.log.Timber

      class App {
        fun onCreate() {
          plantTree()
          Timber.d("Log something")
        }

        private fun plantTree() {
          val tree = Timber.Tree()
          Timber.plant(tree)
        }
      }
    """).indented().within("src")

    lint()
        .files(timber, application)
        .issues(PlantATreeDetector.ISSUE)
        .run()
        .expectClean()
  }

}
