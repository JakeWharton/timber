package timber.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue
import com.google.auto.service.AutoService

@Suppress("UnstableApiUsage", "unused")
@AutoService(value = [IssueRegistry::class])
class TimberIssueRegistry : IssueRegistry() {
  override val issues: List<Issue>
    get() = WrongTimberUsageDetector.issues.asList()

  override val api: Int
    get() = CURRENT_API

  /**
   * works with Studio 4.0 or later; see
   * [com.android.tools.lint.detector.api.describeApi]
   */
  override val minApi: Int
    get() = 7

  override val vendor = Vendor(
    vendorName = "JakeWharton/timber",
    identifier = "com.jakewharton.timber:timber:{version}",
    feedbackUrl = "https://github.com/JakeWharton/timber/issues",
  )
}