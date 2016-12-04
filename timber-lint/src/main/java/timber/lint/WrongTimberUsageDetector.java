package timber.lint;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.tools.lint.checks.StringFormatDetector;
import com.android.tools.lint.client.api.JavaParser;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.android.tools.lint.detector.api.Speed;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.ast.AstVisitor;
import lombok.ast.BinaryExpression;
import lombok.ast.BinaryOperator;
import lombok.ast.BooleanLiteral;
import lombok.ast.CharLiteral;
import lombok.ast.DescribedNode;
import lombok.ast.Expression;
import lombok.ast.ExpressionStatement;
import lombok.ast.FloatingPointLiteral;
import lombok.ast.If;
import lombok.ast.InlineIfExpression;
import lombok.ast.IntegralLiteral;
import lombok.ast.MethodInvocation;
import lombok.ast.Node;
import lombok.ast.NullLiteral;
import lombok.ast.StrictListAccessor;
import lombok.ast.StringLiteral;
import lombok.ast.VariableReference;

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

public final class WrongTimberUsageDetector extends Detector implements Detector.JavaScanner {
  private final static String GET_STRING_METHOD = "getString";
  private final static String TIMBER_TREE_LOG_METHOD_REGEXP = "(v|d|i|w|e|wtf)";

  @NonNull @Override public Speed getSpeed() {
    return Speed.NORMAL;
  }

  @Override public List<String> getApplicableMethodNames() {
    return Arrays.asList("tag", "format", "v", "d", "i", "w", "e", "wtf");
  }

  @Override public void visitMethod(@NonNull JavaContext context, AstVisitor visitor,
      @NonNull MethodInvocation node) {
    String methodName = node.astName().getDescription();
    if ("format".equals(methodName)) {
      if (!(node.astOperand() instanceof VariableReference)) {
        return;
      }
      VariableReference ref = (VariableReference) node.astOperand();
      if (!"String".equals(ref.astIdentifier().astValue())) {
        return;
      }
      // Found a String.format call
      // Look outside to see if we inside of a Timber call
      Node current = node.getParent();
      while (current != null && !(current instanceof ExpressionStatement)) {
        current = current.getParent();
      }
      if (current == null) {
        return;
      }
      ExpressionStatement statement = (ExpressionStatement) current;
      if (!Pattern.matches("^Timber\\." + TIMBER_TREE_LOG_METHOD_REGEXP + ".*", statement.toString())) {
        return;
      }
      context.report(ISSUE_FORMAT, node, context.getLocation(node),
          "Using 'String#format' inside of 'Timber'");
    } else if ("tag".equals(methodName)) {
      Object expression = node.astOperand();
      if (expression instanceof VariableReference) {
        VariableReference ref = (VariableReference) expression;
        if (!"Timber".equals(ref.astIdentifier().astValue())) {
          return;
        }
      }

      if (node.astArguments().isEmpty()) {
        return;
      }
      Node argument = node.astArguments().iterator().next();
      String tag = findLiteralValue(context, argument);
      if (tag != null && tag.length() > 23) {
        String message = String.format(
            "The logging tag can be at most 23 characters, was %1$d (%2$s)",
            tag.length(), tag);
        context.report(ISSUE_TAG_LENGTH, node, context.getLocation(argument), message);
      }
    } else if (node.astOperand() instanceof VariableReference) {
      VariableReference ref = (VariableReference) node.astOperand();
      if ("Log".equals(ref.astIdentifier().astValue())) {
        context.report(ISSUE_LOG, node, context.getRangeLocation(node, 0, node.astName(), 0),
            "Using 'Log' instead of 'Timber'");
        return;
      }
      if (!"Timber".equals(ref.astIdentifier().astValue())) {
        return;
      }
      checkThrowablePosition(context, node);
      checkArguments(context, node);
    } else if (isAstOperandTimberTagLogPattern(node)) {
      List<Node> siblings = node.astOperand().getParent().getChildren();

      List<Node> logNodes = siblings.subList(1, siblings.size());
      List<Expression> expressionNodes = new ArrayList<>(); // casted version of logNodes

      List<Node> logArgs = logNodes.subList(1, logNodes.size());
      for (Node n : logArgs) {
        if (!(n instanceof lombok.ast.Expression)) {
          continue; // this is a failure; see `if` guard just outside of this loop
        }
        expressionNodes.add((Expression) n);
      }

      // If we're short an Expression, then our guess about this node and its siblings may be wrong,
      if (expressionNodes.size() == logArgs.size()) {
        checkStringFormatArguments(
            context, node, expressionNodes.iterator(), expressionNodes.size());
      }
    }
  }

  /**
   * Rough guess as to whether node represents a `Timber.tag(TAG).v(...)` style log experssion.
   *
   * TODO: "Rough guess" because a proper check would check that `v` is being called on an instance
   * of a planted tree, but this is a quick & dirty hack in place of that (eg: generalized something
   * like a JavaContext.resolve check on `node`?).
   */
  private static boolean isAstOperandTimberTagLogPattern(MethodInvocation node) {
    Expression astOperand = node.astOperand();
    if (!(astOperand instanceof MethodInvocation)) {
      return false;
    }
    MethodInvocation m = (MethodInvocation) astOperand;

    if (!"Timber".equals(m.rawOperand().toString())
        || !"tag".equals(m.astName().toString())
        || !Pattern.matches(TIMBER_TREE_LOG_METHOD_REGEXP, node.astName().getDescription())) {
      // Is not of the form "Timber.tag(...).w(...)" (where "w()" can be any valid log method)
      return false;
    }

    return (m.getParent().getChildren().get(1) instanceof DescribedNode);
  }

  private static void checkArguments(JavaContext context, MethodInvocation node) {
    StrictListAccessor<Expression, MethodInvocation> astArguments = node.astArguments();
    checkStringFormatArguments(context, node, astArguments.iterator(), astArguments.size());
  }

  private static void checkStringFormatArguments(
      JavaContext context,
      MethodInvocation reportNode,
      Iterator<Expression> logArguments,
      int originalArgSize) {
    if (!logArguments.hasNext()) {
      return;
    }
    int startIndexOfArguments = 1;
    Expression formatStringArg = logArguments.next();
    if (formatStringArg instanceof VariableReference) {
      if (isSubclassOf(context, (VariableReference) formatStringArg, Throwable.class)) {
        formatStringArg = logArguments.next();
        startIndexOfArguments++;
      }
    }

    String formatString = findLiteralValue(context, formatStringArg);
    // We passed for example a method call
    if (formatString == null) {
      return;
    }
    int argumentCount = getFormatArgumentCount(formatString);
    int passedArgCount = originalArgSize - startIndexOfArguments;
    if (argumentCount < passedArgCount) {
      context.report(ISSUE_ARG_COUNT, reportNode, context.getLocation(reportNode), String.format(
              "Wrong argument count, format string `%1$s` requires "
                  + "`%2$d` but format call supplies `%3$d`", formatString, argumentCount,
              passedArgCount));
      return;
    }

    if (argumentCount == 0) {
      return;
    }

    List<String> types = getStringArgumentTypes(formatString);
    Expression argument = null;
    boolean valid;
    for (int i = 0; i < types.size(); i++) {
      String formatType = types.get(i);
      if (logArguments.hasNext()) {
        argument = logArguments.next();
      } else {
        context.report(ISSUE_ARG_COUNT, reportNode, context.getLocation(reportNode), String.format(
                "Wrong argument count, format string `%1$s` requires "
                    + "`%2$d` but format call supplies `%3$d`", formatString, argumentCount,
                passedArgCount));
      }

      char last = formatType.charAt(formatType.length() - 1);
      if (formatType.length() >= 2
          && Character.toLowerCase(formatType.charAt(formatType.length() - 2)) == 't') {
        // Date time conversion.
        // TODO
        continue;
      }
      Class type = getType(context, argument);
      if (type != null) {
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
          context.report(ISSUE_ARG_TYPES, reportNode, context.getLocation(argument), message);
        }
      }
    }
  }

  private static Class<?> getType(JavaContext context, Expression expression) {
    if (expression == null) {
      return null;
    }

    if (expression instanceof MethodInvocation) {
      MethodInvocation method = (MethodInvocation) expression;
      String methodName = method.astName().astValue();
      if (methodName.equals(GET_STRING_METHOD)) {
        return String.class;
      }
    } else if (expression instanceof StringLiteral) {
      return String.class;
    } else if (expression instanceof IntegralLiteral) {
      return Integer.TYPE;
    } else if (expression instanceof FloatingPointLiteral) {
      return Float.TYPE;
    } else if (expression instanceof CharLiteral) {
      return Character.TYPE;
    } else if (expression instanceof BooleanLiteral) {
      return Boolean.TYPE;
    } else if (expression instanceof NullLiteral) {
      return Object.class;
    }

    if (context != null) {
      JavaParser.TypeDescriptor type = context.getType(expression);
      if (type != null) {
        Class<?> typeClass = getTypeClass(type);
        if (typeClass != null) {
          return typeClass;
        } else {
          return Object.class;
        }
      }
    }

    return null;
  }

  private static Class<?> getTypeClass(@Nullable JavaParser.TypeDescriptor type) {
    if (type != null) {
      return getTypeClass(type.getName());
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
    } else {
      return null;
    }
  }

  private static boolean isSubclassOf(JavaContext context, VariableReference variableReference,
      Class<?> clazz) {
    JavaParser.ResolvedNode resolved = context.resolve(variableReference);
    if (resolved instanceof JavaParser.ResolvedVariable) {
      JavaParser.ResolvedVariable resolvedVariable = (JavaParser.ResolvedVariable) resolved;
      JavaParser.ResolvedClass typeClass = resolvedVariable.getType().getTypeClass();
      return (typeClass != null && typeClass.isSubclassOf(clazz.getName(), false));
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
        types.add(matcher.group(6));
      } else {
        break;
      }
    }
    return types;
  }

  private static String findLiteralValue(@NonNull JavaContext context, @NonNull Node argument) {
    if (argument instanceof StringLiteral) {
      return ((StringLiteral) argument).astValue();
    } else if (argument instanceof BinaryExpression) {
      BinaryExpression expression = (BinaryExpression) argument;
      if (expression.astOperator() == BinaryOperator.PLUS) {
        String left = findLiteralValue(context, expression.astLeft());
        String right = findLiteralValue(context, expression.astRight());
        if (left != null && right != null) {
          return left + right;
        }
      }
    } else {
      JavaParser.ResolvedNode resolved = context.resolve(argument);
      if (resolved instanceof JavaParser.ResolvedField) {
        JavaParser.ResolvedField field = (JavaParser.ResolvedField) resolved;
        Object value = field.getValue();
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

  private static void checkThrowablePosition(JavaContext context, MethodInvocation node) {
    int index = 0;
    for (Node argument : node.astArguments()) {
      if (checkNode(context, node, argument)) {
        break;
      }
      if (argument instanceof VariableReference) {
        VariableReference variableReference = (VariableReference) argument;
        if (index > 0 && isSubclassOf(context, variableReference, Throwable.class)) {
          context.report(ISSUE_THROWABLE, node, context.getLocation(node),
              "Throwable should be first argument");
        }
      }
      index++;
    }
  }

  private static boolean checkNode(JavaContext context, MethodInvocation node, Node argument) {
    if (argument instanceof BinaryExpression) {
      Class argumentType = getType(context, (BinaryExpression) argument);
      if (argumentType == String.class) {
        context.report(ISSUE_BINARY, node, context.getLocation(argument),
            "Replace String concatenation with Timber's string formatting");
        return true;
      }
    } else if (argument instanceof If || argument instanceof InlineIfExpression) {
      return checkConditionalUsage(context, node, argument);
    }
    return false;
  }

  private static boolean checkConditionalUsage(JavaContext context, MethodInvocation node,
      Node arg) {
    Node thenStatement;
    Node elseStatement;
    if (arg instanceof If) {
      If ifArg = (If) arg;
      thenStatement = ifArg.astStatement();
      elseStatement = ifArg.astElseStatement();
    } else if (arg instanceof InlineIfExpression) {
      InlineIfExpression inlineIfArg = (InlineIfExpression) arg;
      thenStatement = inlineIfArg.astIfFalse();
      elseStatement = inlineIfArg.astIfTrue();
    } else {
      return false;
    }
    if (checkNode(context, node, thenStatement)) {
      return false;
    }
    return checkNode(context, node, elseStatement);
  }

  static Issue[] getIssues() {
    return new Issue[] {
        ISSUE_LOG, ISSUE_FORMAT, ISSUE_THROWABLE, ISSUE_BINARY, ISSUE_ARG_COUNT, ISSUE_ARG_TYPES,
        ISSUE_TAG_LENGTH
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
}
