package timber.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest.java
import com.android.tools.lint.checks.infrastructure.LintDetectorTest.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class PlantATreeDetectorTest {

  private val timber = kotlin("timber/log/Timber.kt", """
    package timber.log
    class Timber private constructor() {
      abstract class Tree {
        
      }
      companion object Forest: Tree() {
        @JvmStatic
        fun e(message: String?, vararg args: Any?) {

        }
        @JvmStatic
        fun w(message: String?, vararg args: Any?) {

        }
        @JvmStatic
        fun i(message: String?, vararg args: Any?) {

        }
        @JvmStatic
        fun d(message: String?, vararg args: Any?) {

        }
        @JvmStatic
        fun v(message: String?, vararg args: Any?) {

        }
        @JvmStatic
        fun plant(tree: Tree) {

        }
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

  @Test
  fun testWhenTimberApisAreUsedAndTreeIsPlanted_java() {
    val application = java("com/example/App.java", """
      package com.example;

      import timber.log.Timber;

      class App {
        void onCreate() {
          plantTree();
          Timber.d("Log something");
        }

        void plantTree() {
          val tree = Timber.Tree();
          Timber.plant(tree);
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
