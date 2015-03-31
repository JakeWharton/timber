package timber.lint;

import com.android.tools.lint.detector.api.Issue;

import java.util.Arrays;
import java.util.List;

public final class IssueRegistry extends com.android.tools.lint.client.api.IssueRegistry {
  @Override public List<Issue> getIssues() {
    return Arrays.asList(WrongTimberUsageDetector.ISSUE_LOG,
        WrongTimberUsageDetector.ISSUE_ARG_COUNT,
        WrongTimberUsageDetector.ISSUE_ARG_TYPES,
        WrongTimberUsageDetector.ISSUE_BINARY,
        WrongTimberUsageDetector.ISSUE_FORMAT,
        WrongTimberUsageDetector.ISSUE_THROWABLE,
        WrongTimberUsageDetector.ISSUE_TAG_LENGTH);
  }
}
