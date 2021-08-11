@file:Suppress("UnstableApiUsage")

package timber.lint

import com.android.tools.lint.detector.api.*
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression
import java.util.*

/**
 * A [Detector] which makes sure than anytime Timer APIs are used, there is at-least a single tree
 * planted.
 */
class PlantATreeDetector : Detector(), SourceCodeScanner {
  companion object {
    val ISSUE = Issue.create(
      id = "MustPlantATimberTree",
      briefDescription = "A Timber tree needs to be planted",
      explanation = """
                When using Timber's logging APIs, a `Tree` must be planted on at least a single \
                variant of the app.
            """,
      androidSpecific = true,
      category = Category.CORRECTNESS,
      severity = Severity.ERROR,
      implementation = Implementation(
        PlantATreeDetector::class.java,
        EnumSet.of(Scope.JAVA_FILE)
      )
    )

    // Methods on the companion object are marked as @JvmStatic
    // Therefore we need to check whether they can be resolved to either Timber or Forest.
    private const val TIMBER = "timber.log.Timber"
    private const val FOREST = "timber.log.Timber.Forest"
  }

  // Do we need to check if a Tree is planted
  private var checkForPlantedTrees = false
  private var hasPlantedTree = false
  private var location: Location? = null

  override fun getApplicableMethodNames() = listOf("v", "d", "i", "w", "e", "wtf", "plant")

  override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
    val methodName = method.name
    when (context.driver.phase) {
      1 -> {
        if (methodName.matches(Regex("(v|d|i|w|e|wtf)"))
          && context.evaluator.isMemberInClass(method, FOREST)
        ) {
          if (!checkForPlantedTrees) {
            location = context.getLocation(node)
            checkForPlantedTrees = true
            // Request a second scan with the same scope
            context.driver.requestRepeat(this, null)
          }
        }
      }
      else -> {
        if (methodName.matches(Regex("plant")) &&
          (context.evaluator.isMemberInClass(method, TIMBER) ||
              context.evaluator.isMemberInClass(method, FOREST))
        ) {
          hasPlantedTree = true
        }
      }
    }
  }

  override fun afterCheckRootProject(context: Context) {
    if (checkForPlantedTrees && !hasPlantedTree && context.driver.phase > 1) {
      context.report(
        issue = ISSUE,
        location = location ?: Location.create(context.file),
        message = "A `Tree` must be planted for at least a single variant of the application."
      )
    }
  }
}
