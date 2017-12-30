package timber.lint;

import com.android.tools.lint.checks.infrastructure.TestFile;
import org.junit.Test;

import static com.android.tools.lint.checks.infrastructure.TestFiles.java;
import static com.android.tools.lint.checks.infrastructure.TestFiles.manifest;
import static com.android.tools.lint.checks.infrastructure.TestLintTask.lint;

public final class WrongTimberUsageDetectorTest {
  private static final TestFile TIMBER_STUB = java(""
      + "package timber.log;\n"
      + "public class Timber {\n"
      + "  public static void d(String s, Object... args) {}\n"
      + "  public static void d(Throwable t, String s, Object... args) {}\n"
      + "  public static Tree tag(String tag) { return new Tree(); }\n"
      + "  public static class Tree {\n"
      + "    public void d(String s, Object... args) {}\n"
      + "    public void d(Throwable t, String s, Object... args) {}"
      + "  }\n"
      + "  private static final Tree TREE_OF_SOULS = new Tree();\n"
      + "}");

  @Test public void usingAndroidLogWithTwoArguments() {
    lint() //
        .files( //
            java(""
                + "package foo;\n"
                + "import android.util.Log;\n"
                + "public class Example {\n"
                + "  public void log() {\n"
                + "    Log.d(\"TAG\", \"msg\");\n"
                + "  }\n"
                + "}") //
        )
        .issues(WrongTimberUsageDetector.ISSUE_LOG) //
        .run()
        .expect("src/foo/Example.java:5: "
            + "Warning: Using 'Log' instead of 'Timber' [LogNotTimber]\n"
            + "    Log.d(\"TAG\", \"msg\");\n"
            + "    ~~~~~~~~~~~~~~~~~~~\n"
            + "0 errors, 1 warnings\n")
        .expectFixDiffs(
            "Fix for src/foo/Example.java line 4: Replace with Timber.tag(\"TAG\").d(\"msg\"):\n"
                + "@@ -5 +5\n"
                + "-     Log.d(\"TAG\", \"msg\");\n"
                + "+     Timber.tag(\"TAG\").d(\"msg\");\n"
                + "Fix for src/foo/Example.java line 4: Replace with Timber.d(\"msg\"):\n"
                + "@@ -5 +5\n"
                + "-     Log.d(\"TAG\", \"msg\");\n"
                + "+     Timber.d(\"msg\");\n");
  }

  @Test public void usingAndroidLogWithThreeArguments() {
    lint() //
        .files( //
            java(""
                + "package foo;\n"
                + "import android.util.Log;\n"
                + "public class Example {\n"
                + "  public void log() {\n"
                + "    Log.d(\"TAG\", \"msg\", new Exception());\n"
                + "  }\n"
                + "}") //
        )
        .issues(WrongTimberUsageDetector.ISSUE_LOG) //
        .run()
        .expect("src/foo/Example.java:5: "
            + "Warning: Using 'Log' instead of 'Timber' [LogNotTimber]\n"
            + "    Log.d(\"TAG\", \"msg\", new Exception());\n"
            + "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
            + "0 errors, 1 warnings\n")
        .expectFixDiffs(
            "Fix for src/foo/Example.java line 4: Replace with Timber.tag(\"TAG\").d(new Exception(), \"msg\"):\n"
                + "@@ -5 +5\n"
                + "-     Log.d(\"TAG\", \"msg\", new Exception());\n"
                + "+     Timber.tag(\"TAG\").d(new Exception(), \"msg\");\n"
                + "Fix for src/foo/Example.java line 4: Replace with Timber.d(new Exception(), \"msg\"):\n"
                + "@@ -5 +5\n"
                + "-     Log.d(\"TAG\", \"msg\", new Exception());\n"
                + "+     Timber.d(new Exception(), \"msg\");\n");
  }

  @Test public void usingFullyQualifiedAndroidLogWithTwoArguments() {
    lint() //
        .files( //
            java(""
                + "package foo;\n"
                + "public class Example {\n"
                + "  public void log() {\n"
                + "    android.util.Log.d(\"TAG\", \"msg\");\n"
                + "  }\n"
                + "}") //
        )
        .issues(WrongTimberUsageDetector.ISSUE_LOG) //
        .run()
        .expect("src/foo/Example.java:4: "
            + "Warning: Using 'Log' instead of 'Timber' [LogNotTimber]\n"
            + "    android.util.Log.d(\"TAG\", \"msg\");\n"
            + "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
            + "0 errors, 1 warnings\n")
        .expectFixDiffs(
            "Fix for src/foo/Example.java line 3: Replace with Timber.tag(\"TAG\").d(\"msg\"):\n"
                + "@@ -4 +4\n"
                + "-     android.util.Log.d(\"TAG\", \"msg\");\n"
                + "+     Timber.tag(\"TAG\").d(\"msg\");\n"
                + "Fix for src/foo/Example.java line 3: Replace with Timber.d(\"msg\"):\n"
                + "@@ -4 +4\n"
                + "-     android.util.Log.d(\"TAG\", \"msg\");\n"
                + "+     Timber.d(\"msg\");\n");
  }

  @Test public void usingFullyQualifiedAndroidLogWithThreeArguments() {
    lint() //
        .files( //
            java(""
                + "package foo;\n"
                + "public class Example {\n"
                + "  public void log() {\n"
                + "    android.util.Log.d(\"TAG\", \"msg\", new Exception());\n"
                + "  }\n"
                + "}") //
        )
        .issues(WrongTimberUsageDetector.ISSUE_LOG) //
        .run()
        .expect("src/foo/Example.java:4: "
            + "Warning: Using 'Log' instead of 'Timber' [LogNotTimber]\n"
            + "    android.util.Log.d(\"TAG\", \"msg\", new Exception());\n"
            + "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
            + "0 errors, 1 warnings\n")
        .expectFixDiffs(
            "Fix for src/foo/Example.java line 3: Replace with Timber.tag(\"TAG\").d(new Exception(), \"msg\"):\n"
                + "@@ -4 +4\n"
                + "-     android.util.Log.d(\"TAG\", \"msg\", new Exception());\n"
                + "+     Timber.tag(\"TAG\").d(new Exception(), \"msg\");\n"
                + "Fix for src/foo/Example.java line 3: Replace with Timber.d(new Exception(), \"msg\"):\n"
                + "@@ -4 +4\n"
                + "-     android.util.Log.d(\"TAG\", \"msg\", new Exception());\n"
                + "+     Timber.d(new Exception(), \"msg\");\n");
  }

  @Test public void innerStringFormat() {
    lint() //
        .files(TIMBER_STUB, //
            java(""
                + "package foo;\n"
                + "import timber.log.Timber;\n"
                + "public class Example {\n"
                + "  public void log() {\n"
                + "     Timber.d(String.format(\"%s\", \"arg1\"));\n"
                + "  }\n"
                + "}") //
        )
        .issues(WrongTimberUsageDetector.ISSUE_FORMAT)
        .run()
        .expect("src/foo/Example.java:5: "
            + "Warning: Using 'String#format' inside of 'Timber' [StringFormatInTimber]\n"
            + "     Timber.d(String.format(\"%s\", \"arg1\"));\n"
            + "              ~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
            + "0 errors, 1 warnings\n")
        .expectFixDiffs("Fix for src/foo/Example.java line 4: Remove String.format(...):\n"
            + "@@ -5 +5\n"
            + "-      Timber.d(String.format(\"%s\", \"arg1\"));\n"
            + "+      Timber.d(\"%s\", \"arg1\");\n");
  }

  @Test public void innerStringFormatWithStaticImport() {
    lint() //
        .files(TIMBER_STUB, //
            java(""
                + "package foo;\n"
                + "import timber.log.Timber;\n"
                + "import static java.lang.String.format;\n"
                + "public class Example {\n"
                + "  public void log() {\n"
                + "     Timber.d(format(\"%s\", \"arg1\"));\n"
                + "  }\n"
                + "}") //
        )
        .issues(WrongTimberUsageDetector.ISSUE_FORMAT)
        .run()
        .expect("src/foo/Example.java:6: "
            + "Warning: Using 'String#format' inside of 'Timber' [StringFormatInTimber]\n"
            + "     Timber.d(format(\"%s\", \"arg1\"));\n"
            + "              ~~~~~~~~~~~~~~~~~~~~\n"
            + "0 errors, 1 warnings\n")
        .expectFixDiffs("Fix for src/foo/Example.java line 5: Remove String.format(...):\n"
            + "@@ -6 +6\n"
            + "-      Timber.d(format(\"%s\", \"arg1\"));\n"
            + "+      Timber.d(\"%s\", \"arg1\");\n");
  }

  @Test public void innerStringFormatInNestedMethods() {
    lint() //
        .files(TIMBER_STUB, //
            java(""
                + "package foo;\n"
                + "import timber.log.Timber;\n"
                + "public class Example {\n"
                + "  public void log() {\n"
                + "     Timber.d(id(String.format(\"%s\", \"arg1\")));\n"
                + "  }\n"
                + "  private String id(String s) { return s; }\n"
                + "}") //
        )
        .issues(WrongTimberUsageDetector.ISSUE_FORMAT)
        .run()
        .expect("src/foo/Example.java:5: "
            + "Warning: Using 'String#format' inside of 'Timber' [StringFormatInTimber]\n"
            + "     Timber.d(id(String.format(\"%s\", \"arg1\")));\n"
            + "                 ~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
            + "0 errors, 1 warnings\n");
  }

  @Test public void innerStringFormatInNestedAssignment() {
    lint() //
        .files(TIMBER_STUB, //
            java(""
                + "package foo;\n"
                + "import timber.log.Timber;\n"
                + "public class Example {\n"
                + "  public void log() {\n"
                + "    String msg = null;\n"
                + "    Timber.d(msg = String.format(\"msg\"));\n"
                + "  }\n"
                + "}") //
        )
        .issues(WrongTimberUsageDetector.ISSUE_FORMAT)
        .run()
        .expect("src/foo/Example.java:6: "
            + "Warning: Using 'String#format' inside of 'Timber' [StringFormatInTimber]\n"
            + "    Timber.d(msg = String.format(\"msg\"));\n"
            + "                   ~~~~~~~~~~~~~~~~~~~~\n"
            + "0 errors, 1 warnings\n");
  }

  @Test public void validStringFormatInCodeBlock() {
    lint() //
        .files(TIMBER_STUB, //
            java(""
                + "package foo;\n"
                + "import timber.log.Timber;\n"
                + "public class Example {\n"
                + "  public void log() {\n"
                + "    for(;;) {\n"
                + "      String name = String.format(\"msg\");\n"
                + "    }\n"
                + "  }\n"
                + "}") //
        ) //
        .issues(WrongTimberUsageDetector.ISSUE_FORMAT) //
        .run() //
        .expectClean();
  }

  @Test public void validStringFormatInConstructorCall() {
    lint() //
        .files(TIMBER_STUB, //
            java(""
                + "package foo;\n"
                + "public class Example {\n"
                + "  public void log() {\n"
                + "    new Exception(String.format(\"msg\"));\n"
                + "  }\n"
                + "}") //
        )
        .issues(WrongTimberUsageDetector.ISSUE_FORMAT)
        .run()
        .expectClean();
  }

  @Test public void validStringFormatInStaticArray() {
    lint() //
        .files(TIMBER_STUB, //
            java(""
                + "package foo;\n"
                + "public class Example {\n"
                + "  static String[] X = { String.format(\"%s\", 100) };"
                + "}") //
        )
        .issues(WrongTimberUsageDetector.ISSUE_FORMAT)
        .run()
        .expectClean();
  }


  @Test public void throwableNotAtBeginning() {
    lint() //
        .files(TIMBER_STUB, //
            java(""
                + "package foo;\n"
                + "import timber.log.Timber;\n"
                + "public class Example {\n"
                + "  public void log() {\n"
                + "     Exception e = new Exception();\n"
                + "     Timber.d(\"%s\", e);\n"
                + "  }\n"
                + "}") //
        ) //
        .issues(WrongTimberUsageDetector.ISSUE_THROWABLE)
        .run()
        .expect("src/foo/Example.java:6: "
            + "Warning: Throwable should be first argument [ThrowableNotAtBeginning]\n"
            + "     Timber.d(\"%s\", e);\n"
            + "     ~~~~~~~~~~~~~~~~~\n"
            + "0 errors, 1 warnings\n")
        .expectFixDiffs("Fix for src/foo/Example.java line 5: Replace with e, \"%s\":\n"
            + "@@ -6 +6\n"
            + "-      Timber.d(\"%s\", e);\n"
            + "+      Timber.d(e, \"%s\");\n");
  }

  @Test public void stringConcatenationBothLiterals() {
    lint() //
        .files(TIMBER_STUB, //
            java(""
                + "package foo;\n"
                + "import timber.log.Timber;\n"
                + "public class Example {\n"
                + "  public void log() {\n"
                + "     Timber.d(\"foo\" + \"bar\");\n"
                + "  }\n"
                + "}"))
        .issues(WrongTimberUsageDetector.ISSUE_BINARY)
        .run()
        .expect("src/foo/Example.java:5: "
            + "Warning: Replace String concatenation with Timber's string formatting [BinaryOperationInTimber]\n"
            + "     Timber.d(\"foo\" + \"bar\");\n"
            + "              ~~~~~~~~~~~~~\n"
            + "0 errors, 1 warnings\n")
        .expectFixDiffs("Fix for src/foo/Example.java line 4: Replace with \"foobar\":\n"
            + "@@ -5 +5\n"
            + "-      Timber.d(\"foo\" + \"bar\");\n"
            + "+      Timber.d(\"foobar\");\n");
  }

  @Test public void stringConcatenationLeftLiteral() {
    lint() //
        .files(TIMBER_STUB, //
            java(""
                + "package foo;\n"
                + "import timber.log.Timber;\n"
                + "public class Example {\n"
                + "  public void log() {\n"
                + "     String foo = \"foo\";\n"
                + "     Timber.d(foo + \"bar\");\n"
                + "  }\n"
                + "}"))
        .issues(WrongTimberUsageDetector.ISSUE_BINARY)
        .run()
        .expect("src/foo/Example.java:6: "
            + "Warning: Replace String concatenation with Timber's string formatting [BinaryOperationInTimber]\n"
            + "     Timber.d(foo + \"bar\");\n"
            + "              ~~~~~~~~~~~\n"
            + "0 errors, 1 warnings\n")
        .expectFixDiffs("Fix for src/foo/Example.java line 5: Replace with \"%sbar\", foo:\n"
            + "@@ -6 +6\n"
            + "-      Timber.d(foo + \"bar\");\n"
            + "+      Timber.d(\"%sbar\", foo);\n");
  }

  @Test public void stringConcatenationRightLiteral() {
    lint() //
        .files(TIMBER_STUB, //
            java(""
                + "package foo;\n"
                + "import timber.log.Timber;\n"
                + "public class Example {\n"
                + "  public void log() {\n"
                + "     String bar = \"bar\";\n"
                + "     Timber.d(\"foo\" + bar);\n"
                + "  }\n"
                + "}"))
        .issues(WrongTimberUsageDetector.ISSUE_BINARY)
        .run()
        .expect("src/foo/Example.java:6: "
            + "Warning: Replace String concatenation with Timber's string formatting [BinaryOperationInTimber]\n"
            + "     Timber.d(\"foo\" + bar);\n"
            + "              ~~~~~~~~~~~\n"
            + "0 errors, 1 warnings\n")
        .expectFixDiffs("Fix for src/foo/Example.java line 5: Replace with \"foo%s\", bar:\n"
            + "@@ -6 +6\n"
            + "-      Timber.d(\"foo\" + bar);\n"
            + "+      Timber.d(\"foo%s\", bar);\n");
  }

  @Test public void stringConcatenationBothVariables() {
    lint() //
        .files(TIMBER_STUB, //
            java(""
                + "package foo;\n"
                + "import timber.log.Timber;\n"
                + "public class Example {\n"
                + "  public void log() {\n"
                + "     String foo = \"foo\";\n"
                + "     String bar = \"bar\";\n"
                + "     Timber.d(foo + bar);\n"
                + "  }\n"
                + "}"))
        .issues(WrongTimberUsageDetector.ISSUE_BINARY)
        .run()
        .expect("src/foo/Example.java:7: "
            + "Warning: Replace String concatenation with Timber's string formatting [BinaryOperationInTimber]\n"
            + "     Timber.d(foo + bar);\n"
            + "              ~~~~~~~~~\n"
            + "0 errors, 1 warnings\n")
        .expectFixDiffs("Fix for src/foo/Example.java line 6: Replace with \"%s%s\", foo, bar:\n"
            + "@@ -7 +7\n"
            + "-      Timber.d(foo + bar);\n"
            + "+      Timber.d(\"%s%s\", foo, bar);\n");
  }

  @Test public void stringConcatenationInsideTernary() {
    lint() //
        .files(TIMBER_STUB, //
            java(""
                + "package foo;\n"
                + "import timber.log.Timber;\n"
                + "public class Example {\n"
                + "  public void log() {\n"
                + "     String s = \"world!\";\n"
                + "     Timber.d(true ? \"Hello, \" + s : \"Bye\");\n"
                + "  }\n"
                + "}") //
        ) //
        .issues(WrongTimberUsageDetector.ISSUE_BINARY)
        .run()
        .expect("src/foo/Example.java:6: "
            + "Warning: Replace String concatenation with Timber's string formatting [BinaryOperationInTimber]\n"
            + "     Timber.d(true ? \"Hello, \" + s : \"Bye\");\n"
            + "                     ~~~~~~~~~~~~~\n"
            + "0 errors, 1 warnings\n");
  }

  @Test public void tooManyFormatArgs() {
    lint() //
        .files(TIMBER_STUB, //
            java(""
                + "package foo;\n"
                + "import timber.log.Timber;\n"
                + "public class Example {\n"
                + "  public void log() {\n"
                + "     Timber.d(\"%s %s\", \"arg1\");\n"
                + "  }\n"
                + "}") //
        ) //
        .issues(WrongTimberUsageDetector.ISSUE_ARG_COUNT)
        .run()
        .expect("src/foo/Example.java:5: "
            + "Error: Wrong argument count, format string %s %s requires 2 but format call supplies 1 [TimberArgCount]\n"
            + "     Timber.d(\"%s %s\", \"arg1\");\n"
            + "     ~~~~~~~~~~~~~~~~~~~~~~~~~\n"
            + "1 errors, 0 warnings\n");
  }

  @Test public void tooManyArgs() {
    lint() //
        .files(TIMBER_STUB, //
            java(""
                + "package foo;\n"
                + "import timber.log.Timber;\n"
                + "public class Example {\n"
                + "  public void log() {\n"
                + "     Timber.d(\"%s\", \"arg1\", \"arg2\");\n"
                + "  }\n"
                + "}") //
        )
        .issues(WrongTimberUsageDetector.ISSUE_ARG_COUNT)
        .run()
        .expect("src/foo/Example.java:5: "
            + "Error: Wrong argument count, format string %s requires 1 but format call supplies 2 [TimberArgCount]\n"
            + "     Timber.d(\"%s\", \"arg1\", \"arg2\");\n"
            + "     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
            + "1 errors, 0 warnings\n");
  }

  @Test public void wrongArgTypes() {
    lint() //
        .files(TIMBER_STUB, //
            java(""
                + "package foo;\n"
                + "import timber.log.Timber;\n"
                + "public class Example {\n"
                + "  public void log() {\n"
                + "     Timber.d(\"%d\", \"arg1\");\n"
                + "  }\n"
                + "}") //
        )
        .issues(WrongTimberUsageDetector.ISSUE_ARG_TYPES)
        .run()
        .expect("src/foo/Example.java:5: "
            + "Error: Wrong argument type for formatting argument '#1' in %d: conversion is 'd', received String (argument #2 in method call) [TimberArgTypes]\n"
            + "     Timber.d(\"%d\", \"arg1\");\n"
            + "                    ~~~~~~\n"
            + "1 errors, 0 warnings\n");
  }

  @Test public void tagTooLongLiteralOnly() {
    lint() //
        .files(TIMBER_STUB, //
            java(""
                + "package foo;\n"
                + "import timber.log.Timber;\n"
                + "public class Example {\n"
                + "  public void log() {\n"
                + "     Timber.tag(\"abcdefghijklmnopqrstuvwx\");\n"
                + "  }\n"
                + "}") //
        )
        .issues(WrongTimberUsageDetector.ISSUE_TAG_LENGTH)
        .run()
        .expect("src/foo/Example.java:5: "
            + "Error: The logging tag can be at most 23 characters, was 24 (abcdefghijklmnopqrstuvwx) [TimberTagLength]\n"
            + "     Timber.tag(\"abcdefghijklmnopqrstuvwx\");\n"
            + "                ~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
            + "1 errors, 0 warnings\n");
  }

  @Test public void tagTooLongLiteralPlusField() {
    lint() //
        .files(TIMBER_STUB, //
            java(""
                + "package foo;\n"
                + "import timber.log.Timber;\n"
                + "public class Example {\n"
                + "  private final String field = \"x\";"
                + "  public void log() {\n"
                + "     Timber.tag(\"abcdefghijklmnopqrstuvw\" + field);\n"
                + "  }\n"
                + "}") //
        )
        .issues(WrongTimberUsageDetector.ISSUE_TAG_LENGTH)
        .run()
        .expect("src/foo/Example.java:5: "
            + "Error: The logging tag can be at most 23 characters, was 24 (abcdefghijklmnopqrstuvwx) [TimberTagLength]\n"
            + "     Timber.tag(\"abcdefghijklmnopqrstuvw\" + field);\n"
            + "                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
            + "1 errors, 0 warnings\n");
  }

  @Test public void tagTooLongLiteralOnlyAfterApi23() {
    lint() //
        .files(TIMBER_STUB, //
            java(""
                + "package foo;\n"
                + "import timber.log.Timber;\n"
                + "public class Example {\n"
                + "  public void log() {\n"
                + "     Timber.tag(\"abcdefghijklmnopqrstuvwx\");\n"
                + "  }\n"
                + "}"), //
            manifest().minSdk(24) //
        )
        .issues(WrongTimberUsageDetector.ISSUE_TAG_LENGTH)
        .run()
        .expectClean();
  }

  @Test public void tooManyFormatArgsInTag() {
    lint() //
        .files(TIMBER_STUB, //
            java(""
                + "package foo;\n"
                + "import timber.log.Timber;\n"
                + "public class Example {\n"
                + "  public void log() {\n"
                + "     Timber.tag(\"tag\").d(\"%s %s\", \"arg1\");\n"
                + "  }\n"
                + "}") //
        )
        .issues(WrongTimberUsageDetector.ISSUE_ARG_COUNT)
        .run()
        .expect("src/foo/Example.java:5: "
            + "Error: Wrong argument count, format string %s %s requires 2 but format call supplies 1 [TimberArgCount]\n"
            + "     Timber.tag(\"tag\").d(\"%s %s\", \"arg1\");\n"
            + "     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
            + "1 errors, 0 warnings\n");
  }

  @Test public void tooManyArgsInTag() {
    lint() //
        .files(TIMBER_STUB, //
            java(""
                + "package foo;\n"
                + "import timber.log.Timber;\n"
                + "public class Example {\n"
                + "  public void log() {\n"
                + "     Timber.tag(\"tag\").d(\"%s\", \"arg1\", \"arg2\");\n"
                + "  }\n"
                + "}") //
        )
        .issues(WrongTimberUsageDetector.ISSUE_ARG_COUNT)
        .run()
        .expect("src/foo/Example.java:5: "
            + "Error: Wrong argument count, format string %s requires 1 but format call supplies 2 [TimberArgCount]\n"
            + "     Timber.tag(\"tag\").d(\"%s\", \"arg1\", \"arg2\");\n"
            + "     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
            + "1 errors, 0 warnings\n");
  }

  @Test public void wrongArgTypesInTag() {
    lint() //
        .files(TIMBER_STUB, //
            java(""
                + "package foo;\n"
                + "import timber.log.Timber;\n"
                + "public class Example {\n"
                + "  public void log() {\n"
                + "     Timber.tag(\"tag\").d(\"%d\", \"arg1\");\n"
                + "  }\n"
                + "}") //
        )
        .issues(WrongTimberUsageDetector.ISSUE_ARG_TYPES)
        .run()
        .expect("src/foo/Example.java:5: "
            + "Error: Wrong argument type for formatting argument '#1' in %d: conversion is 'd', received String (argument #2 in method call) [TimberArgTypes]\n"
            + "     Timber.tag(\"tag\").d(\"%d\", \"arg1\");\n"
            + "                               ~~~~~~\n"
            + "1 errors, 0 warnings\n");
  }

  @Test public void exceptionLoggingUsingExceptionMessage() {
    lint() //
        .files(TIMBER_STUB, //
            java(""
                + "package foo;\n"
                + "import timber.log.Timber;\n"
                + "public class Example {\n"
                + "  public void log() {\n"
                + "     Exception e = new Exception();\n"
                + "     Timber.d(e, e.getMessage());\n"
                + "  }\n"
                + "}") //
        )
        .issues(WrongTimberUsageDetector.ISSUE_EXCEPTION_LOGGING)
        .run()
        .expect("src/foo/Example.java:6: "
            + "Warning: Explicitly logging exception message is redundant [TimberExceptionLogging]\n"
            + "     Timber.d(e, e.getMessage());\n"
            + "     ~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
            + "0 errors, 1 warnings\n")
        .expectFixDiffs("Fix for src/foo/Example.java line 5: Remove redundant argument:\n"
            + "@@ -6 +6\n"
            + "-      Timber.d(e, e.getMessage());\n"
            + "+      Timber.d(e);\n");
  }

  @Test public void exceptionLoggingUsingVariable() {
    lint() //
        .files(TIMBER_STUB, //
            java(""
                + "package foo;\n"
                + "import timber.log.Timber;\n"
                + "public class Example {\n"
                + "  public void log() {\n"
                + "     String msg = \"Hello\";\n"
                + "     Exception e = new Exception();\n"
                + "     Timber.d(e, msg);\n"
                + "  }\n"
                + "}") //
        ) //
        .issues(WrongTimberUsageDetector.ISSUE_EXCEPTION_LOGGING) //
        .run() //
        .expectClean();
  }

  @Test public void exceptionLoggingUsingNonFinalField() {
    lint() //
        .files(TIMBER_STUB, //
            java(""
                + "package foo;\n"
                + "import timber.log.Timber;\n"
                + "public class Example {\n"
                + "  private String message;\n"
                + "  public void log() {\n"
                + "     Exception e = new Exception();\n"
                + "     Timber.d(e, message);\n"
                + "  }\n"
                + "}") //
        ) //
        .issues(WrongTimberUsageDetector.ISSUE_EXCEPTION_LOGGING) //
        .run() //
        .expectClean();
  }
  
  @Test public void exceptionLoggingUsingFinalField() {
    lint() //
        .files(TIMBER_STUB, //
            java(""
                + "package foo;\n"
                + "import timber.log.Timber;\n"
                + "public class Example {\n"
                + "  private final String message = \"foo\";\n"
                + "  public void log() {\n"
                + "     Exception e = new Exception();\n"
                + "     Timber.d(e, message);\n"
                + "  }\n"
                + "}") //
        ) //
        .issues(WrongTimberUsageDetector.ISSUE_EXCEPTION_LOGGING) //
        .run() //
        .expectClean();
  }

  @Test public void exceptionLoggingUsingEmptyStringMessage() {
    lint() //
        .files(TIMBER_STUB, //
            java(""
                + "package foo;\n"
                + "import timber.log.Timber;\n"
                + "public class Example {\n"
                + "  public void log() {\n"
                + "     Exception e = new Exception();\n"
                + "     Timber.d(e, \"\");\n"
                + "  }\n"
                + "}") //
        ) //
        .issues(WrongTimberUsageDetector.ISSUE_EXCEPTION_LOGGING)
        .run()
        .expect("src/foo/Example.java:6: "
            + "Warning: Use single-argument log method instead of null/empty message [TimberExceptionLogging]\n"
            + "     Timber.d(e, \"\");\n"
            + "     ~~~~~~~~~~~~~~~\n"
            + "0 errors, 1 warnings\n")
        .expectFixDiffs("Fix for src/foo/Example.java line 5: Remove redundant argument:\n"
            + "@@ -6 +6\n"
            + "-      Timber.d(e, \"\");\n"
            + "+      Timber.d(e);\n");
  }

  @Test public void exceptionLoggingUsingNullMessage() {
    lint() //
        .files(TIMBER_STUB, //
            java(""
                + "package foo;\n"
                + "import timber.log.Timber;\n"
                + "public class Example {\n"
                + "  public void log() {\n"
                + "     Exception e = new Exception();\n"
                + "     Timber.d(e, null);\n"
                + "  }\n"
                + "}") //
        )
        .issues(WrongTimberUsageDetector.ISSUE_EXCEPTION_LOGGING)
        .run()
        .expect("src/foo/Example.java:6: "
            + "Warning: Use single-argument log method instead of null/empty message [TimberExceptionLogging]\n"
            + "     Timber.d(e, null);\n"
            + "     ~~~~~~~~~~~~~~~~~\n"
            + "0 errors, 1 warnings\n")
        .expectFixDiffs("Fix for src/foo/Example.java line 5: Remove redundant argument:\n"
            + "@@ -6 +6\n"
            + "-      Timber.d(e, null);\n"
            + "+      Timber.d(e);\n");
  }

  @Test public void exceptionLoggingUsingValidMessage() {
    lint() //
        .files(TIMBER_STUB, //
            java(""
                + "package foo;\n"
                + "import timber.log.Timber;\n"
                + "public class Example {\n"
                + "  public void log() {\n"
                + "     Exception e = new Exception();\n"
                + "     Timber.d(e, \"Valid message\");\n"
                + "  }\n"
                + "}") //
        ) //
        .issues(WrongTimberUsageDetector.ISSUE_EXCEPTION_LOGGING) //
        .run() //
        .expectClean();
  }

  @Test public void dateFormatNotDisplayingWarning() {
    lint() //
        .files(TIMBER_STUB, //
            java(""
                + "package foo;\n"
                + "import timber.log.Timber;\n"
                + "public class Example {\n"
                + "  public void log() {\n"
                + "    Timber.d(\"%tc\", new java.util.Date());\n"
                + "  }\n"
                + "}") //
        ) //
        .issues(WrongTimberUsageDetector.getIssues()) //
        .run() //
        .expectClean();
  }

  @Test public void wrappedBooleanType() {
    lint() //
        .files(TIMBER_STUB, //
            java(""
                + "package foo;\n"
                + "import timber.log.Timber;\n"
                + "public class Example {\n"
                + "  public void log() {\n"
                + "     Timber.d(\"%b\", Boolean.valueOf(true));\n"
                + "  }\n"
                + "}")) //
        .issues(WrongTimberUsageDetector.getIssues()) //
        .run() //
        .expectClean();
  }
}
