package timber.lint;

import com.android.tools.lint.detector.api.Issue;

import java.util.Arrays;
import java.util.List;

public class IssueRegistry extends com.android.tools.lint.client.api.IssueRegistry {

  @Override
  public List<Issue> getIssues() {
    return Arrays.asList(CallToLogNotTimberDetector.ISSUE);
  }
}
