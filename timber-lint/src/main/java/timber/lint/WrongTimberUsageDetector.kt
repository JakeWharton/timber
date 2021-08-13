package timber.lint

import com.android.tools.lint.detector.api.skipParentheses
import org.jetbrains.uast.util.isMethodCall
import com.android.tools.lint.detector.api.minSdkLessThan
import com.android.tools.lint.detector.api.isString
import com.android.tools.lint.detector.api.isKotlin
import org.jetbrains.uast.isInjectionHost
import org.jetbrains.uast.evaluateString
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.JavaContext
import org.jetbrains.uast.UCallExpression
import com.intellij.psi.PsiMethod
import com.android.tools.lint.client.api.JavaEvaluator
import com.android.tools.lint.detector.api.LintFix
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.UExpression
import com.android.tools.lint.detector.api.Incident
import org.jetbrains.uast.UQualifiedReferenceExpression
import org.jetbrains.uast.UBinaryExpression
import org.jetbrains.uast.UastBinaryOperator
import org.jetbrains.uast.UIfExpression
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiType
import com.intellij.psi.PsiClassType
import com.android.tools.lint.checks.StringFormatDetector
import com.android.tools.lint.client.api.TYPE_BOOLEAN
import com.android.tools.lint.client.api.TYPE_BOOLEAN_WRAPPER
import com.android.tools.lint.client.api.TYPE_BYTE
import com.android.tools.lint.client.api.TYPE_BYTE_WRAPPER
import com.android.tools.lint.client.api.TYPE_CHAR
import com.android.tools.lint.client.api.TYPE_DOUBLE
import com.android.tools.lint.client.api.TYPE_DOUBLE_WRAPPER
import com.android.tools.lint.client.api.TYPE_FLOAT
import com.android.tools.lint.client.api.TYPE_FLOAT_WRAPPER
import com.android.tools.lint.client.api.TYPE_INT
import com.android.tools.lint.client.api.TYPE_INTEGER_WRAPPER
import com.android.tools.lint.client.api.TYPE_LONG
import com.android.tools.lint.client.api.TYPE_LONG_WRAPPER
import com.android.tools.lint.client.api.TYPE_NULL
import com.android.tools.lint.client.api.TYPE_OBJECT
import com.android.tools.lint.client.api.TYPE_SHORT
import com.android.tools.lint.client.api.TYPE_SHORT_WRAPPER
import com.android.tools.lint.client.api.TYPE_STRING
import com.android.tools.lint.detector.api.Category.Companion.CORRECTNESS
import com.android.tools.lint.detector.api.Category.Companion.MESSAGES
import com.android.tools.lint.detector.api.ConstantEvaluator.evaluateString
import com.android.tools.lint.detector.api.Detector.UastScanner
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.Scope.Companion.JAVA_FILE_SCOPE
import com.android.tools.lint.detector.api.Severity.ERROR
import com.android.tools.lint.detector.api.Severity.WARNING
import org.jetbrains.uast.ULiteralExpression
import org.jetbrains.uast.USimpleNameReferenceExpression
import com.intellij.psi.PsiField
import com.intellij.psi.PsiParameter
import java.lang.Byte
import java.lang.Double
import java.lang.Float
import java.lang.IllegalStateException
import java.lang.Long
import java.lang.Short
import java.util.Calendar
import java.util.Date
import java.util.regex.Pattern

class WrongTimberUsageDetector : Detector(), UastScanner {
  override fun getApplicableMethodNames() = listOf("tag", "format", "v", "d", "i", "w", "e", "wtf")

  override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
    val methodName = node.methodName
    val evaluator = context.evaluator

    if ("format" == methodName &&
      (evaluator.isMemberInClass(method, "java.lang.String") ||
          evaluator.isMemberInClass(method, "kotlin.text.StringsKt__StringsJVMKt"))
    ) {
      checkNestedStringFormat(context, node)
      return
    }
    if ("tag" == methodName && evaluator.isMemberInClass(method, "timber.log.Timber")) {
      checkTagLengthIfMinSdkLessThan26(context, node)
    }
    if (evaluator.isMemberInClass(method, "android.util.Log")) {
      context.report(
        Incident(
          issue = ISSUE_LOG,
          scope = node,
          location = context.getLocation(node),
          message = "Using 'Log' instead of 'Timber'",
          fix = quickFixIssueLog(node)
        )
      )
      return
    }
    // Handles Timber.X(..) and Timber.tag(..).X(..) where X in (v|d|i|w|e|wtf).
    if (isTimberLogMethod(method, evaluator)) {
      checkMethodArguments(context, node)
      checkFormatArguments(context, node)
      checkExceptionLogging(context, node)
    }
  }

  private fun isTimberLogMethod(method: PsiMethod, evaluator: JavaEvaluator): Boolean {
    return evaluator.isMemberInClass(method, "timber.log.Timber")
        || evaluator.isMemberInClass(method, "timber.log.Timber.Companion")
        || evaluator.isMemberInClass(method, "timber.log.Timber.Tree")
  }

  private fun checkNestedStringFormat(context: JavaContext, call: UCallExpression) {
    var current: UElement? = call
    while (true) {
      current = skipParentheses(current!!.uastParent)
      if (current == null || current is UMethod) {
        // Reached AST root or code block node; String.format not inside Timber.X(..).
        return
      }
      if (current.isMethodCall()) {
        val psiMethod = (current as UCallExpression).resolve()
        if (psiMethod != null &&
          Pattern.matches(TIMBER_TREE_LOG_METHOD_REGEXP, psiMethod.name)
          && isTimberLogMethod(psiMethod, context.evaluator)
        ) {
          context.report(
            Incident(
              issue = ISSUE_FORMAT,
              scope = call,
              location = context.getLocation(call),
              message = "Using 'String#format' inside of 'Timber'",
              fix = quickFixIssueFormat(call)
            )
          )
          return
        }
      }
    }
  }

  private fun checkTagLengthIfMinSdkLessThan26(context: JavaContext, call: UCallExpression) {
    val argument = call.valueArguments[0]
    val tag = evaluateString(context, argument, true)
    if (tag != null && tag.length > 23) {
      context.report(
        Incident(
          issue = ISSUE_TAG_LENGTH,
          scope = argument,
          location = context.getLocation(argument),
          message = "The logging tag can be at most 23 characters, was ${tag.length} ($tag)",
          fix = quickFixIssueTagLength(argument, tag)
        ),
        // As of API 26, Log tags are no longer limited to 23 chars.
        constraint = minSdkLessThan(26)
      )
    }
  }

  private fun checkFormatArguments(context: JavaContext, call: UCallExpression) {
    val arguments = call.valueArguments
    val numArguments = arguments.size
    if (numArguments == 0) {
      return
    }

    var startIndexOfArguments = 1
    var formatStringArg = arguments[0]
    if (isSubclassOf(context, formatStringArg, Throwable::class.java)) {
      if (numArguments == 1) {
        return
      }
      formatStringArg = arguments[1]
      startIndexOfArguments++
    }

    val formatString = evaluateString(context, formatStringArg, false)
      ?: return // We passed for example a method call

    val formatArgumentCount = getFormatArgumentCount(formatString)
    val passedArgCount = numArguments - startIndexOfArguments
    if (formatArgumentCount < passedArgCount) {
      context.report(
        Incident(
          issue = ISSUE_ARG_COUNT,
          scope = call,
          location = context.getLocation(call),
          message = "Wrong argument count, format string `${formatString}` requires `${formatArgumentCount}` but format call supplies `${passedArgCount}`"
        )
      )
      return
    }

    if (formatArgumentCount == 0) {
      return
    }

    val types = getStringArgumentTypes(formatString)
    var argument: UExpression? = null
    var argumentIndex = startIndexOfArguments
    var valid: Boolean
    for (i in types.indices) {
      val formatType = types[i]
      if (argumentIndex != numArguments) {
        argument = arguments[argumentIndex++]
      } else {
        context.report(
          Incident(
            issue = ISSUE_ARG_COUNT,
            scope = call,
            location = context.getLocation(call),
            message = "Wrong argument count, format string `${formatString}` requires `${formatArgumentCount}` but format call supplies `${passedArgCount}`"
          )
        )
      }

      val type = getType(argument) ?: continue
      val last = formatType.last()
      if (formatType.length >= 2 && formatType[formatType.length - 2].toLowerCase() == 't') {
        // Date time conversion.
        when (last) {
          'H', 'I', 'k', 'l', 'M', 'S', 'L', 'N', 'p', 'z', 'Z', 's', 'Q', // time
          'B', 'b', 'h', 'A', 'a', 'C', 'Y', 'y', 'j', 'm', 'd', 'e', // date
          'R', 'T', 'r', 'D', 'F', 'c' -> { // date/time
            valid =
              type == Integer.TYPE || type == Calendar::class.java || type == Date::class.java || type == java.lang.Long.TYPE
            if (!valid) {
              context.report(
                Incident(
                  issue = ISSUE_ARG_TYPES,
                  scope = call,
                  location = context.getLocation(argument),
                  message = "Wrong argument type for date formatting argument '#${i + 1}' in `${formatString}`: conversion is '`${formatType}`', received `${type.simpleName}` (argument #${startIndexOfArguments + i + 1} in method call)"
                )
              )
            }
          }
          else -> {
            context.report(
              Incident(
                issue = ISSUE_FORMAT,
                scope = call,
                location = context.getLocation(argument),
                message = "Wrong suffix for date format '#${i + 1}' in `${formatString}`: conversion is '`${formatType}`', received `${type.simpleName}` (argument #${startIndexOfArguments + i + 1} in method call)"
              )
            )
          }
        }
        continue
      }

      valid = when (last) {
        'b', 'B' -> type == java.lang.Boolean.TYPE
        'x', 'X', 'd', 'o', 'e', 'E', 'f', 'g', 'G', 'a', 'A' -> {
          type == Integer.TYPE || type == java.lang.Float.TYPE || type == java.lang.Double.TYPE || type == java.lang.Long.TYPE || type == java.lang.Byte.TYPE || type == java.lang.Short.TYPE
        }
        'c', 'C' -> type == Character.TYPE
        'h', 'H' -> type != java.lang.Boolean.TYPE && !Number::class.java.isAssignableFrom(type)
        's', 'S' -> true
        else -> true
      }
      if (!valid) {
        context.report(
          Incident(
            issue = ISSUE_ARG_TYPES,
            scope = call,
            location = context.getLocation(argument),
            message = "Wrong argument type for formatting argument '#${i + 1}' in `${formatString}`: conversion is '`${formatType}`', received `${type.simpleName}` (argument #${startIndexOfArguments + i + 1} in method call)"
          )
        )
      }
    }
  }

  private fun getType(expression: UExpression?): Class<*>? {
    if (expression == null) {
      return null
    }
    if (expression is PsiMethodCallExpression) {
      val call = expression as PsiMethodCallExpression
      val method = call.resolveMethod() ?: return null
      val methodName = method.name
      if (methodName == GET_STRING_METHOD) {
        return String::class.java
      }
    } else if (expression is PsiLiteralExpression) {
      val literalExpression = expression as PsiLiteralExpression
      val expressionType = literalExpression.type
      when {
        isString(expressionType!!) -> return String::class.java
        expressionType === PsiType.INT -> return Integer.TYPE
        expressionType === PsiType.FLOAT -> return java.lang.Float.TYPE
        expressionType === PsiType.CHAR -> return Character.TYPE
        expressionType === PsiType.BOOLEAN -> return java.lang.Boolean.TYPE
        expressionType === PsiType.NULL -> return Any::class.java
      }
    }

    val type = expression.getExpressionType()
    if (type != null) {
      val typeClass = getTypeClass(type)
      return typeClass ?: Any::class.java
    }

    return null
  }

  private fun getTypeClass(type: PsiType?): Class<*>? {
    return when (type?.canonicalText) {
      null -> null
      TYPE_STRING, "String" -> String::class.java
      TYPE_INT -> Integer.TYPE
      TYPE_BOOLEAN -> java.lang.Boolean.TYPE
      TYPE_NULL -> Object::class.java
      TYPE_LONG -> Long.TYPE
      TYPE_FLOAT -> Float.TYPE
      TYPE_DOUBLE -> Double.TYPE
      TYPE_CHAR -> Character.TYPE
      TYPE_OBJECT -> null
      TYPE_INTEGER_WRAPPER, TYPE_SHORT_WRAPPER, TYPE_BYTE_WRAPPER, TYPE_LONG_WRAPPER -> Integer.TYPE
      TYPE_FLOAT_WRAPPER, TYPE_DOUBLE_WRAPPER -> Float.TYPE
      TYPE_BOOLEAN_WRAPPER -> java.lang.Boolean.TYPE
      TYPE_BYTE -> Byte.TYPE
      TYPE_SHORT -> Short.TYPE
      "Date", "java.util.Date" -> Date::class.java
      "Calendar", "java.util.Calendar" -> Calendar::class.java
      "BigDecimal", "java.math.BigDecimal" -> Float.TYPE
      "BigInteger", "java.math.BigInteger" -> Integer.TYPE
      else -> null
    }
  }

  private fun isSubclassOf(
    context: JavaContext, expression: UExpression, cls: Class<*>
  ): Boolean {
    val expressionType = expression.getExpressionType()
    if (expressionType is PsiClassType) {
      return context.evaluator.extendsClass(expressionType.resolve(), cls.name, false)
    }
    return false
  }

  private fun getStringArgumentTypes(formatString: String): List<String> {
    val types = mutableListOf<String>()
    val matcher = StringFormatDetector.FORMAT.matcher(formatString)
    var index = 0
    var prevIndex = 0

    while (true) {
      if (matcher.find(index)) {
        val matchStart = matcher.start()
        while (prevIndex < matchStart) {
          val c = formatString[prevIndex]
          if (c == '\\') {
            prevIndex++
          }
          prevIndex++
        }
        if (prevIndex > matchStart) {
          index = prevIndex
          continue
        }

        index = matcher.end()
        val str = formatString.substring(matchStart, matcher.end())
        if ("%%" == str || "%n" == str) {
          continue
        }
        val time = matcher.group(5)
        types += if ("t".equals(time, ignoreCase = true)) {
          time + matcher.group(6)
        } else {
          matcher.group(6)
        }
      } else {
        break
      }
    }
    return types
  }

  private fun getFormatArgumentCount(s: String): Int {
    val matcher = StringFormatDetector.FORMAT.matcher(s)
    var index = 0
    var prevIndex = 0
    var nextNumber = 1
    var max = 0
    while (true) {
      if (matcher.find(index)) {
        val value = matcher.group(6)
        if ("%" == value || "n" == value) {
          index = matcher.end()
          continue
        }
        val matchStart = matcher.start()
        while (prevIndex < matchStart) {
          val c = s[prevIndex]
          if (c == '\\') {
            prevIndex++
          }
          prevIndex++
        }
        if (prevIndex > matchStart) {
          index = prevIndex
          continue
        }

        var number: Int
        var numberString = matcher.group(1)
        if (numberString != null) {
          // Strip off trailing $
          numberString = numberString.substring(0, numberString.length - 1)
          number = numberString.toInt()
          nextNumber = number + 1
        } else {
          number = nextNumber++
        }
        if (number > max) {
          max = number
        }
        index = matcher.end()
      } else {
        break
      }
    }
    return max
  }

  private fun checkMethodArguments(context: JavaContext, call: UCallExpression) {
    call.valueArguments.forEachIndexed loop@{ i, argument ->
      if (checkElement(context, call, argument)) return@loop

      if (i > 0 && isSubclassOf(context, argument, Throwable::class.java)) {
        context.report(
          Incident(
            issue = ISSUE_THROWABLE,
            scope = call,
            location = context.getLocation(call),
            message = "Throwable should be first argument",
            fix = quickFixIssueThrowable(call, call.valueArguments, argument)
          )
        )
      }
    }
  }

  private fun checkExceptionLogging(context: JavaContext, call: UCallExpression) {
    val arguments = call.valueArguments
    val numArguments = arguments.size
    if (numArguments > 1 && isSubclassOf(context, arguments[0], Throwable::class.java)) {
      val messageArg = arguments[1]

      if (isLoggingExceptionMessage(context, messageArg)) {
        context.report(
          Incident(
            issue = ISSUE_EXCEPTION_LOGGING,
            scope = messageArg,
            location = context.getLocation(call),
            message = "Explicitly logging exception message is redundant",
            fix = quickFixRemoveRedundantArgument(messageArg)
          )
        )
        return
      }

      val s = evaluateString(context, messageArg, true)
      if (s == null && !canEvaluateExpression(messageArg)) {
        // Parameters and non-final fields can't be evaluated.
        return
      }

      if (s == null || s.isEmpty()) {
        context.report(
          Incident(
            issue = ISSUE_EXCEPTION_LOGGING,
            scope = messageArg,
            location = context.getLocation(call),
            message = "Use single-argument log method instead of null/empty message",
            fix = quickFixRemoveRedundantArgument(messageArg)
          )
        )
      }
    } else if (numArguments == 1 && !isSubclassOf(context, arguments[0], Throwable::class.java)) {
      val messageArg = arguments[0]

      if (isLoggingExceptionMessage(context, messageArg)) {
        context.report(
          Incident(
            issue = ISSUE_EXCEPTION_LOGGING,
            scope = messageArg,
            location = context.getLocation(call),
            message = "Explicitly logging exception message is redundant",
            fix = quickFixReplaceMessageWithThrowable(messageArg)
          )
        )
      }
    }
  }

  private fun isLoggingExceptionMessage(context: JavaContext, arg: UExpression): Boolean {
    if (arg !is UQualifiedReferenceExpression) {
      return false
    }

    val psi = arg.sourcePsi
    if (psi != null && isKotlin(psi.language)) {
      return isPropertyOnSubclassOf(context, arg, "message", Throwable::class.java)
    }

    val selector = arg.selector

    // what other UExpressions could be a selector?
    return if (selector !is UCallExpression) {
      false
    } else isCallFromMethodInSubclassOf(
      context = context,
      call = selector,
      methodName = "getMessage",
      classType = Throwable::class.java
    )
  }

  private fun canEvaluateExpression(expression: UExpression): Boolean {
    // TODO - try using CallGraph?
    if (expression is ULiteralExpression) {
      return true
    }
    if (expression !is USimpleNameReferenceExpression) {
      return false
    }
    val resolvedElement = expression.resolve()
    return !(resolvedElement is PsiField || resolvedElement is PsiParameter)
  }

  private fun isCallFromMethodInSubclassOf(
    context: JavaContext, call: UCallExpression, methodName: String, classType: Class<*>
  ): Boolean {
    val method = call.resolve()
    return method != null
        && methodName == call.methodName
        && context.evaluator.isMemberInSubClassOf(method, classType.canonicalName, false)
  }

  private fun isPropertyOnSubclassOf(
    context: JavaContext,
    expression: UQualifiedReferenceExpression,
    propertyName: String,
    classType: Class<*>
  ): Boolean {
    return isSubclassOf(context, expression.receiver, classType)
        && expression.selector.asSourceString() == propertyName
  }

  private fun checkElement(
    context: JavaContext, call: UCallExpression, element: UElement?
  ): Boolean {
    if (element is UBinaryExpression) {
      val operator = element.operator
      if (operator === UastBinaryOperator.PLUS || operator === UastBinaryOperator.PLUS_ASSIGN) {
        val argumentType = getType(element)
        if (argumentType == String::class.java) {
          if (element.leftOperand.isInjectionHost()
            && element.rightOperand.isInjectionHost()
          ) {
            return false
          }
          context.report(
            Incident(
              issue = ISSUE_BINARY,
              scope = call,
              location = context.getLocation(element),
              message = "Replace String concatenation with Timber's string formatting",
              fix = quickFixIssueBinary(element)
            )
          )
          return true
        }
      }
    } else if (element is UIfExpression) {
      return checkConditionalUsage(context, call, element)
    }
    return false
  }

  private fun checkConditionalUsage(
    context: JavaContext, call: UCallExpression, element: UElement
  ): Boolean {
    return if (element is UIfExpression) {
      if (checkElement(context, call, element.thenExpression)) {
        false
      } else {
        checkElement(context, call, element.elseExpression)
      }
    } else {
      false
    }
  }

  private fun quickFixIssueLog(logCall: UCallExpression): LintFix {
    val arguments = logCall.valueArguments
    val methodName = logCall.methodName
    val tag = arguments[0]

    // 1st suggestion respects author's tag preference.
    // 2nd suggestion drops it (Timber defaults to calling class name).
    var fixSource1 = "Timber.tag(${tag.asSourceString()})."
    var fixSource2 = "Timber."

    when (arguments.size) {
      2 -> {
        val msgOrThrowable = arguments[1]
        fixSource1 += "$methodName(${msgOrThrowable.asSourceString()})"
        fixSource2 += "$methodName(${msgOrThrowable.asSourceString()})"
      }
      3 -> {
        val msg = arguments[1]
        val throwable = arguments[2]
        fixSource1 += "$methodName(${throwable.sourcePsi?.text}, ${msg.asSourceString()})"
        fixSource2 += "$methodName(${throwable.sourcePsi?.text}, ${msg.asSourceString()})"
      }
      else -> {
        throw IllegalStateException("android.util.Log overloads should have 2 or 3 arguments")
      }
    }

    val logCallSource = logCall.uastParent!!.sourcePsi?.text
    return fix().group()
      .add(
        fix().replace().text(logCallSource).shortenNames().reformat(true).with(fixSource1).build()
      )
      .add(
        fix().replace().text(logCallSource).shortenNames().reformat(true).with(fixSource2).build()
      )
      .build()
  }

  private fun quickFixIssueFormat(stringFormatCall: UCallExpression): LintFix {
    // Handles:
    // 1) String.format(..)
    // 2) format(...) [static import]
    val callReceiver = stringFormatCall.receiver
    var callSourceString = if (callReceiver == null) "" else "${callReceiver.asSourceString()}."
    callSourceString += stringFormatCall.methodName

    return fix().name("Remove String.format(...)").composite() //
      // Delete closing parenthesis of String.format(...)
      .add(fix().replace().pattern("$callSourceString\\(.*(\\))").with("").build())
      // Delete "String.format("
      .add(fix().replace().text("$callSourceString(").with("").build()).build()
  }

  private fun quickFixIssueThrowable(
    call: UCallExpression, arguments: List<UExpression>, throwable: UExpression
  ): LintFix {
    val rearrangedArgs = buildString {
      append(throwable.asSourceString())
      arguments.forEach { arg ->
        if (arg !== throwable) {
          append(", ${arg.asSourceString()}")
        }
      }
    }
    return fix()
      .replace()
      .pattern("\\." + call.methodName + "\\((.*)\\)")
      .with(rearrangedArgs)
      .build()
  }

  private fun quickFixIssueBinary(binaryExpression: UBinaryExpression): LintFix {
    val leftOperand = binaryExpression.leftOperand
    val rightOperand = binaryExpression.rightOperand
    val isLeftLiteral = leftOperand.isInjectionHost()
    val isRightLiteral = rightOperand.isInjectionHost()

    // "a" + "b" => "ab"
    if (isLeftLiteral && isRightLiteral) {
      return fix().replace()
        .text(binaryExpression.asSourceString())
        .with("\"${binaryExpression.evaluateString()}\"")
        .build()
    }

    val args: String = when {
      isLeftLiteral -> {
        "\"${leftOperand.evaluateString()}%s\", ${rightOperand.asSourceString()}"
      }
      isRightLiteral -> {
        "\"%s${rightOperand.evaluateString()}\", ${leftOperand.asSourceString()}"
      }
      else -> {
        "\"%s%s\", ${leftOperand.asSourceString()}, ${rightOperand.asSourceString()}"
      }
    }
    return fix().replace().text(binaryExpression.asSourceString()).with(args).build()
  }

  private fun quickFixIssueTagLength(argument: UExpression, tag: String): LintFix {
    val numCharsToTrim = tag.length - 23
    return fix().replace()
      .name("Strip last " + if (numCharsToTrim == 1) "char" else "$numCharsToTrim chars")
      .text(argument.asSourceString())
      .with("\"${tag.substring(0, 23)}\"")
      .build()
  }

  private fun quickFixRemoveRedundantArgument(arg: UExpression): LintFix {
    return fix().replace()
      .name("Remove redundant argument")
      .text(", ${arg.asSourceString()}")
      .with("")
      .build()
  }

  private fun quickFixReplaceMessageWithThrowable(arg: UExpression): LintFix {
    // guaranteed based on callers of this method
    val receiver = (arg as UQualifiedReferenceExpression).receiver
    return fix().replace()
      .name("Replace message with throwable")
      .text(arg.asSourceString())
      .with(receiver.asSourceString())
      .build()
  }

  companion object {
    private const val GET_STRING_METHOD = "getString"
    private const val TIMBER_TREE_LOG_METHOD_REGEXP = "(v|d|i|w|e|wtf)"

    val ISSUE_LOG = Issue.create(
      id = "LogNotTimber",
      briefDescription = "Logging call to Log instead of Timber",
      explanation = "Since Timber is included in the project, it is likely that calls to Log should instead be going to Timber.",
      category = MESSAGES,
      priority = 5,
      severity = WARNING,
      implementation = Implementation(WrongTimberUsageDetector::class.java, JAVA_FILE_SCOPE)
    )
    val ISSUE_FORMAT = Issue.create(
      id = "StringFormatInTimber",
      briefDescription = "Logging call with Timber contains String#format()",
      explanation = "Since Timber handles String.format automatically, you may not use String#format().",
      category = MESSAGES,
      priority = 5,
      severity = WARNING,
      implementation = Implementation(WrongTimberUsageDetector::class.java, JAVA_FILE_SCOPE)
    )
    val ISSUE_THROWABLE = Issue.create(
      id = "ThrowableNotAtBeginning",
      briefDescription = "Exception in Timber not at the beginning",
      explanation = "In Timber you have to pass a Throwable at the beginning of the call.",
      category = MESSAGES,
      priority = 5,
      severity = WARNING,
      implementation = Implementation(WrongTimberUsageDetector::class.java, JAVA_FILE_SCOPE)
    )
    val ISSUE_BINARY = Issue.create(
      id = "BinaryOperationInTimber",
      briefDescription = "Use String#format()",
      explanation = "Since Timber handles String#format() automatically, use this instead of String concatenation.",
      category = MESSAGES,
      priority = 5,
      severity = WARNING,
      implementation = Implementation(WrongTimberUsageDetector::class.java, JAVA_FILE_SCOPE)
    )
    val ISSUE_ARG_COUNT = Issue.create(
      id = "TimberArgCount",
      briefDescription = "Formatting argument types incomplete or inconsistent",
      explanation = "When a formatted string takes arguments, you need to pass at least that amount of arguments to the formatting call.",
      category = MESSAGES,
      priority = 9,
      severity = ERROR,
      implementation = Implementation(WrongTimberUsageDetector::class.java, JAVA_FILE_SCOPE)
    )
    val ISSUE_ARG_TYPES = Issue.create(
      id = "TimberArgTypes",
      briefDescription = "Formatting string doesn't match passed arguments",
      explanation = "The argument types that you specified in your formatting string does not match the types of the arguments that you passed to your formatting call.",
      category = MESSAGES,
      priority = 9,
      severity = ERROR,
      implementation = Implementation(WrongTimberUsageDetector::class.java, JAVA_FILE_SCOPE)
    )
    val ISSUE_TAG_LENGTH = Issue.create(
      id = "TimberTagLength",
      briefDescription = "Too Long Log Tags",
      explanation = "Log tags are only allowed to be at most" + " 23 tag characters long.",
      category = CORRECTNESS,
      priority = 5,
      severity = ERROR,
      implementation = Implementation(WrongTimberUsageDetector::class.java, JAVA_FILE_SCOPE)
    )
    val ISSUE_EXCEPTION_LOGGING = Issue.create(
      id = "TimberExceptionLogging",
      briefDescription = "Exception Logging",
      explanation = "Explicitly including the exception message is redundant when supplying an exception to log.",
      category = CORRECTNESS,
      priority = 3,
      severity = WARNING,
      implementation = Implementation(WrongTimberUsageDetector::class.java, JAVA_FILE_SCOPE)
    )

    val issues = arrayOf(
      ISSUE_LOG, ISSUE_FORMAT, ISSUE_THROWABLE, ISSUE_BINARY, ISSUE_ARG_COUNT, ISSUE_ARG_TYPES,
      ISSUE_TAG_LENGTH, ISSUE_EXCEPTION_LOGGING
    )
  }
}
