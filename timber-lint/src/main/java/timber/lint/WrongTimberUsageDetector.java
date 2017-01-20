package timber.lint;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.tools.lint.checks.StringFormatDetector;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.LintUtils;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.JavaTokenType;
import com.intellij.psi.PsiBinaryExpression;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiConditionalExpression;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiIfStatement;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.PsiType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.android.tools.lint.client.api.JavaParser.TYPE_BOOLEAN;
import static com.android.tools.lint.client.api.JavaParser.TYPE_BYTE;
import static com.android.tools.lint.client.api.JavaParser.TYPE_CHAR;
import static com.android.tools.lint.client.api.JavaParser.TYPE_DOUBLE;
import static com.android.tools.lint.client.api.JavaParser.TYPE_FLOAT;
import static com.android.tools.lint.client.api.JavaParser.TYPE_INT;
import static com.android.tools.lint.client.api.JavaParser.TYPE_LONG;
import static com.android.tools.lint.client.api.JavaParser.TYPE_NULL;
import static com.android.tools.lint.client.api.JavaParser.TYPE_OBJECT;
import static com.android.tools.lint.client.api.JavaParser.TYPE_SHORT;
import static com.android.tools.lint.client.api.JavaParser.TYPE_STRING;

public final class WrongTimberUsageDetector extends Detector implements Detector.JavaPsiScanner {
  private final static String GET_STRING_METHOD = "getString";
  private final static String TIMBER_TREE_LOG_METHOD_REGEXP = "(v|d|i|w|e|wtf)";

  @Override public List<String> getApplicableMethodNames() {
    return Arrays.asList("tag", "format", "v", "d", "i", "w", "e", "wtf");
  }

  @Override public void visitMethod(JavaContext context, JavaElementVisitor visitor,
      PsiMethodCallExpression call, PsiMethod method) {
    PsiReferenceExpression methodExpression = call.getMethodExpression();
    String fullyQualifiedMethodName = methodExpression.getQualifiedName();
    if ("java.lang.String.format".equals(fullyQualifiedMethodName)) {
      checkNestedStringFormat(context, call);
      return;
    }
    if (fullyQualifiedMethodName.startsWith("timber.log.Timber.tag")) {
      checkTagLength(context, call);
      return;
    }
    if (fullyQualifiedMethodName.startsWith("android.util.Log.")) {
      context.report(ISSUE_LOG, methodExpression, context.getLocation(methodExpression),
          "Using 'Log' instead of 'Timber'");
      return;
    }
    // Handles Timber.X(..) and Timber.tag(..).X(..) where X in (v|d|i|w|e|wtf).
    if (fullyQualifiedMethodName.startsWith("timber.log.Timber.")) {
      checkMethodArguments(context, call);
      checkFormatArguments(context, call);
      checkExceptionLogging(context, call);
      return;
    }
  }

  private static void checkNestedStringFormat(JavaContext context, PsiMethodCallExpression call) {
    PsiElement current = call;
    while (true) {
      current = LintUtils.skipParentheses(current.getParent());
      if (current == null || current instanceof PsiCodeBlock) {
        // Reached AST root or code block node; String.format not inside Timber.X(..).
        return;
      }
      if (current instanceof PsiMethodCallExpression) {
        PsiMethodCallExpression maybeTimberLog = (PsiMethodCallExpression) current;
        if (Pattern.matches("timber\\.log\\.Timber\\." + TIMBER_TREE_LOG_METHOD_REGEXP,
            maybeTimberLog.getMethodExpression().getQualifiedName())) {
          context.report(ISSUE_FORMAT, call, context.getLocation(call),
              "Using 'String#format' inside of 'Timber'");
          return;
        }
      }
    }
  }

  private static void checkTagLength(JavaContext context, PsiMethodCallExpression call) {
    PsiExpression argument = call.getArgumentList().getExpressions()[0];
    String tag = findLiteralValue(argument);
    if (tag != null && tag.length() > 23) {
      String message =
          String.format("The logging tag can be at most 23 characters, was %1$d (%2$s)",
              tag.length(), tag);
      context.report(ISSUE_TAG_LENGTH, argument, context.getLocation(argument), message);
    }
  }

  private static void checkFormatArguments(JavaContext context, PsiMethodCallExpression call) {
    PsiExpression[] arguments = call.getArgumentList().getExpressions();
    if (arguments.length == 0) {
      return;
    }

    int startIndexOfArguments = 1;
    PsiExpression formatStringArg = arguments[0];
    if (isSubclassOf(context, formatStringArg, Throwable.class)) {
      if (arguments.length == 1) {
        return;
      }
      formatStringArg = arguments[1];
      startIndexOfArguments++;
    }

    String formatString = findLiteralValue(formatStringArg);
    // We passed for example a method call
    if (formatString == null) {
      return;
    }

    int argumentCount = getFormatArgumentCount(formatString);
    int passedArgCount = arguments.length - startIndexOfArguments;
    if (argumentCount < passedArgCount) {
      context.report(ISSUE_ARG_COUNT, call, context.getLocation(call), String.format(
          "Wrong argument count, format string `%1$s` requires "
              + "`%2$d` but format call supplies `%3$d`", formatString, argumentCount,
          passedArgCount));
      return;
    }

    if (argumentCount == 0) {
      return;
    }

    List<String> types = getStringArgumentTypes(formatString);
    PsiExpression argument = null;
    int argumentIndex = startIndexOfArguments;
    boolean valid;
    for (int i = 0; i < types.size(); i++) {
      String formatType = types.get(i);
      if (argumentIndex != arguments.length) {
        argument = arguments[argumentIndex++];
      } else {
        context.report(ISSUE_ARG_COUNT, call, context.getLocation(call), String.format(
            "Wrong argument count, format string `%1$s` requires "
                + "`%2$d` but format call supplies `%3$d`", formatString, argumentCount,
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
        switch(last) {
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
            valid = type == Integer.TYPE
                    || type == Calendar.class
                    || type == Date.class;
            if (!valid) {
              String message = String.format("Wrong argument type for date formatting argument '#%1$d' "
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

  private static Class<?> getType(PsiExpression expression) {
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

    PsiType type = expression.getType();
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

  private static boolean isSubclassOf(JavaContext context, PsiExpression expression, Class<?> cls) {
    PsiType expressionType = expression.getType();
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

  private static String findLiteralValue(PsiExpression argument) {
    if (argument instanceof PsiLiteralExpression) {
      PsiLiteralExpression literalExpression = (PsiLiteralExpression) argument;
      Object value = literalExpression.getValue();
      if (value instanceof String) {
        return (String) value;
      }
    } else if (argument instanceof PsiBinaryExpression) {
      PsiBinaryExpression binaryExpression = (PsiBinaryExpression) argument;
      if (binaryExpression.getOperationTokenType() == JavaTokenType.PLUS) {
        String left = findLiteralValue(binaryExpression.getLOperand());
        String right = findLiteralValue(binaryExpression.getROperand());
        if (left != null && right != null) {
          return left + right;
        }
      }
    } else if (argument instanceof PsiReferenceExpression) {
      PsiReferenceExpression referenceExpression = (PsiReferenceExpression) argument;
      PsiElement resolved = referenceExpression.resolve();
      if (resolved instanceof PsiField) {
        PsiField field = (PsiField) resolved;
        Object value = field.computeConstantValue();
        if (value instanceof String) {
          return (String) value;
        }
      }
    }

    return null;
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

  private static void checkMethodArguments(JavaContext context, PsiMethodCallExpression call) {
    PsiExpression[] arguments = call.getArgumentList().getExpressions();
    for (int i = 0; i < arguments.length; i++) {
      PsiExpression argument = arguments[i];
      if (checkElement(context, call, argument)) {
        break;
      }
      if (i > 0 && isSubclassOf(context, argument, Throwable.class)) {
        context.report(ISSUE_THROWABLE, call, context.getLocation(call),
            "Throwable should be first argument");
      }
    }
  }

  private static void checkExceptionLogging(JavaContext context, PsiMethodCallExpression call) {
    PsiExpression[] arguments = call.getArgumentList().getExpressions();

    if (arguments.length > 1) {
      boolean isFirstParameterThrowable = isSubclassOf(context, arguments[0], Throwable.class);

      if (isFirstParameterThrowable) {
        PsiExpression secondArgument = arguments[1];
        String message = findLiteralValue(secondArgument);

        boolean callsGetMessage = false;

        if (secondArgument instanceof PsiMethodCallExpression) {
          PsiMethodCallExpression callExpression = (PsiMethodCallExpression) secondArgument;
          callsGetMessage = callExpression.getMethodExpression().getCanonicalText().endsWith("getMessage");
        }

        if (callsGetMessage) {
          context.report(ISSUE_EXCEPTION_LOGGING, secondArgument, context.getLocation(secondArgument),
              "Explicitly logging exception message is redundant");
        } else if (message == null || "".equals(message)) {
          context.report(ISSUE_EXCEPTION_LOGGING, secondArgument, context.getLocation(secondArgument),
              "Use single-argument log method instead of null/empty message");
        }
      }
    }
  }

  private static boolean checkElement(JavaContext context, PsiMethodCallExpression call,
      PsiElement element) {
    if (element instanceof PsiBinaryExpression) {
      Class argumentType = getType((PsiBinaryExpression) element);
      if (argumentType == String.class) {
        context.report(ISSUE_BINARY, call, context.getLocation(element),
            "Replace String concatenation with Timber's string formatting");
        return true;
      }
    } else if (element instanceof PsiIfStatement || element instanceof PsiConditionalExpression) {
      return checkConditionalUsage(context, call, element);
    }
    return false;
  }

  private static boolean checkConditionalUsage(JavaContext context, PsiMethodCallExpression call,
      PsiElement element) {
    PsiElement thenElement;
    PsiElement elseElement;
    if (element instanceof PsiIfStatement) {
      PsiIfStatement ifArg = (PsiIfStatement) element;
      thenElement = ifArg.getThenBranch();
      elseElement = ifArg.getElseBranch();
    } else if (element instanceof PsiConditionalExpression) {
      PsiConditionalExpression inlineIfArg = (PsiConditionalExpression) element;
      thenElement = inlineIfArg.getThenExpression();
      elseElement = inlineIfArg.getElseExpression();
    } else {
      return false;
    }
    if (checkElement(context, call, thenElement)) {
      return false;
    }
    return checkElement(context, call, elseElement);
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
  public static final Issue ISSUE_TAG_LENGTH =
      Issue.create("TimberTagLength", "Too Long Log Tags", "Log tags are only allowed to be at most"
              + " 23 tag characters long.", Category.CORRECTNESS, 5, Severity.ERROR,
          new Implementation(WrongTimberUsageDetector.class, Scope.JAVA_FILE_SCOPE));
  public static final Issue ISSUE_EXCEPTION_LOGGING =
      Issue.create("TimberExceptionLogging", "Exception Logging", "Explicitly including the"
              + " exception message is redundant when supplying an exception to log.",
          Category.CORRECTNESS, 3, Severity.WARNING,
          new Implementation(WrongTimberUsageDetector.class, Scope.JAVA_FILE_SCOPE));
}
