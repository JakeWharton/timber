package timber.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.java
import com.android.tools.lint.checks.infrastructure.TestFiles.manifest
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class WrongTimberUsageDetectorTest {
  private val TIMBER_STUB = java("""
      |package timber.log;
      |public class Timber {
      |  public static void d(String s, Object... args) {}
      |  public static void d(Throwable t, String s, Object... args) {}
      |  public static Tree tag(String tag) { return new Tree(); }
      |  public static class Tree {
      |    public void d(String s, Object... args) {}
      |    public void d(Throwable t, String s, Object... args) {}
      |  }
      |  private static final Tree TREE_OF_SOULS = new Tree();
      |}""".trimMargin())

  @Test fun usingAndroidLogWithTwoArguments() {
    lint()
        .files(
            java("""
                |package foo;
                |import android.util.Log;
                |public class Example {
                |  public void log() {
                |    Log.d("TAG", "msg");
                |  }
                |}""".trimMargin())
        )
        .issues(WrongTimberUsageDetector.ISSUE_LOG)
        .run()
        .expect("""
            |src/foo/Example.java:5: Warning: Using 'Log' instead of 'Timber' [LogNotTimber]
            |    Log.d("TAG", "msg");
            |    ~~~~~~~~~~~~~~~~~~~
            |0 errors, 1 warnings""".trimMargin())
        .expectFixDiffs("""
            |Fix for src/foo/Example.java line 4: Replace with Timber.tag("TAG").d("msg"):
            |@@ -5 +5
            |-     Log.d("TAG", "msg");
            |+     Timber.tag("TAG").d("msg");
            |Fix for src/foo/Example.java line 4: Replace with Timber.d("msg"):
            |@@ -5 +5
            |-     Log.d("TAG", "msg");
            |+     Timber.d("msg");
            |""".trimMargin())
  }

  @Test fun usingAndroidLogWithThreeArguments() {
    lint()
        .files(
            java("""
                |package foo;
                |import android.util.Log;
                |public class Example {
                |  public void log() {
                |    Log.d("TAG", "msg", new Exception());
                |  }
                |}""".trimMargin())
        )
        .issues(WrongTimberUsageDetector.ISSUE_LOG)
        .run()
        .expect("""
            |src/foo/Example.java:5: Warning: Using 'Log' instead of 'Timber' [LogNotTimber]
            |    Log.d("TAG", "msg", new Exception());
            |    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            |0 errors, 1 warnings""".trimMargin())
        .expectFixDiffs("""
            |Fix for src/foo/Example.java line 4: Replace with Timber.tag("TAG").d(new Exception(), "msg"):
            |@@ -5 +5
            |-     Log.d("TAG", "msg", new Exception());
            |+     Timber.tag("TAG").d(new Exception(), "msg");
            |Fix for src/foo/Example.java line 4: Replace with Timber.d(new Exception(), "msg"):
            |@@ -5 +5
            |-     Log.d("TAG", "msg", new Exception());
            |+     Timber.d(new Exception(), "msg");
            |""".trimMargin())
  }

  @Test fun usingFullyQualifiedAndroidLogWithTwoArguments() {
    lint()
        .files(
            java("""
                |package foo;
                |public class Example {
                |  public void log() {
                |    android.util.Log.d("TAG", "msg");
                |  }
                |}""".trimMargin())
        )
        .issues(WrongTimberUsageDetector.ISSUE_LOG)
        .run()
        .expect("""
            |src/foo/Example.java:4: Warning: Using 'Log' instead of 'Timber' [LogNotTimber]
            |    android.util.Log.d("TAG", "msg");
            |    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            |0 errors, 1 warnings""".trimMargin())
        .expectFixDiffs("""
            |Fix for src/foo/Example.java line 3: Replace with Timber.tag("TAG").d("msg"):
            |@@ -4 +4
            |-     android.util.Log.d("TAG", "msg");
            |+     Timber.tag("TAG").d("msg");
            |Fix for src/foo/Example.java line 3: Replace with Timber.d("msg"):
            |@@ -4 +4
            |-     android.util.Log.d("TAG", "msg");
            |+     Timber.d("msg");
            |""".trimMargin())
  }

  @Test fun usingFullyQualifiedAndroidLogWithThreeArguments() {
    lint()
        .files(
            java("""
                |package foo;
                |public class Example {
                |  public void log() {
                |    android.util.Log.d("TAG", "msg", new Exception());
                |  }
                |}""".trimMargin())
        )
        .issues(WrongTimberUsageDetector.ISSUE_LOG)
        .run()
        .expect("""
            |src/foo/Example.java:4: Warning: Using 'Log' instead of 'Timber' [LogNotTimber]
            |    android.util.Log.d("TAG", "msg", new Exception());
            |    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            |0 errors, 1 warnings""".trimMargin())
        .expectFixDiffs("""
            |Fix for src/foo/Example.java line 3: Replace with Timber.tag("TAG").d(new Exception(), "msg"):
            |@@ -4 +4
            |-     android.util.Log.d("TAG", "msg", new Exception());
            |+     Timber.tag("TAG").d(new Exception(), "msg");
            |Fix for src/foo/Example.java line 3: Replace with Timber.d(new Exception(), "msg"):
            |@@ -4 +4
            |-     android.util.Log.d("TAG", "msg", new Exception());
            |+     Timber.d(new Exception(), "msg");
            |""".trimMargin())
  }

  @Test fun innerStringFormat() {
    lint()
        .files(TIMBER_STUB,
            java("""
                |package foo;
                |import timber.log.Timber;
                |public class Example {
                |  public void log() {
                |     Timber.d(String.format("%s", "arg1"));
                |  }
                |}""".trimMargin())
        )
        .issues(WrongTimberUsageDetector.ISSUE_FORMAT)
        .run()
        .expect("""
            |src/foo/Example.java:5: Warning: Using 'String#format' inside of 'Timber' [StringFormatInTimber]
            |     Timber.d(String.format("%s", "arg1"));
            |              ~~~~~~~~~~~~~~~~~~~~~~~~~~~
            |0 errors, 1 warnings""".trimMargin())
        .expectFixDiffs("""
            |Fix for src/foo/Example.java line 4: Remove String.format(...):
            |@@ -5 +5
            |-      Timber.d(String.format("%s", "arg1"));
            |+      Timber.d("%s", "arg1");
            |""".trimMargin())
  }

  @Test fun innerStringFormatWithStaticImport() {
    lint()
        .files(TIMBER_STUB,
            java("""
                |package foo;
                |import timber.log.Timber;
                |import static java.lang.String.format;
                |public class Example {
                |  public void log() {
                |     Timber.d(format("%s", "arg1"));
                |  }
                |}""".trimMargin())
        )
        .issues(WrongTimberUsageDetector.ISSUE_FORMAT)
        .run()
        .expect("""
            |src/foo/Example.java:6: Warning: Using 'String#format' inside of 'Timber' [StringFormatInTimber]
            |     Timber.d(format("%s", "arg1"));
            |              ~~~~~~~~~~~~~~~~~~~~
            |0 errors, 1 warnings""".trimMargin())
        .expectFixDiffs("""
            |Fix for src/foo/Example.java line 5: Remove String.format(...):
            |@@ -6 +6
            |-      Timber.d(format("%s", "arg1"));
            |+      Timber.d("%s", "arg1");
            |""".trimMargin())
  }

  @Test fun innerStringFormatInNestedMethods() {
    lint()
        .files(TIMBER_STUB,
            java("""
                |package foo;
                |import timber.log.Timber;
                |public class Example {
                |  public void log() {
                |     Timber.d(id(String.format("%s", "arg1")));
                |  }
                |  private String id(String s) { return s; }
                |}""".trimMargin())
        )
        .issues(WrongTimberUsageDetector.ISSUE_FORMAT)
        .run()
        .expect("""
            |src/foo/Example.java:5: Warning: Using 'String#format' inside of 'Timber' [StringFormatInTimber]
            |     Timber.d(id(String.format("%s", "arg1")));
            |                 ~~~~~~~~~~~~~~~~~~~~~~~~~~~
            |0 errors, 1 warnings""".trimMargin())
  }

  @Test fun innerStringFormatInNestedAssignment() {
    lint()
        .files(TIMBER_STUB,
            java("""
                |package foo;
                |import timber.log.Timber;
                |public class Example {
                |  public void log() {
                |    String msg = null;
                |    Timber.d(msg = String.format("msg"));
                |  }
                |}""".trimMargin())
        )
        .issues(WrongTimberUsageDetector.ISSUE_FORMAT)
        .run()
        .expect("""
            |src/foo/Example.java:6: Warning: Using 'String#format' inside of 'Timber' [StringFormatInTimber]
            |    Timber.d(msg = String.format("msg"));
            |                   ~~~~~~~~~~~~~~~~~~~~
            |0 errors, 1 warnings""".trimMargin())
  }

  @Test fun validStringFormatInCodeBlock() {
    lint()
        .files(TIMBER_STUB,
            java("""
                |package foo;
                |import timber.log.Timber;
                |public class Example {
                |  public void log() {
                |    for(;;) {
                |      String name = String.format("msg");
                |    }
                |  }
                |}""".trimMargin())
        )
        .issues(WrongTimberUsageDetector.ISSUE_FORMAT)
        .run()
        .expectClean()
  }

  @Test fun validStringFormatInConstructorCall() {
    lint()
        .files(TIMBER_STUB,
            java("""
                |package foo;
                |public class Example {
                |  public void log() {
                |    new Exception(String.format("msg"));
                |  }
                |}""".trimMargin())
        )
        .issues(WrongTimberUsageDetector.ISSUE_FORMAT)
        .run()
        .expectClean()
  }

  @Test fun validStringFormatInStaticArray() {
    lint()
        .files(TIMBER_STUB,
            java("""
                |package foo;
                |public class Example {
                |  static String[] X = { String.format("%s", 100) };
                |}""".trimMargin())
        )
        .issues(WrongTimberUsageDetector.ISSUE_FORMAT)
        .run()
        .expectClean()
  }


  @Test fun throwableNotAtBeginning() {
    lint()
        .files(TIMBER_STUB,
            java("""
                |package foo;
                |import timber.log.Timber;
                |public class Example {
                |  public void log() {
                |     Exception e = new Exception();
                |     Timber.d("%s", e);
                |  }
                |}""".trimMargin())
        )
        .issues(WrongTimberUsageDetector.ISSUE_THROWABLE)
        .run()
        .expect("""
            |src/foo/Example.java:6: Warning: Throwable should be first argument [ThrowableNotAtBeginning]
            |     Timber.d("%s", e);
            |     ~~~~~~~~~~~~~~~~~
            |0 errors, 1 warnings""".trimMargin())
        .expectFixDiffs("""
            |Fix for src/foo/Example.java line 5: Replace with e, "%s":
            |@@ -6 +6
            |-      Timber.d("%s", e);
            |+      Timber.d(e, "%s");
            |""".trimMargin())
  }

  @Test fun stringConcatenationBothLiterals() {
    lint()
        .files(TIMBER_STUB,
            java("""
                |package foo;
                |import timber.log.Timber;
                |public class Example {
                |  public void log() {
                |     Timber.d("foo" + "bar");
                |  }
                |}""".trimMargin()))
        .issues(WrongTimberUsageDetector.ISSUE_BINARY)
        .run()
        .expect("""
            |src/foo/Example.java:5: Warning: Replace String concatenation with Timber's string formatting [BinaryOperationInTimber]
            |     Timber.d("foo" + "bar");
            |              ~~~~~~~~~~~~~
            |0 errors, 1 warnings""".trimMargin())
        .expectFixDiffs("""
            |Fix for src/foo/Example.java line 4: Replace with "foobar":
            |@@ -5 +5
            |-      Timber.d("foo" + "bar");
            |+      Timber.d("foobar");
            |""".trimMargin())
  }

  @Test fun stringConcatenationLeftLiteral() {
    lint()
        .files(TIMBER_STUB,
            java("""
                |package foo;
                |import timber.log.Timber;
                |public class Example {
                |  public void log() {
                |     String foo = "foo";
                |     Timber.d(foo + "bar");
                |  }
                |}""".trimMargin()))
        .issues(WrongTimberUsageDetector.ISSUE_BINARY)
        .run()
        .expect("""
            |src/foo/Example.java:6: Warning: Replace String concatenation with Timber's string formatting [BinaryOperationInTimber]
            |     Timber.d(foo + "bar");
            |              ~~~~~~~~~~~
            |0 errors, 1 warnings""".trimMargin())
        .expectFixDiffs("""
            |Fix for src/foo/Example.java line 5: Replace with "%sbar", foo:
            |@@ -6 +6
            |-      Timber.d(foo + "bar");
            |+      Timber.d("%sbar", foo);
            |""".trimMargin())
  }

  @Test fun stringConcatenationRightLiteral() {
    lint()
        .files(TIMBER_STUB,
            java("""
                |package foo;
                |import timber.log.Timber;
                |public class Example {
                |  public void log() {
                |     String bar = "bar";
                |     Timber.d("foo" + bar);
                |  }
                |}""".trimMargin()))
        .issues(WrongTimberUsageDetector.ISSUE_BINARY)
        .run()
        .expect("""
            |src/foo/Example.java:6: Warning: Replace String concatenation with Timber's string formatting [BinaryOperationInTimber]
            |     Timber.d("foo" + bar);
            |              ~~~~~~~~~~~
            |0 errors, 1 warnings""".trimMargin())
        .expectFixDiffs("""
            |Fix for src/foo/Example.java line 5: Replace with "foo%s", bar:
            |@@ -6 +6
            |-      Timber.d("foo" + bar);
            |+      Timber.d("foo%s", bar);
            |""".trimMargin())
  }

  @Test fun stringConcatenationBothVariables() {
    lint()
        .files(TIMBER_STUB,
            java("""
                |package foo;
                |import timber.log.Timber;
                |public class Example {
                |  public void log() {
                |     String foo = "foo";
                |     String bar = "bar";
                |     Timber.d(foo + bar);
                |  }
                |}""".trimMargin()))
        .issues(WrongTimberUsageDetector.ISSUE_BINARY)
        .run()
        .expect("""
            |src/foo/Example.java:7: Warning: Replace String concatenation with Timber's string formatting [BinaryOperationInTimber]
            |     Timber.d(foo + bar);
            |              ~~~~~~~~~
            |0 errors, 1 warnings""".trimMargin())
        .expectFixDiffs("""
            |Fix for src/foo/Example.java line 6: Replace with "%s%s", foo, bar:
            |@@ -7 +7
            |-      Timber.d(foo + bar);
            |+      Timber.d("%s%s", foo, bar);
            |""".trimMargin())
  }

  @Test fun stringConcatenationInsideTernary() {
    lint()
        .files(TIMBER_STUB,
            java("""
                |package foo;
                |import timber.log.Timber;
                |public class Example {
                |  public void log() {
                |     String s = "world!";
                |     Timber.d(true ? "Hello, " + s : "Bye");
                |  }
                |}""".trimMargin())
        )
        .issues(WrongTimberUsageDetector.ISSUE_BINARY)
        .run()
        .expect("""
            |src/foo/Example.java:6: Warning: Replace String concatenation with Timber's string formatting [BinaryOperationInTimber]
            |     Timber.d(true ? "Hello, " + s : "Bye");
            |                     ~~~~~~~~~~~~~
            |0 errors, 1 warnings""".trimMargin())
  }

  @Test fun tooManyFormatArgs() {
    lint()
        .files(TIMBER_STUB,
            java("""
                |package foo;
                |import timber.log.Timber;
                |public class Example {
                |  public void log() {
                |     Timber.d("%s %s", "arg1");
                |  }
                |}""".trimMargin())
        )
        .issues(WrongTimberUsageDetector.ISSUE_ARG_COUNT)
        .run()
        .expect("""
            |src/foo/Example.java:5: Error: Wrong argument count, format string %s %s requires 2 but format call supplies 1 [TimberArgCount]
            |     Timber.d("%s %s", "arg1");
            |     ~~~~~~~~~~~~~~~~~~~~~~~~~
            |1 errors, 0 warnings""".trimMargin())
  }

  @Test fun tooManyArgs() {
    lint()
        .files(TIMBER_STUB,
            java("""
                |package foo;
                |import timber.log.Timber;
                |public class Example {
                |  public void log() {
                |     Timber.d("%s", "arg1", "arg2");
                |  }
                |}""".trimMargin())
        )
        .issues(WrongTimberUsageDetector.ISSUE_ARG_COUNT)
        .run()
        .expect("""
            |src/foo/Example.java:5: Error: Wrong argument count, format string %s requires 1 but format call supplies 2 [TimberArgCount]
            |     Timber.d("%s", "arg1", "arg2");
            |     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            |1 errors, 0 warnings""".trimMargin())
  }

  @Test fun wrongArgTypes() {
    lint()
        .files(TIMBER_STUB,
            java("""
                |package foo;
                |import timber.log.Timber;
                |public class Example {
                |  public void log() {
                |     Timber.d("%d", "arg1");
                |  }
                |}""".trimMargin())
        )
        .issues(WrongTimberUsageDetector.ISSUE_ARG_TYPES)
        .run()
        .expect("""
            |src/foo/Example.java:5: Error: Wrong argument type for formatting argument '#1' in %d: conversion is 'd', received String (argument #2 in method call) [TimberArgTypes]
            |     Timber.d("%d", "arg1");
            |                    ~~~~~~
            |1 errors, 0 warnings""".trimMargin())
  }

  @Test fun tagTooLongLiteralOnly() {
    lint()
        .files(TIMBER_STUB,
            java("""
                |package foo;
                |import timber.log.Timber;
                |public class Example {
                |  public void log() {
                |     Timber.tag("abcdefghijklmnopqrstuvwx");
                |  }
                |}""".trimMargin())
        )
        .issues(WrongTimberUsageDetector.ISSUE_TAG_LENGTH)
        .run()
        .expect("""
            |src/foo/Example.java:5: Error: The logging tag can be at most 23 characters, was 24 (abcdefghijklmnopqrstuvwx) [TimberTagLength]
            |     Timber.tag("abcdefghijklmnopqrstuvwx");
            |                ~~~~~~~~~~~~~~~~~~~~~~~~~~
            |1 errors, 0 warnings""".trimMargin())
  }

  @Test fun tagTooLongLiteralPlusField() {
    lint()
        .files(TIMBER_STUB,
            java("""
                |package foo;
                |import timber.log.Timber;
                |public class Example {
                |  private final String field = "x";
                |  public void log() {
                |     Timber.tag("abcdefghijklmnopqrstuvw" + field);
                |  }
                |}""".trimMargin())
        )
        .issues(WrongTimberUsageDetector.ISSUE_TAG_LENGTH)
        .run()
        .expect("""
            |src/foo/Example.java:6: Error: The logging tag can be at most 23 characters, was 24 (abcdefghijklmnopqrstuvwx) [TimberTagLength]
            |     Timber.tag("abcdefghijklmnopqrstuvw" + field);
            |                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            |1 errors, 0 warnings""".trimMargin())
  }

  @Test fun tagTooLongLiteralOnlyAfterApi23() {
    lint()
        .files(TIMBER_STUB,
            java("""
                |package foo;
                |import timber.log.Timber;
                |public class Example {
                |  public void log() {
                |     Timber.tag("abcdefghijklmnopqrstuvwx");
                |  }
                |}""".trimMargin()),
            manifest().minSdk(24)
        )
        .issues(WrongTimberUsageDetector.ISSUE_TAG_LENGTH)
        .run()
        .expectClean()
  }

  @Test fun tooManyFormatArgsInTag() {
    lint()
        .files(TIMBER_STUB,
            java("""
                |package foo;
                |import timber.log.Timber;
                |public class Example {
                |  public void log() {
                |     Timber.tag("tag").d("%s %s", "arg1");
                |  }
                |}""".trimMargin())
        )
        .issues(WrongTimberUsageDetector.ISSUE_ARG_COUNT)
        .run()
        .expect("""
            |src/foo/Example.java:5: Error: Wrong argument count, format string %s %s requires 2 but format call supplies 1 [TimberArgCount]
            |     Timber.tag("tag").d("%s %s", "arg1");
            |     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            |1 errors, 0 warnings""".trimMargin())
  }

  @Test fun tooManyArgsInTag() {
    lint()
        .files(TIMBER_STUB,
            java("""
                |package foo;
                |import timber.log.Timber;
                |public class Example {
                |  public void log() {
                |     Timber.tag("tag").d("%s", "arg1", "arg2");
                |  }
                |}""".trimMargin())
        )
        .issues(WrongTimberUsageDetector.ISSUE_ARG_COUNT)
        .run()
        .expect("""
            |src/foo/Example.java:5: Error: Wrong argument count, format string %s requires 1 but format call supplies 2 [TimberArgCount]
            |     Timber.tag("tag").d("%s", "arg1", "arg2");
            |     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            |1 errors, 0 warnings""".trimMargin())
  }

  @Test fun wrongArgTypesInTag() {
    lint()
        .files(TIMBER_STUB,
            java("""
                |package foo;
                |import timber.log.Timber;
                |public class Example {
                |  public void log() {
                |     Timber.tag("tag").d("%d", "arg1");
                |  }
                |}""".trimMargin())
        )
        .issues(WrongTimberUsageDetector.ISSUE_ARG_TYPES)
        .run()
        .expect("""
            |src/foo/Example.java:5: Error: Wrong argument type for formatting argument '#1' in %d: conversion is 'd', received String (argument #2 in method call) [TimberArgTypes]
            |     Timber.tag("tag").d("%d", "arg1");
            |                               ~~~~~~
            |1 errors, 0 warnings""".trimMargin())
  }

  @Test fun exceptionLoggingUsingExceptionMessage() {
    lint()
        .files(TIMBER_STUB,
            java("""
                |package foo;
                |import timber.log.Timber;
                |public class Example {
                |  public void log() {
                |     Exception e = new Exception();
                |     Timber.d(e.getMessage());
                |  }
                |}""".trimMargin())
        )
        .issues(WrongTimberUsageDetector.ISSUE_EXCEPTION_LOGGING)
        .run()
        .expect("""
            |src/foo/Example.java:6: Warning: Explicitly logging exception message is redundant [TimberExceptionLogging]
            |     Timber.d(e.getMessage());
            |     ~~~~~~~~~~~~~~~~~~~~~~~~
            |0 errors, 1 warnings""".trimMargin())
        .expectFixDiffs("""
            |Fix for src/foo/Example.java line 5: Replace message with throwable:
            |@@ -6 +6
            |-      Timber.d(e.getMessage());
            |+      Timber.d(e);
            |""".trimMargin())
  }

  @Test fun exceptionLoggingUsingExceptionMessageInLambdaWithExpressionBody() {
    lint()
        .files(TIMBER_STUB,
            java("""
                |package foo;
                |import timber.log.Timber;
                |private interface Callback {
                |  void call(Exception e);
                |}
                |public class Example {
                |  public void log() {
                |     Callback cb = t -> {
                |       Timber.d(t.getMessage());
                |     };
                |  }
                |}
                |""".trimMargin()
            )
        )
        .issues(WrongTimberUsageDetector.ISSUE_EXCEPTION_LOGGING)
        .run()
        .expect("""
                |src/foo/Callback.java:9: Warning: Explicitly logging exception message is redundant [TimberExceptionLogging]
                |       Timber.d(t.getMessage());
                |       ~~~~~~~~~~~~~~~~~~~~~~~~
                |0 errors, 1 warnings""".trimMargin())
        .expectFixDiffs("""
                |Fix for src/foo/Callback.java line 8: Replace message with throwable:
                |@@ -9 +9
                |-        Timber.d(t.getMessage());
                |+        Timber.d(t);
                |Fix for src/foo/Callback.java line 8: Replace message with throwable + replace lambda with method reference:
                |@@ -8 +8
                |-      Callback cb = t -> {
                |-        Timber.d(t.getMessage());
                |-      };
                |+      Callback cb = Timber::d;
                |""".trimMargin())
  }

  @Test fun exceptionLoggingUsingExceptionMessageInLambdaWithBlockBody() {
    lint()
        .files(TIMBER_STUB,
            java("""
                |package foo;
                |import timber.log.Timber;
                |private interface Callback {
                |  void call(Exception e);
                |}
                |public class Example {
                |  public void log() {
                |     Callback cb = t -> Timber.d(t.getMessage());
                |  }
                |}
                |""".trimMargin()
            )
        )
        .issues(WrongTimberUsageDetector.ISSUE_EXCEPTION_LOGGING)
        .run()
        .expect("""
                |src/foo/Callback.java:8: Warning: Explicitly logging exception message is redundant [TimberExceptionLogging]
                |     Callback cb = t -> Timber.d(t.getMessage());
                |                        ~~~~~~~~~~~~~~~~~~~~~~~~
                |0 errors, 1 warnings""".trimMargin())
        .expectFixDiffs("""
                |Fix for src/foo/Callback.java line 7: Replace message with throwable:
                |@@ -8 +8
                |-      Callback cb = t -> Timber.d(t.getMessage());
                |+      Callback cb = t -> Timber.d(t);
                |Fix for src/foo/Callback.java line 7: Replace message with throwable + replace lambda with method reference:
                |@@ -8 +8
                |-      Callback cb = t -> Timber.d(t.getMessage());
                |+      Callback cb = Timber::d;
                |""".trimMargin())
  }

  @Test fun exceptionLoggingUsingExceptionMessageArgument() {
    lint()
        .files(TIMBER_STUB,
            java("""
                |package foo;
                |import timber.log.Timber;
                |public class Example {
                |  public void log() {
                |     Exception e = new Exception();
                |     Timber.d(e, e.getMessage());
                |  }
                |}""".trimMargin())
        )
        .issues(WrongTimberUsageDetector.ISSUE_EXCEPTION_LOGGING)
        .run()
        .expect("""
            |src/foo/Example.java:6: Warning: Explicitly logging exception message is redundant [TimberExceptionLogging]
            |     Timber.d(e, e.getMessage());
            |     ~~~~~~~~~~~~~~~~~~~~~~~~~~~
            |0 errors, 1 warnings""".trimMargin())
        .expectFixDiffs("""
            |Fix for src/foo/Example.java line 5: Remove redundant argument:
            |@@ -6 +6
            |-      Timber.d(e, e.getMessage());
            |+      Timber.d(e);
            |""".trimMargin())
  }

  @Test fun exceptionLoggingUsingVariable() {
    lint()
        .files(TIMBER_STUB,
            java("""
                |package foo;
                |import timber.log.Timber;
                |public class Example {
                |  public void log() {
                |     String msg = "Hello";
                |     Exception e = new Exception();
                |     Timber.d(e, msg);
                |  }
                |}""".trimMargin())
        )
        .issues(WrongTimberUsageDetector.ISSUE_EXCEPTION_LOGGING)
        .run()
        .expectClean()
  }

  @Test fun exceptionLoggingUsingNonFinalField() {
    lint()
        .files(TIMBER_STUB,
            java("""
                |package foo;
                |import timber.log.Timber;
                |public class Example {
                |  private String message;
                |  public void log() {
                |     Exception e = new Exception();
                |     Timber.d(e, message);
                |  }
                |}""".trimMargin())
        )
        .issues(WrongTimberUsageDetector.ISSUE_EXCEPTION_LOGGING)
        .run()
        .expectClean()
  }

  @Test fun exceptionLoggingUsingFinalField() {
    lint()
        .files(TIMBER_STUB,
            java("""
                |package foo;
                |import timber.log.Timber;
                |public class Example {
                |  private final String message = "foo";
                |  public void log() {
                |     Exception e = new Exception();
                |     Timber.d(e, message);
                |  }
                |}""".trimMargin())
        )
        .issues(WrongTimberUsageDetector.ISSUE_EXCEPTION_LOGGING)
        .run()
        .expectClean()
  }

  @Test fun exceptionLoggingUsingEmptyStringMessage() {
    lint()
        .files(TIMBER_STUB,
            java("""
                |package foo;
                |import timber.log.Timber;
                |public class Example {
                |  public void log() {
                |     Exception e = new Exception();
                |     Timber.d(e, "");
                |  }
                |}""".trimMargin())
        )
        .issues(WrongTimberUsageDetector.ISSUE_EXCEPTION_LOGGING)
        .run()
        .expect("""
            |src/foo/Example.java:6: Warning: Use single-argument log method instead of null/empty message [TimberExceptionLogging]
            |     Timber.d(e, "");
            |     ~~~~~~~~~~~~~~~
            |0 errors, 1 warnings""".trimMargin())
        .expectFixDiffs("""
            |Fix for src/foo/Example.java line 5: Remove redundant argument:
            |@@ -6 +6
            |-      Timber.d(e, "");
            |+      Timber.d(e);
            |""".trimMargin())
  }

  @Test fun exceptionLoggingUsingNullMessage() {
    lint()
        .files(TIMBER_STUB,
            java("""
                |package foo;
                |import timber.log.Timber;
                |public class Example {
                |  public void log() {
                |     Exception e = new Exception();
                |     Timber.d(e, null);
                |  }
                |}""".trimMargin())
        )
        .issues(WrongTimberUsageDetector.ISSUE_EXCEPTION_LOGGING)
        .run()
        .expect("""
            |src/foo/Example.java:6: Warning: Use single-argument log method instead of null/empty message [TimberExceptionLogging]
            |     Timber.d(e, null);
            |     ~~~~~~~~~~~~~~~~~
            |0 errors, 1 warnings""".trimMargin())
        .expectFixDiffs("""
            |Fix for src/foo/Example.java line 5: Remove redundant argument:
            |@@ -6 +6
            |-      Timber.d(e, null);
            |+      Timber.d(e);
            |""".trimMargin())
  }

  @Test fun exceptionLoggingUsingValidMessage() {
    lint()
        .files(TIMBER_STUB,
            java("""
                |package foo;
                |import timber.log.Timber;
                |public class Example {
                |  public void log() {
                |     Exception e = new Exception();
                |     Timber.d(e, "Valid message");
                |  }
                |}""".trimMargin())
        )
        .issues(WrongTimberUsageDetector.ISSUE_EXCEPTION_LOGGING)
        .run()
        .expectClean()
  }

  @Test fun dateFormatNotDisplayingWarning() {
    lint()
        .files(TIMBER_STUB,
            java("""
                |package foo;
                |import timber.log.Timber;
                |public class Example {
                |  public void log() {
                |    Timber.d("%tc", new java.util.Date());
                |  }
                |}""".trimMargin())
        )
        .issues(*WrongTimberUsageDetector.getIssues())
        .run()
        .expectClean()
  }

  @Test fun wrappedBooleanType() {
    lint()
        .files(TIMBER_STUB,
            java("""
                |package foo;
                |import timber.log.Timber;
                |public class Example {
                |  public void log() {
                |     Timber.d("%b", Boolean.valueOf(true));
                |  }
                |}""".trimMargin()))
        .issues(*WrongTimberUsageDetector.getIssues())
        .run()
        .expectClean()
  }
}
