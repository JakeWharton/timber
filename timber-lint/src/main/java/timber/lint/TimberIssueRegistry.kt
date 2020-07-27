@file:Suppress("UnstableApiUsage")

package timber.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue

class TimberIssueRegistry : IssueRegistry() {
  override val api = CURRENT_API
  override val minApi: Int = CURRENT_API
  override val issues: List<Issue> = listOf(
      *WrongTimberUsageDetector.getIssues(),
      PlantATreeDetector.ISSUE
  )
}
