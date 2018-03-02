package timber.lint;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.tools.lint.checks.StringFormatDetector;
import com.android.tools.lint.client.api.JavaEvaluator;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.LintFix;
import com.android.tools.lint.detector.api.LintUtils;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.uast.UBinaryExpression;
import org.jetbrains.uast.UCallExpression;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UExpression;
import org.jetbrains.uast.UIfExpression;
import org.jetbrains.uast.UMethod;
import org.jetbrains.uast.UQualifiedReferenceExpression;
import org.jetbrains.uast.USimpleNameReferenceExpression;
import org.jetbrains.uast.UastBinaryOperator;
import org.jetbrains.uast.util.UastExpressionUtils;

import static com.android.tools.lint.client.api.JavaEvaluatorKt.TYPE_BOOLEAN;
import static com.android.tools.lint.client.api.JavaEvaluatorKt.TYPE_BYTE;
import static com.android.tools.lint.client.api.JavaEvaluatorKt.TYPE_CHAR;
import static com.android.tools.lint.client.api.JavaEvaluatorKt.TYPE_DOUBLE;
import static com.android.tools.lint.client.api.JavaEvaluatorKt.TYPE_FLOAT;
import static com.android.tools.lint.client.api.JavaEvaluatorKt.TYPE_INT;
import static com.android.tools.lint.client.api.JavaEvaluatorKt.TYPE_LONG;
import static com.android.tools.lint.client.api.JavaEvaluatorKt.TYPE_NULL;
import static com.android.tools.lint.client.api.JavaEvaluatorKt.TYPE_OBJECT;
import static com.android.tools.lint.client.api.JavaEvaluatorKt.TYPE_SHORT;
import static com.android.tools.lint.client.api.JavaEvaluatorKt.TYPE_STRING;
import static com.android.tools.lint.detector.api.ConstantEvaluator.evaluateString;
import static org.jetbrains.uast.UastBinaryOperator.PLUS;
import static org.jetbrains.uast.UastBinaryOperator.PLUS_ASSIGN;
import static org.jetbrains.uast.UastLiteralUtils.isStringLiteral;
import static org.jetbrains.uast.UastUtils.evaluateString;

public final class WrongTimberUsageDetector extends Detector implements Detector.UastScanner {
  private final static String GET_STRING_METHOD = "getString";
  private final static String TIMBER_TREE_LOG_METHOD_REGEXP = "(v|d|i|w|e|wtf)";

  @Override public List<String> getApplicableMethodNames() {
    return Arrays.asList("tag", "format", "v", "d", "i", "w", "e", "wtf");
  }

  @Override public void visitMethod(JavaContext context, UCallExpression call, PsiMethod method) {
    String methodName = call.getMethodName();
    JavaEvaluator evaluator = context.getEvaluator();

    if ("format".equals(methodName) && evaluator.isMemberInClass(method, "java.lang.String")) {
      checkNestedStringFormat(context, call);
      return;
    }
    // As of API 24, Log tags are no longer limited to 23 chars.
    if ("tag".equals(methodName)
        && evaluator.isMemberInClass(method, "timber.log.Timber")
        && context.getMainProject().getMinSdk() <= 23) {
      checkTagLength(context, call);
      return;
    }
    if (evaluator.isMemberInClass(method, "android.util.Log")) {
      LintFix fix = quickFixIssueLog(call);
      context.report(ISSUE_LOG, call, context.getLocation(call), "Using 'Log' instead of 'Timber'",
          fix);
      return;
    }
    // Handles Timber.X(..) and Timber.tag(..).X(..) where X in (v|d|i|w|e|wtf).
    if (evaluator.isMemberInClass(method, "timber.log.Timber") //
        || evaluator.isMemberInClass(method, "timber.log.Timber.Tree")) {
      checkMethodArguments(context, call);
      checkFormatArguments(context, call);
      checkExceptionLogging(context, call);
    }
  }

  private void checkNestedStringFormat(JavaContext context, UCallExpression call) {
    UElement current = call;
    while (true) {
      current = LintUtils.skipParentheses(current.getUastParent());
      if (current == null || current instanceof UMethod) {
        // Reached AST root or code block node; String.format not inside Timber.X(..).
        return;
      }
      if (UastExpressionUtils.isMethodCall(current)) {
        UCallExpression maybeTimberLogCall = (UCallExpression) current;
        JavaEvaluator evaluator = context.getEvaluator();
        PsiMethod psiMethod = maybeTimberLogCall.resolve();
        if (Pattern.matches(TIMBER_TREE_LOG_METHOD_REGEXP, psiMethod.getName())
            && evaluator.isMemberInClass(psiMethod, "timber.log.Timber")) {
          LintFix fix = quickFixIssueFormat(call);
          context.report(ISSUE_FORMAT, call, context.getLocation(call),
              "Using 'String#format' inside of 'Timber'", fix);
          return;
        }
      }
    }
  }

  private void checkTagLength(JavaContext context, UCallExpression call) {
    List<UExpression> arguments = call.getValueArguments();
    UExpression argument = arguments.get(0);
    String tag = evaluateString(context, argument, true);
    if (tag != null && tag.length() > 23) {
      String message =
          String.format("The logging tag can be at most 23 characters, was %1$d (%2$s)",
              tag.length(), tag);
      LintFix fix = quickFixIssueTagLength(argument, tag);
      context.report(ISSUE_TAG_LENGTH, argument, context.getLocation(argument), message, fix);
    }
  }

  private static void checkFormatArguments(JavaContext context, UCallExpression call) {
    List<UExpression> arguments = call.getValueArguments();
    int numArguments = arguments.size();
    if (numArguments == 0) {
      return;
    }

    int startIndexOfArguments = 1;
    UExpression formatStringArg = arguments.get(0);
    if (isSubclassOf(context, formatStringArg, Throwable.class)) {
      if (numArguments == 1) {
        return;
      }
      formatStringArg = arguments.get(1);
      startIndexOfArguments++;
    }

    String formatString = evaluateString(context, formatStringArg, true);
    // We passed for example a method call
    if (formatString == null) {
      return;
    }

    int formatArgumentCount = getFormatArgumentCount(formatString);
    int passedArgCount = numArguments - startIndexOfArguments;
    if (formatArgumentCount < passedArgCount) {
      context.report(ISSUE_ARG_COUNT, call, context.getLocation(call), String.format(
          "Wrong argument count, format string `%1$s` requires "
              + "`%2$d` but format call supplies `%3$d`", formatString, formatArgumentCount,
          passedArgCount));
      return;
    }

    if (formatArgumentCount == 0) {
      return;
    }

    List<String> types = getStringArgumentTypes(formatString);
    UExpression argument = null;
    int argumentIndex = startIndexOfArguments;
    boolean valid;
    for (int i = 0; i < types.size(); i++) {
      String formatType = types.get(i);
      if (argumentIndex != numArguments) {
        argument = arguments.get(argumentIndex++);
      } else {
        context.report(ISSUE_ARG_COUNT, call, context.getLocation(call), String.format(
            "Wrong argument count, format string `%1$s` requires "
                + "`%2$d` but format call supplies `%3$d`", formatString, formatArgumentCount,
            passedArgCount));
      }

      Class type = getType(argument);
      if (type == null) {
        continue;
      }

      char last = formatType.charAt(formatType.length() - 1);
      if (formatType.length() >= 2
          && Character.toLowerCase(formatType.charAt(formatType.length() - 2)) == 't') {
        // Date time conversion.
        switch (last) {
          // time
          case 'H':
          case 'I':
          case 'k':
          case 'l':
          case 'M':
          case 'S':
          case 'L':
          case 'N':
          case 'p':
          case 'z':
          case 'Z':
          case 's':
          case 'Q':
            // date
          case 'B':
          case 'b':
          case 'h':
          case 'A':
          case 'a':
          case 'C':
          case 'Y':
          case 'y':
          case 'j':
          case 'm':
          case 'd':
          case 'e':
            // date/time
          case 'R':
          case 'T':
          case 'r':
          case 'D':
          case 'F':
          case 'c':
            valid = type == Integer.TYPE || type == Calendar.class || type == Date.class;
            if (!valid) {
              String message = String.format(
                  "Wrong argument type for date formatting argument '#%1$d' "
                      + "in `%2$s`: conversion is '`%3$s`', received `%4$s` "
                      + "(argument #%5$d in method call)", i + 1, formatString, formatType,
                  type.getSimpleName(), startIndexOfArguments + i + 1);
              context.report(ISSUE_ARG_TYPES, call, context.getLocation(argument), message);
            }
            break;
          default:
            String message = String.format("Wrong suffix for date format '#%1$d' "
                    + "in `%2$s`: conversion is '`%3$s`', received `%4$s` "
                    + "(argument #%5$d in method call)", i + 1, formatString, formatType,
                type.getSimpleName(), startIndexOfArguments + i + 1);
            context.report(ISSUE_FORMAT, call, context.getLocation(argument), message);
        }
        continue;
      }
      switch (last) {
        case 'b':
        case 'B':
          valid = type == Boolean.TYPE;
          break;
        case 'x':
        case 'X':
        case 'd':
        case 'o':
        case 'e':
        case 'E':
        case 'f':
        case 'g':
        case 'G':
        case 'a':
        case 'A':
          valid = type == Integer.TYPE
              || type == Float.TYPE
              || type == Double.TYPE
              || type == Long.TYPE
              || type == Byte.TYPE
              || type == Short.TYPE;
          break;
        case 'c':
        case 'C':
          valid = type == Character.TYPE;
          break;
        case 'h':
        case 'H':
          valid = type != Boolean.TYPE && !Number.class.isAssignableFrom(type);
          break;
        case 's':
        case 'S':
        default:
          valid = true;
      }
      if (!valid) {
        String message = String.format("Wrong argument type for formatting argument '#%1$d' "
                + "in `%2$s`: conversion is '`%3$s`', received `%4$s` "
                + "(argument #%5$d in method call)", i + 1, formatString, formatType,
            type.getSimpleName(), startIndexOfArguments + i + 1);
        context.report(ISSUE_ARG_TYPES, call, context.getLocation(argument), message);
      }
    }
  }

  private static Class<?> getType(UExpression expression) {
    if (expression == null) {
      return null;
    }
    if (expression instanceof PsiMethodCallExpression) {
      PsiMethodCallExpression call = (PsiMethodCallExpression) expression;
      PsiMethod method = call.resolveMethod();
      if (method == null) {
        return null;
      }
      String methodName = method.getName();
      if (methodName.equals(GET_STRING_METHOD)) {
        return String.class;
      }
    } else if (expression instanceof PsiLiteralExpression) {
      PsiLiteralExpression literalExpression = (PsiLiteralExpression) expression;
      PsiType expressionType = literalExpression.getType();
      if (LintUtils.isString(expressionType)) {
        return String.class;
      } else if (expressionType == PsiType.INT) {
        return Integer.TYPE;
      } else if (expressionType == PsiType.FLOAT) {
        return Float.TYPE;
      } else if (expressionType == PsiType.CHAR) {
        return Character.TYPE;
      } else if (expressionType == PsiType.BOOLEAN) {
        return Boolean.TYPE;
      } else if (expressionType == PsiType.NULL) {
        return Object.class;
      }
    }

    PsiType type = expression.getExpressionType();
    if (type != null) {
      Class<?> typeClass = getTypeClass(type);
      return typeClass != null ? typeClass : Object.class;
    }

    return null;
  }

  private static Class<?> getTypeClass(@Nullable PsiType type) {
    if (type != null) {
      return getTypeClass(type.getCanonicalText());
    }
    return null;
  }

  private static Class<?> getTypeClass(@Nullable String typeClassName) {
    if (typeClassName == null) {
      return null;
    } else if (typeClassName.equals(TYPE_STRING) || "String".equals(typeClassName)) {
      return String.class;
    } else if (typeClassName.equals(TYPE_INT)) {
      return Integer.TYPE;
    } else if (typeClassName.equals(TYPE_BOOLEAN)) {
      return Boolean.TYPE;
    } else if (typeClassName.equals(TYPE_NULL)) {
      return Object.class;
    } else if (typeClassName.equals(TYPE_LONG)) {
      return Long.TYPE;
    } else if (typeClassName.equals(TYPE_FLOAT)) {
      return Float.TYPE;
    } else if (typeClassName.equals(TYPE_DOUBLE)) {
      return Double.TYPE;
    } else if (typeClassName.equals(TYPE_CHAR)) {
      return Character.TYPE;
    } else if ("BigDecimal".equals(typeClassName) || "java.math.BigDecimal".equals(typeClassName)) {
      return Float.TYPE;
    } else if ("BigInteger".equals(typeClassName) || "java.math.BigInteger".equals(typeClassName)) {
      return Integer.TYPE;
    } else if (typeClassName.equals(TYPE_OBJECT)) {
      return null;
    } else if (typeClassName.startsWith("java.lang.")) {
      if ("java.lang.Integer".equals(typeClassName)
          || "java.lang.Short".equals(typeClassName)
          || "java.lang.Byte".equals(typeClassName)
          || "java.lang.Long".equals(typeClassName)) {
        return Integer.TYPE;
      } else if ("java.lang.Float".equals(typeClassName) || "java.lang.Double".equals(
          typeClassName)) {
        return Float.TYPE;
      } else if ("java.lang.Boolean".equals(typeClassName)) {
        return Boolean.TYPE;
      } else {
        return null;
      }
    } else if (typeClassName.equals(TYPE_BYTE)) {
      return Byte.TYPE;
    } else if (typeClassName.equals(TYPE_SHORT)) {
      return Short.TYPE;
    } else if ("Date".equals(typeClassName) || "java.util.Date".equals(typeClassName)) {
      return Date.class;
    } else if ("Calendar".equals(typeClassName) || "java.util.Calendar".equals(typeClassName)) {
      return Calendar.class;
    } else {
      return null;
    }
  }

  private static boolean isSubclassOf(JavaContext context, UExpression expression, Class<?> cls) {
    PsiType expressionType = expression.getExpressionType();
    if (expressionType instanceof PsiClassType) {
      PsiClassType classType = (PsiClassType) expressionType;
      PsiClass resolvedClass = classType.resolve();
      return context.getEvaluator().extendsClass(resolvedClass, cls.getName(), false);
    }
    return false;
  }

  private static List<String> getStringArgumentTypes(String formatString) {
    List<String> types = new ArrayList<>();
    Matcher matcher = StringFormatDetector.FORMAT.matcher(formatString);
    int index = 0;
    int prevIndex = 0;
    while (true) {
      if (matcher.find(index)) {
        int matchStart = matcher.start();
        while (prevIndex < matchStart) {
          char c = formatString.charAt(prevIndex);
          if (c == '\\') {
            prevIndex++;
          }
          prevIndex++;
        }
        if (prevIndex > matchStart) {
          index = prevIndex;
          continue;
        }

        index = matcher.end();
        String str = formatString.substring(matchStart, matcher.end());
        if ("%%".equals(str) || "%n".equals(str)) {
          continue;
        }
        String time = matcher.group(5);
        if ("t".equalsIgnoreCase(time)) {
          types.add(time + matcher.group(6));
        } else {
          types.add(matcher.group(6));
        }
      } else {
        break;
      }
    }
    return types;
  }

  private static int getFormatArgumentCount(@NonNull String s) {
    Matcher matcher = StringFormatDetector.FORMAT.matcher(s);
    int index = 0;
    int prevIndex = 0;
    int nextNumber = 1;
    int max = 0;
    while (true) {
      if (matcher.find(index)) {
        String value = matcher.group(6);
        if ("%".equals(value) || "n".equals(value)) {
          index = matcher.end();
          continue;
        }
        int matchStart = matcher.start();
        for (; prevIndex < matchStart; prevIndex++) {
          char c = s.charAt(prevIndex);
          if (c == '\\') {
            prevIndex++;
          }
        }
        if (prevIndex > matchStart) {
          index = prevIndex;
          continue;
        }

        int number;
        String numberString = matcher.group(1);
        if (numberString != null) {
          // Strip off trailing $
          numberString = numberString.substring(0, numberString.length() - 1);
          number = Integer.parseInt(numberString);
          nextNumber = number + 1;
        } else {
          number = nextNumber++;
        }
        if (number > max) {
          max = number;
        }
        index = matcher.end();
      } else {
        break;
      }
    }

    return max;
  }

  private void checkMethodArguments(JavaContext context, UCallExpression call) {
    List<UExpression> arguments = call.getValueArguments();
    int numArguments = arguments.size();
    for (int i = 0; i < numArguments; i++) {
      UExpression argument = arguments.get(i);
      if (checkElement(context, call, argument)) {
        break;
      }
      if (i > 0 && isSubclassOf(context, argument, Throwable.class)) {
        LintFix fix = quickFixIssueThrowable(call, arguments, argument);
        context.report(ISSUE_THROWABLE, call, context.getLocation(call),
            "Throwable should be first argument", fix);
      }
    }
  }

  private void checkExceptionLogging(JavaContext context, UCallExpression call) {
    List<UExpression> arguments = call.getValueArguments();
    int numArguments = arguments.size();

    if (numArguments > 1 && isSubclassOf(context, arguments.get(0), Throwable.class)) {
      UExpression messageArg = arguments.get(1);

      if (isLoggingExceptionMessage(context, messageArg)) {
        context.report(ISSUE_EXCEPTION_LOGGING, messageArg, context.getLocation(call),
            "Explicitly logging exception message is redundant",
            quickFixRemoveRedundantArgument(messageArg));
        return;
      }

      String s = evaluateString(context, messageArg, true);
      if (s == null && isField(messageArg)) {
        // Non-final fields can't be evaluated.
        return;
      }

      if (s == null || s.isEmpty()) {
        LintFix fix = quickFixRemoveRedundantArgument(messageArg);
        context.report(ISSUE_EXCEPTION_LOGGING, messageArg, context.getLocation(call),
            "Use single-argument log method instead of null/empty message", fix);
      }
    } else if (numArguments == 1 && !isSubclassOf(context, arguments.get(0), Throwable.class)) {
      UExpression messageArg = arguments.get(0);

      if (isLoggingExceptionMessage(context, messageArg)) {
        context.report(ISSUE_EXCEPTION_LOGGING, messageArg, context.getLocation(call),
            "Explicitly logging exception message is redundant",
            quickFixReplaceMessageWithThrowable(messageArg));
      }
    }
  }

  private boolean isLoggingExceptionMessage(JavaContext context, UExpression arg) {
    if (!(arg instanceof UQualifiedReferenceExpression)) {
      return false;
    }

    UQualifiedReferenceExpression argExpression = (UQualifiedReferenceExpression) arg;
    UExpression selector = argExpression.getSelector();

    // what other UExpressions could be a selector?
    return isCallFromMethodInSubclassOf(context, (UCallExpression) selector, "getMessage",
        "java.lang.Throwable");
  }

  private static boolean isField(UExpression expression) {
    return expression instanceof USimpleNameReferenceExpression
        && (((USimpleNameReferenceExpression) expression).resolve() instanceof PsiField);
  }

  private static boolean isCallFromMethodInSubclassOf(JavaContext context, UCallExpression call,
      String methodName, String className) {
    JavaEvaluator evaluator = context.getEvaluator();
    PsiMethod method = call.resolve();
    return method != null //
        && methodName.equals(call.getMethodName()) //
        && evaluator.isMemberInSubClassOf(method, className, false);
  }

  private boolean checkElement(JavaContext context, UCallExpression call, UElement element) {
    if (element instanceof UBinaryExpression) {
      UBinaryExpression binaryExpression = (UBinaryExpression) element;
      UastBinaryOperator operator = binaryExpression.getOperator();
      if (operator == PLUS || operator == PLUS_ASSIGN) {
        Class argumentType = getType(binaryExpression);
        if (argumentType == String.class) {
          LintFix fix = quickFixIssueBinary(binaryExpression);
          context.report(ISSUE_BINARY, call, context.getLocation(element),
              "Replace String concatenation with Timber's string formatting", fix);
          return true;
        }
      }
    } else if (element instanceof UIfExpression) {
      return checkConditionalUsage(context, call, element);
    }
    return false;
  }

  private boolean checkConditionalUsage(JavaContext context, UCallExpression call,
      UElement element) {
    UElement thenElement;
    UElement elseElement;
    if (element instanceof UIfExpression) {
      UIfExpression ifArg = (UIfExpression) element;
      thenElement = ifArg.getThenExpression();
      elseElement = ifArg.getElseExpression();
    } else {
      return false;
    }
    if (checkElement(context, call, thenElement)) {
      return false;
    }
    return checkElement(context, call, elseElement);
  }

  private LintFix quickFixIssueLog(UCallExpression logCall) {
    List<UExpression> arguments = logCall.getValueArguments();
    String methodName = logCall.getMethodName();
    UExpression tag = arguments.get(0);

    // 1st suggestion respects author's tag preference.
    // 2nd suggestion drops it (Timber defaults to calling class name).
    String fixSource1 = "Timber.tag(" + tag.asSourceString() + ").";
    String fixSource2 = "Timber.";

    int numArguments = arguments.size();
    if (numArguments == 2) {
      UExpression msgOrThrowable = arguments.get(1);
      fixSource1 += methodName + "(" + msgOrThrowable.asSourceString() + ")";
      fixSource2 += methodName + "(" + msgOrThrowable.asSourceString() + ")";
    } else if (numArguments == 3) {
      UExpression msg = arguments.get(1);
      UExpression throwable = arguments.get(2);
      fixSource1 +=
          methodName + "(" + throwable.asSourceString() + ", " + msg.asSourceString() + ")";
      fixSource2 +=
          methodName + "(" + throwable.asSourceString() + ", " + msg.asSourceString() + ")";
    } else {
      throw new IllegalStateException("android.util.Log overloads should have 2 or 3 arguments");
    }

    String logCallSource = logCall.asSourceString();
    LintFix.GroupBuilder fixGrouper = fix().group();
    fixGrouper.add(
        fix().replace().text(logCallSource).shortenNames().reformat(true).with(fixSource1).build());
    fixGrouper.add(
        fix().replace().text(logCallSource).shortenNames().reformat(true).with(fixSource2).build());
    return fixGrouper.build();
  }

  private LintFix quickFixIssueFormat(UCallExpression stringFormatCall) {
    // Handles:
    // 1) String.format(..)
    // 2) format(...) [static import]
    UExpression callReceiver = stringFormatCall.getReceiver();
    String callSourceString = callReceiver == null ? "" : callReceiver.asSourceString() + ".";
    callSourceString += stringFormatCall.getMethodName();

    return fix().name("Remove String.format(...)").composite() //
        // Delete closing parenthesis of String.format(...)
        .add(fix().replace().pattern(callSourceString + "\\(.*(\\))").with("").build())
        // Delete "String.format("
        .add(fix().replace().text(callSourceString + "(").with("").build()).build();
  }

  private LintFix quickFixIssueThrowable(UCallExpression call, List<UExpression> arguments,
      UExpression throwable) {
    String rearrangedArgs = throwable.asSourceString();
    for (UExpression arg : arguments) {
      if (arg != throwable) {
        rearrangedArgs += (", " + arg.asSourceString());
      }
    }
    return fix().replace() //
        .pattern("\\." + call.getMethodName() + "\\((.*)\\)").with(rearrangedArgs).build();
  }

  private LintFix quickFixIssueBinary(UBinaryExpression binaryExpression) {
    UExpression leftOperand = binaryExpression.getLeftOperand();
    UExpression rightOperand = binaryExpression.getRightOperand();
    boolean isLeftLiteral = isStringLiteral(leftOperand);
    boolean isRightLiteral = isStringLiteral(rightOperand);

    // "a" + "b" => "ab"
    if (isLeftLiteral && isRightLiteral) {
      return fix().replace() //
          .text(binaryExpression.asSourceString())
          .with("\"" + evaluateString(binaryExpression) + "\"")
          .build();
    }

    String args;
    if (isLeftLiteral) {
      args = "\"" + evaluateString(leftOperand) + "%s\", " + rightOperand.asSourceString();
    } else if (isRightLiteral) {
      args = "\"%s" + evaluateString(rightOperand) + "\", " + leftOperand.asSourceString();
    } else {
      args = "\"%s%s\", " + leftOperand.asSourceString() + ", " + rightOperand.asSourceString();
    }
    return fix().replace().text(binaryExpression.asSourceString()).with(args).build();
  }

  private LintFix quickFixIssueTagLength(UExpression argument, String tag) {
    int numCharsToTrim = tag.length() - 23;
    return fix().replace()
        .name("Strip last " + (numCharsToTrim == 1 ? "char" : numCharsToTrim + " chars"))
        .text(argument.asSourceString())
        .with("\"" + tag.substring(0, 23) + "\"")
        .build();
  }

  private LintFix quickFixRemoveRedundantArgument(UExpression arg) {
    return fix().replace()
        .name("Remove redundant argument")
        .text(", " + arg.asSourceString())
        .with("")
        .build();
  }

  private LintFix quickFixReplaceMessageWithThrowable(UExpression arg) {
    // guaranteed based on callers of this method
    UQualifiedReferenceExpression argExpression = (UQualifiedReferenceExpression) arg;
    UExpression receiver = argExpression.getReceiver();

    return fix().replace()
        .name("Replace message with throwable")
        .text(arg.asSourceString())
        .with(receiver.asSourceString())
        .build();
  }

  static Issue[] getIssues() {
    return new Issue[] {
        ISSUE_LOG, ISSUE_FORMAT, ISSUE_THROWABLE, ISSUE_BINARY, ISSUE_ARG_COUNT, ISSUE_ARG_TYPES,
        ISSUE_TAG_LENGTH, ISSUE_EXCEPTION_LOGGING
    };
  }

  public static final Issue ISSUE_LOG =
      Issue.create("LogNotTimber", "Logging call to Log instead of Timber",
          "Since Timber is included in the project, it is likely that calls to Log should instead"
              + " be going to Timber.", Category.MESSAGES, 5, Severity.WARNING,
          new Implementation(WrongTimberUsageDetector.class, Scope.JAVA_FILE_SCOPE));
  public static final Issue ISSUE_FORMAT =
      Issue.create("StringFormatInTimber", "Logging call with Timber contains String#format()",
          "Since Timber handles String.format automatically, you may not use String#format().",
          Category.MESSAGES, 5, Severity.WARNING,
          new Implementation(WrongTimberUsageDetector.class, Scope.JAVA_FILE_SCOPE));
  public static final Issue ISSUE_THROWABLE =
      Issue.create("ThrowableNotAtBeginning", "Exception in Timber not at the beginning",
          "In Timber you have to pass a Throwable at the beginning of the call.", Category.MESSAGES,
          5, Severity.WARNING,
          new Implementation(WrongTimberUsageDetector.class, Scope.JAVA_FILE_SCOPE));
  public static final Issue ISSUE_BINARY =
      Issue.create("BinaryOperationInTimber", "Use String#format()",
          "Since Timber handles String#format() automatically, use this instead of String"
              + " concatenation.", Category.MESSAGES, 5, Severity.WARNING,
          new Implementation(WrongTimberUsageDetector.class, Scope.JAVA_FILE_SCOPE));
  public static final Issue ISSUE_ARG_COUNT =
      Issue.create("TimberArgCount", "Formatting argument types incomplete or inconsistent",
          "When a formatted string takes arguments, you need to pass at least that amount of"
              + " arguments to the formatting call.", Category.MESSAGES, 9, Severity.ERROR,
          new Implementation(WrongTimberUsageDetector.class, Scope.JAVA_FILE_SCOPE));
  public static final Issue ISSUE_ARG_TYPES =
      Issue.create("TimberArgTypes", "Formatting string doesn't match passed arguments",
          "The argument types that you specified in your formatting string does not match the types"
              + " of the arguments that you passed to your formatting call.", Category.MESSAGES, 9,
          Severity.ERROR,
          new Implementation(WrongTimberUsageDetector.class, Scope.JAVA_FILE_SCOPE));
  public static final Issue ISSUE_TAG_LENGTH = Issue.create("TimberTagLength", "Too Long Log Tags",
      "Log tags are only allowed to be at most" + " 23 tag characters long.", Category.CORRECTNESS,
      5, Severity.ERROR, new Implementation(WrongTimberUsageDetector.class, Scope.JAVA_FILE_SCOPE));
  public static final Issue ISSUE_EXCEPTION_LOGGING =
      Issue.create("TimberExceptionLogging", "Exception Logging", "Explicitly including the"
              + " exception message is redundant when supplying an exception to log.",
          Category.CORRECTNESS, 3, Severity.WARNING,
          new Implementation(WrongTimberUsageDetector.class, Scope.JAVA_FILE_SCOPE));
}
