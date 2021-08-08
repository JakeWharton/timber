package timber.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API
import com.google.auto.service.AutoService

@AutoService(value = [IssueRegistry::class])
class TimberIssueRegistry : IssueRegistry() {
  override val issues = WrongTimberUsageDetector.getIssues().asList()

  override val api = CURRENT_API

  /**
   * works with Studio 4.0 or later; see
   * [com.android.tools.lint.detector.api.describeApi]
   */
  override val minApi = 7

  override val vendor = Vendor(
    vendorName = "JakeWharton/timber",
    identifier = "com.jakewharton.timber:timber:{version}",
    feedbackUrl = "https://github.com/JakeWharton/timber/issues",
  )
}