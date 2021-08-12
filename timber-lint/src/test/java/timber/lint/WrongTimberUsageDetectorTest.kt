package timber.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.java
import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestFiles.manifest
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test
import timber.lint.WrongTimberUsageDetector.Companion.issues

class WrongTimberUsageDetectorTest {
  private val TIMBER_STUB = kotlin("""
      |package timber.log
      |class Timber private constructor() {
      |  private companion object {
      |    @JvmStatic fun d(message: String?, vararg args: Any?) {}
      |    @JvmStatic fun d(t: Throwable?, message: String, vararg args: Any?) {}
      |    @JvmStatic fun tag(tag: String) = Tree()
      |  }
      |  open class Tree {
      |    open fun d(message: String?, vararg args: Any?) {}
      |    open fun d(t: Throwable?, message: String?, vararg args: Any?) {}
      |  }
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
                |}""".trimMargin()),
          kotlin("""
                |package foo
                |import android.util.Log
                |class Example {
                |  fun log() {
                |    Log.d("TAG", "msg")
                |  }
                |}""".trimMargin())
        )
        .issues(*issues)
        .run()
        .expect("""
            |src/foo/Example.java:5: Warning: Using 'Log' instead of 'Timber' [LogNotTimber]
            |    Log.d("TAG", "msg");
            |    ~~~~~~~~~~~~~~~~~~~
            |src/foo/Example.kt:5: Warning: Using 'Log' instead of 'Timber' [LogNotTimber]
            |    Log.d("TAG", "msg")
            |    ~~~~~~~~~~~~~~~~~~~
            |0 errors, 2 warnings""".trimMargin())
        .expectFixDiffs("""
            |Fix for src/foo/Example.java line 5: Replace with Timber.tag("TAG").d("msg"):
            |@@ -5 +5
            |-     Log.d("TAG", "msg");
            |+     Timber.tag("TAG").d("msg");
            |Fix for src/foo/Example.java line 5: Replace with Timber.d("msg"):
            |@@ -5 +5
            |-     Log.d("TAG", "msg");
            |+     Timber.d("msg");
            |Fix for src/foo/Example.kt line 5: Replace with Timber.tag("TAG").d("msg"):
            |@@ -5 +5
            |-     Log.d("TAG", "msg")
            |+     Timber.tag("TAG").d("msg")
            |Fix for src/foo/Example.kt line 5: Replace with Timber.d("msg"):
            |@@ -5 +5
            |-     Log.d("TAG", "msg")
            |+     Timber.d("msg")
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
                |}""".trimMargin()),
            kotlin("""
                |package foo
                |import android.util.Log
                |class Example {
                |  fun log() {
                |    Log.d("TAG", "msg", Exception())
                |  }
                |}""".trimMargin())
        )
        .issues(*issues)
        .run()
        .expect("""
            |src/foo/Example.java:5: Warning: Using 'Log' instead of 'Timber' [LogNotTimber]
            |    Log.d("TAG", "msg", new Exception());
            |    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            |src/foo/Example.kt:5: Warning: Using 'Log' instead of 'Timber' [LogNotTimber]
            |    Log.d("TAG", "msg", Exception())
            |    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            |0 errors, 2 warnings""".trimMargin())
        .expectFixDiffs("""
            |Fix for src/foo/Example.java line 5: Replace with Timber.tag("TAG").d(new Exception(), "msg"):
            |@@ -5 +5
            |-     Log.d("TAG", "msg", new Exception());
            |+     Timber.tag("TAG").d(new Exception(), "msg");
            |Fix for src/foo/Example.java line 5: Replace with Timber.d(new Exception(), "msg"):
            |@@ -5 +5
            |-     Log.d("TAG", "msg", new Exception());
            |+     Timber.d(new Exception(), "msg");
            |Fix for src/foo/Example.kt line 5: Replace with Timber.tag("TAG").d(Exception(), "msg"):
            |@@ -5 +5
            |-     Log.d("TAG", "msg", Exception())
            |+     Timber.tag("TAG").d(Exception(), "msg")
            |Fix for src/foo/Example.kt line 5: Replace with Timber.d(Exception(), "msg"):
            |@@ -5 +5
            |-     Log.d("TAG", "msg", Exception())
            |+     Timber.d(Exception(), "msg")
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
                |}""".trimMargin()),
            kotlin("""
                |package foo
                |class Example {
                |  fun log() {
                |    android.util.Log.d("TAG", "msg")
                |  }
                |}""".trimMargin())
        )
        .issues(*issues)
        .run()
        .expect("""
            |src/foo/Example.java:4: Warning: Using 'Log' instead of 'Timber' [LogNotTimber]
            |    android.util.Log.d("TAG", "msg");
            |    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            |src/foo/Example.kt:4: Warning: Using 'Log' instead of 'Timber' [LogNotTimber]
            |    android.util.Log.d("TAG", "msg")
            |    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            |0 errors, 2 warnings""".trimMargin())
        .expectFixDiffs("""
            |Fix for src/foo/Example.java line 4: Replace with Timber.tag("TAG").d("msg"):
            |@@ -4 +4
            |-     android.util.Log.d("TAG", "msg");
            |+     Timber.tag("TAG").d("msg");
            |Fix for src/foo/Example.java line 4: Replace with Timber.d("msg"):
            |@@ -4 +4
            |-     android.util.Log.d("TAG", "msg");
            |+     Timber.d("msg");
            |Fix for src/foo/Example.kt line 4: Replace with Timber.tag("TAG").d("msg"):
            |@@ -4 +4
            |-     android.util.Log.d("TAG", "msg")
            |+     Timber.tag("TAG").d("msg")
            |Fix for src/foo/Example.kt line 4: Replace with Timber.d("msg"):
            |@@ -4 +4
            |-     android.util.Log.d("TAG", "msg")
            |+     Timber.d("msg")
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
                |}""".trimMargin()),
          kotlin("""
                |package foo
                |class Example {
                |  fun log() {
                |    android.util.Log.d("TAG", "msg", Exception())
                |  }
                |}""".trimMargin())
        )
        .issues(*issues)
        .run()
        .expect("""
            |src/foo/Example.java:4: Warning: Using 'Log' instead of 'Timber' [LogNotTimber]
            |    android.util.Log.d("TAG", "msg", new Exception());
            |    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            |src/foo/Example.kt:4: Warning: Using 'Log' instead of 'Timber' [LogNotTimber]
            |    android.util.Log.d("TAG", "msg", Exception())
            |    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            |0 errors, 2 warnings""".trimMargin())
        .expectFixDiffs("""
            |Fix for src/foo/Example.java line 4: Replace with Timber.tag("TAG").d(new Exception(), "msg"):
            |@@ -4 +4
            |-     android.util.Log.d("TAG", "msg", new Exception());
            |+     Timber.tag("TAG").d(new Exception(), "msg");
            |Fix for src/foo/Example.java line 4: Replace with Timber.d(new Exception(), "msg"):
            |@@ -4 +4
            |-     android.util.Log.d("TAG", "msg", new Exception());
            |+     Timber.d(new Exception(), "msg");
            |Fix for src/foo/Example.kt line 4: Replace with Timber.tag("TAG").d(Exception(), "msg"):
            |@@ -4 +4
            |-     android.util.Log.d("TAG", "msg", Exception())
            |+     Timber.tag("TAG").d(Exception(), "msg")
            |Fix for src/foo/Example.kt line 4: Replace with Timber.d(Exception(), "msg"):
            |@@ -4 +4
            |-     android.util.Log.d("TAG", "msg", Exception())
            |+     Timber.d(Exception(), "msg")
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
                |}""".trimMargin()),
            kotlin("""
                |package foo
                |import timber.log.Timber
                |class Example {
                |  fun log() {
                |     Timber.d(String.format("%s", "arg1"))
                |  }
                |}""".trimMargin())
        )
        .issues(*issues)
        .run()
        .expect("""
            |src/foo/Example.java:5: Warning: Using 'String#format' inside of 'Timber' [StringFormatInTimber]
            |     Timber.d(String.format("%s", "arg1"));
            |              ~~~~~~~~~~~~~~~~~~~~~~~~~~~
            |src/foo/Example.kt:5: Warning: Using 'String#format' inside of 'Timber' [StringFormatInTimber]
            |     Timber.d(String.format("%s", "arg1"))
            |              ~~~~~~~~~~~~~~~~~~~~~~~~~~~
            |0 errors, 2 warnings""".trimMargin())
        .expectFixDiffs("""
            |Fix for src/foo/Example.java line 5: Remove String.format(...):
            |@@ -5 +5
            |-      Timber.d(String.format("%s", "arg1"));
            |+      Timber.d("%s", "arg1");
            |Fix for src/foo/Example.kt line 5: Remove String.format(...):
            |@@ -5 +5
            |-      Timber.d(String.format("%s", "arg1"))
            |+      Timber.d("%s", "arg1")
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
                |}""".trimMargin()),
            kotlin("""
                |package foo
                |import timber.log.Timber
                |import java.lang.String.format
                |class Example {
                |  fun log() {
                |     Timber.d(format("%s", "arg1"))
                |  }
                |}""".trimMargin())
        )
        // Remove when AGP 7.1.0-alpha07 is out
        // https://groups.google.com/g/lint-dev/c/BigCO8sMhKU
        .allowCompilationErrors()
        .issues(*issues)
        .run()
        .expect("""
            |src/foo/Example.java:6: Warning: Using 'String#format' inside of 'Timber' [StringFormatInTimber]
            |     Timber.d(format("%s", "arg1"));
            |              ~~~~~~~~~~~~~~~~~~~~
            |src/foo/Example.kt:6: Warning: Using 'String#format' inside of 'Timber' [StringFormatInTimber]
            |     Timber.d(format("%s", "arg1"))
            |              ~~~~~~~~~~~~~~~~~~~~
            |0 errors, 2 warnings""".trimMargin())
        .expectFixDiffs("""
            |Fix for src/foo/Example.java line 6: Remove String.format(...):
            |@@ -6 +6
            |-      Timber.d(format("%s", "arg1"));
            |+      Timber.d("%s", "arg1");
            |Fix for src/foo/Example.kt line 6: Remove String.format(...):
            |@@ -6 +6
            |-      Timber.d(format("%s", "arg1"))
            |+      Timber.d("%s", "arg1")
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
                |}""".trimMargin()),
            kotlin("""
                |package foo
                |import timber.log.Timber
                |class Example {
                |  fun log() {
                |     Timber.d(id(String.format("%s", "arg1")))
                |  }
                |  private fun id(s: String): String { return s }
                |}""".trimMargin())
        )
        .issues(*issues)
        .run()
        .expect("""
            |src/foo/Example.java:5: Warning: Using 'String#format' inside of 'Timber' [StringFormatInTimber]
            |     Timber.d(id(String.format("%s", "arg1")));
            |                 ~~~~~~~~~~~~~~~~~~~~~~~~~~~
            |src/foo/Example.kt:5: Warning: Using 'String#format' inside of 'Timber' [StringFormatInTimber]
            |     Timber.d(id(String.format("%s", "arg1")))
            |                 ~~~~~~~~~~~~~~~~~~~~~~~~~~~
            |0 errors, 2 warnings""".trimMargin())
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
          // no kotlin equivalent, since nested assignments do not exist
        )
        .issues(*issues)
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
                |public class Example {
                |  public void log() {
                |    for(;;) {
                |      String name = String.format("msg");
                |    }
                |  }
                |}""".trimMargin()),
            kotlin("""
                |package foo
                |class Example {
                |  fun log() {
                |    while(true) {
                |      val name = String.format("msg")
                |    }
                |  }
                |}""".trimMargin())
        )
        .issues(*issues)
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
                |}""".trimMargin()),
          kotlin("""
                |package foo
                |class Example {
                |  fun log() {
                |    Exception(String.format("msg"))
                |  }
                |}""".trimMargin())
        )
        .issues(*issues)
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
                |}""".trimMargin()),
           kotlin("""
                |package foo
                |class Example {
                |  companion object {
                |    val X = arrayOf(String.format("%s", 100))
                |  }
                |}""".trimMargin())
        )
        .issues(*issues)
        .run()
        .expectClean()
  }

  @Test fun validStringFormatExtracted() {
    lint()
      .files(TIMBER_STUB,
          java("""
              |package foo;
              |import timber.log.Timber;
              |public class Example {
              |  public void log() {
              |    String message = String.format("%s", "foo");
              |    Timber.d(message);
              |  }
              |}""".trimMargin()),
          kotlin("""
              |package foo
              |import timber.log.Timber
              |class Example {
              |  fun log() {
              |    val message = String.format("%s", "foo")
              |    Timber.d(message)
              |  }
              |}""".trimMargin()),
      )
      .issues(*issues)
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
                |}""".trimMargin()),
            kotlin("""
                |package foo
                |import timber.log.Timber
                |class Example {
                |  fun log() {
                |     val e = Exception()
                |     Timber.d("%s", e)
                |  }
                |}""".trimMargin())
        )
        .issues(*issues)
        .run()
        .expect("""
            |src/foo/Example.java:6: Warning: Throwable should be first argument [ThrowableNotAtBeginning]
            |     Timber.d("%s", e);
            |     ~~~~~~~~~~~~~~~~~
            |src/foo/Example.kt:6: Warning: Throwable should be first argument [ThrowableNotAtBeginning]
            |     Timber.d("%s", e)
            |     ~~~~~~~~~~~~~~~~~
            |0 errors, 2 warnings""".trimMargin())
        .expectFixDiffs("""
            |Fix for src/foo/Example.java line 6: Replace with e, "%s":
            |@@ -6 +6
            |-      Timber.d("%s", e);
            |+      Timber.d(e, "%s");
            |Fix for src/foo/Example.kt line 6: Replace with e, "%s":
            |@@ -6 +6
            |-      Timber.d("%s", e)
            |+      Timber.d(e, "%s")
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
                |}""".trimMargin()),
            kotlin("""
                |package foo
                |import timber.log.Timber
                |class Example {
                |  fun log() {
                |     Timber.d("foo" + "bar")
                |  }
                |}""".trimMargin())
        )
        .issues(*issues)
        .run()
        .expectClean()
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
                |}""".trimMargin()),
          kotlin("""
                |package foo
                |import timber.log.Timber
                |class Example {
                |  fun log() {
                |     val foo = "foo"
                |     Timber.d("${"$"}{foo}bar")
                |  }
                |}""".trimMargin())
        )
        .issues(*issues)
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
                |}""".trimMargin()),
            kotlin("""
                |package foo
                |import timber.log.Timber
                |class Example {
                |  fun log() {
                |     val bar = "bar"
                |     Timber.d("foo${"$"}bar")
                |  }
                |}""".trimMargin())
        )
        .issues(*issues)
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
                |}""".trimMargin()),
          kotlin("""
                |package foo
                |import timber.log.Timber
                |class Example {
                |  fun log() {
                |     val foo = "foo"
                |     val bar = "bar"
                |     Timber.d("${"$"}foo${"$"}bar")
                |  }
                |}""".trimMargin())
        )
        .issues(*issues)
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
                |}""".trimMargin()),
          kotlin("""
                |package foo
                |import timber.log.Timber
                |class Example {
                |  fun log() {
                |     val s = "world!"
                |     Timber.d(if(true) "Hello, ${"$"}s" else "Bye")
                |  }
                |}""".trimMargin())
        )
        .issues(*issues)
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
                |}""".trimMargin()),
            kotlin("""
                |package foo
                |import timber.log.Timber
                |class Example {
                |  fun log() {
                |     Timber.d("%s %s", "arg1")
                |  }
                |}""".trimMargin())
        )
        .issues(*issues)
        .run()
        .expect("""
            |src/foo/Example.java:5: Error: Wrong argument count, format string %s %s requires 2 but format call supplies 1 [TimberArgCount]
            |     Timber.d("%s %s", "arg1");
            |     ~~~~~~~~~~~~~~~~~~~~~~~~~
            |src/foo/Example.kt:5: Error: Wrong argument count, format string %s %s requires 2 but format call supplies 1 [TimberArgCount]
            |     Timber.d("%s %s", "arg1")
            |     ~~~~~~~~~~~~~~~~~~~~~~~~~
            |2 errors, 0 warnings""".trimMargin())
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
                |}""".trimMargin()),
            kotlin("""
                |package foo
                |import timber.log.Timber
                |class Example {
                |  fun log() {
                |     Timber.d("%s", "arg1", "arg2")
                |  }
                |}""".trimMargin())
        )
        .issues(*issues)
        .run()
        .expect("""
            |src/foo/Example.java:5: Error: Wrong argument count, format string %s requires 1 but format call supplies 2 [TimberArgCount]
            |     Timber.d("%s", "arg1", "arg2");
            |     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            |src/foo/Example.kt:5: Error: Wrong argument count, format string %s requires 1 but format call supplies 2 [TimberArgCount]
            |     Timber.d("%s", "arg1", "arg2")
            |     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            |2 errors, 0 warnings""".trimMargin())
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
                |}""".trimMargin()),
            kotlin("""
                |package foo
                |import timber.log.Timber
                |class Example {
                |  fun log() {
                |     Timber.d("%d", "arg1")
                |  }
                |}""".trimMargin())
        )
        .issues(*issues)
        .run()
        .expect("""
            |src/foo/Example.java:5: Error: Wrong argument type for formatting argument '#1' in %d: conversion is 'd', received String (argument #2 in method call) [TimberArgTypes]
            |     Timber.d("%d", "arg1");
            |                    ~~~~~~
            |src/foo/Example.kt:5: Error: Wrong argument type for formatting argument '#1' in %d: conversion is 'd', received String (argument #2 in method call) [TimberArgTypes]
            |     Timber.d("%d", "arg1")
            |                     ~~~~
            |2 errors, 0 warnings""".trimMargin())
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
                |}""".trimMargin()),
            kotlin("""
                |package foo
                |import timber.log.Timber
                |class Example {
                |  fun log() {
                |     Timber.tag("abcdefghijklmnopqrstuvwx")
                |  }
                |}""".trimMargin()),
                manifest().minSdk(25)
        )
        .issues(*issues)
        .run()
        .expect("""
            |src/foo/Example.java:5: Error: The logging tag can be at most 23 characters, was 24 (abcdefghijklmnopqrstuvwx) [TimberTagLength]
            |     Timber.tag("abcdefghijklmnopqrstuvwx");
            |                ~~~~~~~~~~~~~~~~~~~~~~~~~~
            |1 errors, 0 warnings""".trimMargin())
  }

  @Test fun tagTooLongLiteralOnlyBeforeApi26() {
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
            kotlin("""
                |package foo
                |import timber.log.Timber
                |class Example {
                |  fun log() {
                |     Timber.tag("abcdefghijklmnopqrstuvwx")
                |  }
                |}""".trimMargin()),
            manifest().minSdk(26)
        )
        .issues(*issues)
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
                |}""".trimMargin()),
            kotlin("""
                |package foo
                |import timber.log.Timber
                |class Example {
                |  fun log() {
                |     Timber.tag("tag").d("%s %s", "arg1")
                |  }
                |}""".trimMargin())
        )
        .issues(*issues)
        .run()
        .expect("""
            |src/foo/Example.java:5: Error: Wrong argument count, format string %s %s requires 2 but format call supplies 1 [TimberArgCount]
            |     Timber.tag("tag").d("%s %s", "arg1");
            |     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            |src/foo/Example.kt:5: Error: Wrong argument count, format string %s %s requires 2 but format call supplies 1 [TimberArgCount]
            |     Timber.tag("tag").d("%s %s", "arg1")
            |     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            |2 errors, 0 warnings""".trimMargin())
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
                |}""".trimMargin()),
            kotlin("""
                |package foo
                |import timber.log.Timber
                |class Example {
                |  fun log() {
                |     Timber.tag("tag").d("%s", "arg1", "arg2")
                |  }
                |}""".trimMargin())
        )
        .issues(*issues)
        .run()
        .expect("""
            |src/foo/Example.java:5: Error: Wrong argument count, format string %s requires 1 but format call supplies 2 [TimberArgCount]
            |     Timber.tag("tag").d("%s", "arg1", "arg2");
            |     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            |src/foo/Example.kt:5: Error: Wrong argument count, format string %s requires 1 but format call supplies 2 [TimberArgCount]
            |     Timber.tag("tag").d("%s", "arg1", "arg2")
            |     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            |2 errors, 0 warnings""".trimMargin())
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
                |}""".trimMargin()),
            kotlin("""
                |package foo
                |import timber.log.Timber
                |class Example {
                |  fun log() {
                |     Timber.tag("tag").d("%d", "arg1")
                |  }
                |}""".trimMargin())
        )
        .issues(*issues)
        .run()
        .expect("""
            |src/foo/Example.java:5: Error: Wrong argument type for formatting argument '#1' in %d: conversion is 'd', received String (argument #2 in method call) [TimberArgTypes]
            |     Timber.tag("tag").d("%d", "arg1");
            |                               ~~~~~~
            |src/foo/Example.kt:5: Error: Wrong argument type for formatting argument '#1' in %d: conversion is 'd', received String (argument #2 in method call) [TimberArgTypes]
            |     Timber.tag("tag").d("%d", "arg1")
            |                                ~~~~
            |2 errors, 0 warnings""".trimMargin())
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
                |}""".trimMargin()),
            kotlin("""
                |package foo
                |import timber.log.Timber
                |class Example {
                |  fun log() {
                |     val e = Exception()
                |     Timber.d(e.message)
                |  }
                |}""".trimMargin())
        )
        .issues(*issues)
        .run()
        .expect("""
            |src/foo/Example.java:6: Warning: Explicitly logging exception message is redundant [TimberExceptionLogging]
            |     Timber.d(e.getMessage());
            |     ~~~~~~~~~~~~~~~~~~~~~~~~
            |src/foo/Example.kt:6: Warning: Explicitly logging exception message is redundant [TimberExceptionLogging]
            |     Timber.d(e.message)
            |     ~~~~~~~~~~~~~~~~~~~
            |0 errors, 2 warnings""".trimMargin())
        .expectFixDiffs("""
            |Fix for src/foo/Example.java line 6: Replace message with throwable:
            |@@ -6 +6
            |-      Timber.d(e.getMessage());
            |+      Timber.d(e);
            |Fix for src/foo/Example.kt line 6: Replace message with throwable:
            |@@ -6 +6
            |-      Timber.d(e.message)
            |+      Timber.d(e)
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
                |}""".trimMargin()),
            kotlin("""
                |package foo
                |import timber.log.Timber
                |class Example {
                |  fun log() {
                |     val e = Exception()
                |     Timber.d(e, e.message)
                |  }
                |}""".trimMargin())
        )
        .issues(*issues)
        .run()
        .expect("""
            |src/foo/Example.java:6: Warning: Explicitly logging exception message is redundant [TimberExceptionLogging]
            |     Timber.d(e, e.getMessage());
            |     ~~~~~~~~~~~~~~~~~~~~~~~~~~~
            |src/foo/Example.kt:6: Warning: Explicitly logging exception message is redundant [TimberExceptionLogging]
            |     Timber.d(e, e.message)
            |     ~~~~~~~~~~~~~~~~~~~~~~
            |0 errors, 2 warnings""".trimMargin())
        .expectFixDiffs("""
            |Fix for src/foo/Example.java line 5: Remove redundant argument:
            |@@ -6 +6
            |-      Timber.d(e, e.getMessage());
            |+      Timber.d(e);
            |Fix for src/foo/Example.kt line 5: Remove redundant argument:
            |@@ -6 +6
            |-      Timber.d(e, e.message)
            |+      Timber.d(e)
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
                |}""".trimMargin()),
            kotlin("""
                |package foo
                |import timber.log.Timber
                |class Example {
                |  fun log() {
                |     val msg = "Hello"
                |     val e = Exception()
                |     Timber.d(e, msg)  
                |  }
                |}""".trimMargin())
        )
        .issues(*issues)
        .run()
        .expectClean()
  }

  @Test fun exceptionLoggingUsingParameter() {
    lint()
        .files(TIMBER_STUB,
            java("""
                |package foo;
                |import timber.log.Timber;
                |public class Example {
                |  public void log(Exception e, String message) {
                |     Timber.d(e, message);
                |  }
                |}""".trimMargin()),
            kotlin("""
                |package foo
                |import timber.log.Timber
                |class Example {
                |  fun log(e: Exception, message: String) {
                |     Timber.d(e, message)
                |  }
                |}""".trimMargin())
        )
        .issues(*issues)
        .run()
        .expectClean()
  }

  @Test fun exceptionLoggingUsingMethod() {
    lint()
        .files(TIMBER_STUB,
            java("""
                |package foo;
                |import timber.log.Timber;
                |public class Example {
                |  public void log(Exception e) {
                |    Timber.d(e, method());
                |  }
                |  private String method() {
                |    return "foo";
                |  }
                |}""".trimMargin()),
            kotlin("""
                |package foo
                |import timber.log.Timber
                |class Example {
                |  fun log(e: Exception) {
                |     Timber.d(e, method())
                |  }
                |  private fun method(): String {
                |     return "foo"
                |  }
                |}""".trimMargin())
        )
        .issues(*issues)
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
                |}""".trimMargin()),
            kotlin("""
                |package foo
                |import timber.log.Timber
                |class Example {
                |  private var message = ""
                |  fun log() {
                |     val e = Exception()
                |     Timber.d(e, message)
                |  }
                |}""".trimMargin())
        )
        .issues(*issues)
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
                |}""".trimMargin()),
            kotlin("""
                |package foo
                |import timber.log.Timber
                |class Example {
                |  private val message = ""
                |  fun log() {
                |     val e = Exception()
                |     Timber.d(e, message)
                |  }
                |}""".trimMargin())
        )
        .issues(*issues)
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
                |}""".trimMargin()),
            kotlin("""
                |package foo
                |import timber.log.Timber
                |class Example {
                |  fun log() {
                |     val e = Exception()
                |     Timber.d(e, "")
                |  }
                |}""".trimMargin())
        )
        .issues(*issues)
        .run()
        .expect("""
            |src/foo/Example.java:6: Warning: Use single-argument log method instead of null/empty message [TimberExceptionLogging]
            |     Timber.d(e, "");
            |     ~~~~~~~~~~~~~~~
            |src/foo/Example.kt:6: Warning: Use single-argument log method instead of null/empty message [TimberExceptionLogging]
            |     Timber.d(e, "")
            |     ~~~~~~~~~~~~~~~
            |0 errors, 2 warnings""".trimMargin())
        .expectFixDiffs("""
            |Fix for src/foo/Example.java line 6: Remove redundant argument:
            |@@ -6 +6
            |-      Timber.d(e, "");
            |+      Timber.d(e);
            |Fix for src/foo/Example.kt line 6: Remove redundant argument:
            |@@ -6 +6
            |-      Timber.d(e, "")
            |+      Timber.d(e)
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
                |}""".trimMargin()),
            kotlin("""
                |package foo
                |import timber.log.Timber
                |class Example {
                |  fun log() {
                |     val e = Exception()
                |     Timber.d(e, null)
                |  }
                |}""".trimMargin())
        )
        .issues(*issues)
        .run()
        .expect("""
            |src/foo/Example.java:6: Warning: Use single-argument log method instead of null/empty message [TimberExceptionLogging]
            |     Timber.d(e, null);
            |     ~~~~~~~~~~~~~~~~~
            |src/foo/Example.kt:6: Warning: Use single-argument log method instead of null/empty message [TimberExceptionLogging]
            |     Timber.d(e, null)
            |     ~~~~~~~~~~~~~~~~~
            |0 errors, 2 warnings""".trimMargin())
        .expectFixDiffs("""
            |Fix for src/foo/Example.java line 6: Remove redundant argument:
            |@@ -6 +6
            |-      Timber.d(e, null);
            |+      Timber.d(e);
            |Fix for src/foo/Example.kt line 6: Remove redundant argument:
            |@@ -6 +6
            |-      Timber.d(e, null)
            |+      Timber.d(e)
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
                |}""".trimMargin()),
            kotlin("""
                |package foo
                |import timber.log.Timber
                |class Example {
                |  fun log() {
                |     val e = Exception()
                |     Timber.d(e, "Valid message")
                |  }
                |}""".trimMargin())
        )
        .issues(*issues)
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
                |}""".trimMargin()),
            kotlin("""
                |package foo
                |import timber.log.Timber
                |class Example {
                |  fun log() {
                |    Timber.d("%tc", java.util.Date())
                |  }
                |}""".trimMargin())
        )
        .issues(*issues)
        .run()
        .expectClean()
  }

  @Test fun systemTimeMillisValidMessage() {
    lint()
        .files(TIMBER_STUB,
            java("""
                |package foo;
                |import timber.log.Timber;
                |public class Example {
                |  public void log() {
                |    Timber.d("%tc", System.currentTimeMillis());
                |  }
                |}""".trimMargin()),
            kotlin("""
                |package foo
                |import timber.log.Timber
                |class Example {
                |  fun log() {
                |    Timber.d("%tc", System.currentTimeMillis())
                |  }
                |}""".trimMargin())
        )
        .issues(*issues)
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
                |}""".trimMargin()),
            // no kotlin equivalent, since primitive wrappers do not exist
        )
        .issues(*issues)
        .run()
        .expectClean()
  }

  @Test fun memberVariable() {
    lint()
        .files(TIMBER_STUB,
            java("""
                |package foo;
                |import timber.log.Timber;
                |public class Example {
                |  public static class Bar {
                |    public static String baz = "timber";
                |  }
                |  public void log() {
                |    Bar bar = new Bar();
                |    Timber.d(bar.baz);
                |  }
                |}
                """.trimMargin()),
            kotlin("""
                |package foo
                |import timber.log.Timber
                |class Example {
                |  class Bar {
                |    val baz = "timber"
                |  }
                |  fun log() {
                |    val bar = Bar()
                |    Timber.d(bar.baz)
                |  }
                |}
                """.trimMargin())
        )
        .issues(*issues)
        .run()
        .expectClean()
  }
}
