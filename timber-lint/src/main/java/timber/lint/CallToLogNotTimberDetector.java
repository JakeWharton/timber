package timber.lint;

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;

import java.util.Arrays;
import java.util.List;

import lombok.ast.AstVisitor;
import lombok.ast.MethodInvocation;

public class CallToLogNotTimberDetector extends Detector implements Detector.JavaScanner {

  @Override
  public List<String> getApplicableMethodNames() {
    return Arrays.asList("d", "i", "w", "e");
  }

  @Override
  public void visitMethod(JavaContext context, AstVisitor visitor, MethodInvocation node) {
    if (node.toString().startsWith("Log.")) {
      context.report(ISSUE, node, context.getLocation(node),
          "Using 'Log' instead of 'Timber'", null);
    }
  }

  public static final Issue ISSUE = Issue.create(
      "LogNotTimber",
      "Logging call to Log instead of Timber",
      "This check looks through all the logging calls for instances where the Android Log " +
          "class was used instead of Timber.",
      "Since Timber is included in the project, it is likely that calls to Log should " +
          "instead be going to Timber.",
      Category.CORRECTNESS, 5, Severity.WARNING,
      new Implementation(CallToLogNotTimberDetector.class, Scope.JAVA_FILE_SCOPE));
}
