package timber.lint;

import com.android.tools.lint.checks.infrastructure.LintDetectorTest;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Issue;
import java.util.Arrays;
import java.util.List;
import org.intellij.lang.annotations.Language;

import static org.fest.assertions.api.Assertions.assertThat;

public class WrongTimberUsageDetectorTest extends LintDetectorTest {
  private static final String NO_WARNINGS = "No warnings.";
  private final TestFile timberStub = java(""
      + "package timber.log;\n"
      + "public class Timber {\n"
      + "  public static void d(String s) { TREE_OF_SOULS.d(s); }\n"
      + "  public static Tree tag(String s) { return new Tree(); }\n"
      + "  public static class Tree {\n"
      + "    public void d(String s) {}\n"
      + "  }\n"
      + "  private static final Tree TREE_OF_SOULS = new Tree();\n"
      + "}");

  public void testUsingAndroidLog() throws Exception {
    @Language("JAVA") String source = ""
        + "package foo;\n"
        + "import android.util.Log;\n"
        + "public class Example {\n"
        + "  public void log() {\n"
        + "    Log.d(\"TAG\", \"msg\");\n"
        + "  }\n"
        + "}";
    assertThat(lintProject(java(source))).isEqualTo("src/foo/Example.java:5: "
        + "Warning: Using 'Log' instead of 'Timber' [LogNotTimber]\n"
        + "    Log.d(\"TAG\", \"msg\");\n"
        + "    ~~~~~\n"
        + "0 errors, 1 warnings\n");
  }

  public void testInnerStringFormatInNestedMethods() throws Exception {
    @Language("JAVA") String source = ""
        + "package foo;\n"
        + "import timber.log.Timber;\n"
        + "public class Example {\n"
        + "  public void log() {\n"
        + "     Timber.d(id(String.format(\"%s\", \"arg1\")));\n"
        + "  }\n"
        + "  private String id(String s) { return s; }\n"
        + "}";
    assertThat(lintProject(java(source), timberStub)).isEqualTo("src/foo/Example.java:5: "
        + "Warning: Using 'String#format' inside of 'Timber' [StringFormatInTimber]\n"
        + "     Timber.d(id(String.format(\"%s\", \"arg1\")));\n"
        + "                 ~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
        + "0 errors, 1 warnings\n");
  }

  public void testInnerStringFormatInNestedAssignment() throws Exception {
    @Language("JAVA") String source = ""
        + "package foo;\n"
        + "import timber.log.Timber;\n"
        + "public class Example {\n"
        + "  public void log() {\n"
        + "    String msg = null;\n"
        + "    Timber.d(msg = String.format(\"msg\"));\n"
        + "  }\n"
        + "}";
    assertThat(lintProject(java(source), timberStub)).isEqualTo("src/foo/Example.java:6: "
        + "Warning: Using 'String#format' inside of 'Timber' [StringFormatInTimber]\n"
        + "    Timber.d(msg = String.format(\"msg\"));\n"
        + "                   ~~~~~~~~~~~~~~~~~~~~\n"
        + "0 errors, 1 warnings\n");
  }

  public void testValidStringFormatInCodeBlock() throws Exception {
    @Language("JAVA") String source = ""
        + "package foo;\n"
        + "import timber.log.Timber;\n"
        + "public class Example {\n"
        + "  public void log() {\n"
        + "    for(;;) {\n"
        + "      String name = String.format(\"msg\");\n"
        + "    }\n"
        + "  }\n"
        + "}";
    assertThat(lintProject(java(source), timberStub)).isEqualTo(NO_WARNINGS);
  }

  public void testThrowableNotAtBeginning() throws Exception {
    @Language("JAVA") String source = ""
        + "package foo;\n"
        + "import timber.log.Timber;\n"
        + "public class Example {\n"
        + "  public void log() {\n"
        + "     Exception e = new Exception();\n"
        + "     Timber.d(\"%s\", e);\n"
        + "  }\n"
        + "}";
    assertThat(lintProject(java(source), timberStub)).isEqualTo("src/foo/Example.java:6: "
        + "Warning: Throwable should be first argument [ThrowableNotAtBeginning]\n"
        + "     Timber.d(\"%s\", e);\n"
        + "     ~~~~~~~~~~~~~~~~~\n"
        + "0 errors, 1 warnings\n");
  }

  public void testBinaryOperation() throws Exception {
    @Language("JAVA") String source = ""
        + "package foo;\n"
        + "import timber.log.Timber;\n"
        + "public class Example {\n"
        + "  public void log() {\n"
        + "     String s = \"world!\";\n"
        + "     Timber.d(\"Hello, \" + s);\n"
        + "  }\n"
        + "}";
    assertThat(lintProject(java(source), timberStub)).isEqualTo("src/foo/Example.java:6: "
        + "Warning: Replace String concatenation with Timber's string formatting [BinaryOperationInTimber]\n"
        + "     Timber.d(\"Hello, \" + s);\n"
        + "              ~~~~~~~~~~~~~\n"
        + "0 errors, 1 warnings\n");
  }

  public void testBinaryOperationInsideTernary() throws Exception {
    @Language("JAVA") String source = ""
        + "package foo;\n"
        + "import timber.log.Timber;\n"
        + "public class Example {\n"
        + "  public void log() {\n"
        + "     String s = \"world!\";\n"
        + "     Timber.d(true ? \"Hello, \" + s : \"Bye\");\n"
        + "  }\n"
        + "}";
    assertThat(lintProject(java(source), timberStub)).isEqualTo("src/foo/Example.java:6: "
        + "Warning: Replace String concatenation with Timber's string formatting [BinaryOperationInTimber]\n"
        + "     Timber.d(true ? \"Hello, \" + s : \"Bye\");\n"
        + "                     ~~~~~~~~~~~~~\n"
        + "0 errors, 1 warnings\n");
  }

  public void testTooManyFormatArgs() throws Exception {
    @Language("JAVA") String source = ""
        + "package foo;\n"
        + "import timber.log.Timber;\n"
        + "public class Example {\n"
        + "  public void log() {\n"
        + "     Timber.d(\"%s %s\", \"arg1\");\n"
        + "  }\n"
        + "}";
    assertThat(lintProject(java(source), timberStub)).isEqualTo("src/foo/Example.java:5: "
        + "Error: Wrong argument count, format string %s %s requires 2 but format call supplies 1 [TimberArgCount]\n"
        + "     Timber.d(\"%s %s\", \"arg1\");\n"
        + "     ~~~~~~~~~~~~~~~~~~~~~~~~~\n"
        + "1 errors, 0 warnings\n");
  }

  public void testTooManyArgs() throws Exception {
    @Language("JAVA") String source = ""
        + "package foo;\n"
        + "import timber.log.Timber;\n"
        + "public class Example {\n"
        + "  public void log() {\n"
        + "     Timber.d(\"%s\", \"arg1\", \"arg2\");\n"
        + "  }\n"
        + "}";
    assertThat(lintProject(java(source), timberStub)).isEqualTo("src/foo/Example.java:5: "
        + "Error: Wrong argument count, format string %s requires 1 but format call supplies 2 [TimberArgCount]\n"
        + "     Timber.d(\"%s\", \"arg1\", \"arg2\");\n"
        + "     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
        + "1 errors, 0 warnings\n");
  }

  public void testWrongArgTypes() throws Exception {
    @Language("JAVA") String source = ""
        + "package foo;\n"
        + "import timber.log.Timber;\n"
        + "public class Example {\n"
        + "  public void log() {\n"
        + "     Timber.d(\"%d\", \"arg1\");\n"
        + "  }\n"
        + "}";
    assertThat(lintProject(java(source), timberStub)).isEqualTo("src/foo/Example.java:5: "
        + "Error: Wrong argument type for formatting argument '#1' in %d: conversion is 'd', received String (argument #2 in method call) [TimberArgTypes]\n"
        + "     Timber.d(\"%d\", \"arg1\");\n"
        + "                    ~~~~~~\n"
        + "1 errors, 0 warnings\n");
  }

  public void testTagTooLongLiteralOnly() throws Exception {
    @Language("JAVA") String source = ""
        + "package foo;\n"
        + "import timber.log.Timber;\n"
        + "public class Example {\n"
        + "  public void log() {\n"
        + "     Timber.tag(\"abcdefghijklmnopqrstuvwx\");\n"
        + "  }\n"
        + "}";
    assertThat(lintProject(java(source), timberStub)).isEqualTo("src/foo/Example.java:5: "
        + "Error: The logging tag can be at most 23 characters, was 24 (abcdefghijklmnopqrstuvwx) [TimberTagLength]\n"
        + "     Timber.tag(\"abcdefghijklmnopqrstuvwx\");\n"
        + "                ~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
        + "1 errors, 0 warnings\n");
  }

  public void testTagTooLongLiteralPlusField() throws Exception {
    @Language("JAVA") String source = ""
        + "package foo;\n"
        + "import timber.log.Timber;\n"
        + "public class Example {\n"
        + "  private final String field = \"x\";"
        + "  public void log() {\n"
        + "     Timber.tag(\"abcdefghijklmnopqrstuvw\" + field);\n"
        + "  }\n"
        + "}";
    assertThat(lintProject(java(source), timberStub)).isEqualTo("src/foo/Example.java:5: "
        + "Error: The logging tag can be at most 23 characters, was 24 (abcdefghijklmnopqrstuvwx) [TimberTagLength]\n"
        + "     Timber.tag(\"abcdefghijklmnopqrstuvw\" + field);\n"
        + "                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
        + "1 errors, 0 warnings\n");
  }

  public void testTooManyFormatArgsInTag() throws Exception {
    @Language("JAVA") String source = ""
        + "package foo;\n"
        + "import timber.log.Timber;\n"
        + "public class Example {\n"
        + "  public void log() {\n"
        + "     Timber.tag(\"tag\").d(\"%s %s\", \"arg1\");\n"
        + "  }\n"
        + "}";
    assertThat(lintProject(java(source), timberStub)).isEqualTo("src/foo/Example.java:5: "
        + "Error: Wrong argument count, format string %s %s requires 2 but format call supplies 1 [TimberArgCount]\n"
        + "     Timber.tag(\"tag\").d(\"%s %s\", \"arg1\");\n"
        + "     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
        + "1 errors, 0 warnings\n");
  }

  public void testTooManyArgsInTag() throws Exception {
    @Language("JAVA") String source = ""
        + "package foo;\n"
        + "import timber.log.Timber;\n"
        + "public class Example {\n"
        + "  public void log() {\n"
        + "     Timber.tag(\"tag\").d(\"%s\", \"arg1\", \"arg2\");\n"
        + "  }\n"
        + "}";
    assertThat(lintProject(java(source), timberStub)).isEqualTo("src/foo/Example.java:5: "
        + "Error: Wrong argument count, format string %s requires 1 but format call supplies 2 [TimberArgCount]\n"
        + "     Timber.tag(\"tag\").d(\"%s\", \"arg1\", \"arg2\");\n"
        + "     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
        + "1 errors, 0 warnings\n");
  }

  public void testWrongArgTypesInTag() throws Exception {
    @Language("JAVA") String source = ""
        + "package foo;\n"
        + "import timber.log.Timber;\n"
        + "public class Example {\n"
        + "  public void log() {\n"
        + "     Timber.tag(\"tag\").d(\"%d\", \"arg1\");\n"
        + "  }\n"
        + "}";
    assertThat(lintProject(java(source), timberStub)).isEqualTo("src/foo/Example.java:5: "
        + "Error: Wrong argument type for formatting argument '#1' in %d: conversion is 'd', received String (argument #2 in method call) [TimberArgTypes]\n"
        + "     Timber.tag(\"tag\").d(\"%d\", \"arg1\");\n"
        + "                               ~~~~~~\n"
        + "1 errors, 0 warnings\n");
  }

  public void testExceptionLoggingUsingMessage() throws Exception {
    @Language("JAVA") String source = ""
        + "package foo;\n"
        + "import timber.log.Timber;\n"
        + "public class Example {\n"
        + "  public void log() {\n"
        + "     Exception e = new Exception();\n"
        + "     Timber.d(e, e.getMessage());\n"
        + "  }\n"
        + "}";
    assertThat(lintProject(java(source), timberStub)).isEqualTo("src/foo/Example.java:6: "
        + "Warning: Explicitly logging exception message is redundant [TimberExceptionLogging]\n"
        + "     Timber.d(e, e.getMessage());\n"
        + "                 ~~~~~~~~~~~~~~\n"
        + "0 errors, 1 warnings\n");
  }

  public void testExceptionLoggingUsingVariable() throws Exception {
    @Language("JAVA")
    String source2= ""
        + "package foo;\n"
        + "import timber.log.Timber;\n"
        + "public class Example {\n"
        + "  public void log() {\n"
        + "     String msg = \"Hello\";\n"
        + "     Exception e = new Exception();\n"
        + "     Timber.d(e, msg);\n"
        + "  }\n"
        + "}";
    assertThat(lintProject(java(source2), timberStub)).isEqualTo(NO_WARNINGS);
  }

  public void testExceptionLoggingUsingNull() throws Exception {
    @Language("JAVA")
    String source2= ""
        + "package foo;\n"
        + "import timber.log.Timber;\n"
        + "public class Example {\n"
        + "  public void log() {\n"
        + "     Exception e = new Exception();\n"
        + "     Timber.d(e, null);\n"
        + "  }\n"
        + "}";
    assertThat(lintProject(java(source2), timberStub)).isEqualTo("src/foo/Example.java:6: "
        + "Warning: Use single-argument log method instead of null/empty message [TimberExceptionLogging]\n"
        + "     Timber.d(e, null);\n"
        + "                 ~~~~\n"
        + "0 errors, 1 warnings\n");
  }

  public void testExceptionLoggingUsingEmpty() throws Exception {
    @Language("JAVA")
    String source2= ""
        + "package foo;\n"
        + "import timber.log.Timber;\n"
        + "public class Example {\n"
        + "  public void log() {\n"
        + "     Exception e = new Exception();\n"
        + "     Timber.d(e, \"\");\n"
        + "  }\n"
        + "}";
    assertThat(lintProject(java(source2), timberStub)).isEqualTo("src/foo/Example.java:6: "
        + "Warning: Use single-argument log method instead of null/empty message [TimberExceptionLogging]\n"
        + "     Timber.d(e, \"\");\n"
        + "                 ~~\n"
        + "0 errors, 1 warnings\n");
  }

  public void testExceptionLoggingUsingEmptyStringMessage() throws Exception {
    @Language("JAVA") String source = ""
        + "package foo;\n"
        + "import timber.log.Timber;\n"
        + "public class Example {\n"
        + "  public void log() {\n"
        + "     Exception e = new Exception();\n"
        + "     Timber.d(e, \"\");\n"
        + "  }\n"
        + "}";
    assertThat(lintProject(java(source), timberStub)).isEqualTo("src/foo/Example.java:6: "
        + "Warning: Use single-argument log method instead of null/empty message [TimberExceptionLogging]\n"
        + "     Timber.d(e, \"\");\n"
        + "                 ~~\n"
        + "0 errors, 1 warnings\n");
  }

  public void testExceptionLoggingUsingNullMessage() throws Exception {
    @Language("JAVA") String source = ""
        + "package foo;\n"
        + "import timber.log.Timber;\n"
        + "public class Example {\n"
        + "  public void log() {\n"
        + "     Exception e = new Exception();\n"
        + "     Timber.d(e, null);\n"
        + "  }\n"
        + "}";
    assertThat(lintProject(java(source), timberStub)).isEqualTo("src/foo/Example.java:6: "
        + "Warning: Use single-argument log method instead of null/empty message [TimberExceptionLogging]\n"
        + "     Timber.d(e, null);\n"
        + "                 ~~~~\n"
        + "0 errors, 1 warnings\n");
  }

  public void testExceptionLoggingUsingValidMessage() throws Exception {
    @Language("JAVA") String source = ""
        + "package foo;\n"
        + "import timber.log.Timber;\n"
        + "public class Example {\n"
        + "  public void log() {\n"
        + "     Exception e = new Exception();\n"
        + "     Timber.d(e, \"Valid message\");\n"
        + "  }\n"
        + "}";
    assertThat(lintProject(java(source), timberStub)).isEqualTo(NO_WARNINGS);
  }

  public void testDateFormatNotDisplayingWarning() throws Exception {
    @Language("JAVA") String source = ""
        + "package foo;\n"
        + "import timber.log.Timber;\n"
        + "public class Example {\n"
        + "  public void log() {\n"
        + "    Timber.d(\"%tc\", new java.util.Date());\n"
        + "  }\n"
        + "}";
    assertThat(lintProject(java(source), timberStub)).isEqualTo(NO_WARNINGS);
  }

  public void testWrappedBooleanType() throws Exception {
    @Language("JAVA") String source = ""
            + "package foo;\n"
            + "import timber.log.Timber;\n"
            + "public class Example {\n"
            + "  public void log() {\n"
            + "     Timber.d(\"%b\", Boolean.valueOf(true));\n"
            + "  }\n"
            + "}";
    assertThat(lintProject(java(source), timberStub)).isEqualTo(NO_WARNINGS);
  }

  @Override protected Detector getDetector() {
    return new WrongTimberUsageDetector();
  }

  @Override protected List<Issue> getIssues() {
    return Arrays.asList(WrongTimberUsageDetector.getIssues());
  }

  @Override protected boolean allowCompilationErrors() {
    return true;
  }
}
