package com.example.timber.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.Log
import timber.log.Timber
import java.lang.Exception
import java.lang.String.format

@SuppressLint("Registered")
class KotlinLintActivity : Activity() {
  /**
   * Below are some examples of how NOT to use Timber.
   *
   * To see how a particular lint issue behaves, comment/remove its corresponding id from the set
   * of SuppressLint ids below.
   */
  @SuppressLint(
    "LogNotTimber",
    "StringFormatInTimber",
    "ThrowableNotAtBeginning",
    "BinaryOperationInTimber",
    "TimberArgCount",
    "TimberArgTypes",
    "TimberTagLength",
    "TimberExceptionLogging"
  )
  @Suppress("RemoveRedundantQualifierName")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // LogNotTimber
    Log.d("TAG", "msg")
    Log.d("TAG", "msg", Exception())
    android.util.Log.d("TAG", "msg")
    android.util.Log.d("TAG", "msg", Exception())

    // StringFormatInTimber
    Timber.w(String.format("%s", getString()))
    Timber.w(format("%s", getString()))

    // ThrowableNotAtBeginning
    Timber.d("%s", Exception())

    // BinaryOperationInTimber
    val foo = "foo"
    val bar = "bar"
    Timber.d("foo" + "bar")
    Timber.d("foo$bar")
    Timber.d("${foo}bar")
    Timber.d("$foo$bar")

    // TimberArgCount
    Timber.d("%s %s", "arg0")
    Timber.d("%s", "arg0", "arg1")
    Timber.tag("tag").d("%s %s", "arg0")
    Timber.tag("tag").d("%s", "arg0", "arg1")

    // TimberArgTypes
    Timber.d("%d", "arg0")
    Timber.tag("tag").d("%d", "arg0")

    // TimberTagLength
    Timber.tag("abcdefghijklmnopqrstuvwx")
    Timber.tag("abcdefghijklmnopqrstuvw" + "x")

    // TimberExceptionLogging
    Timber.d(Exception(), Exception().message)
    Timber.d(Exception(), "")
    Timber.d(Exception(), null)
    Timber.d(Exception().message)
  }

  private fun getString() = "foo"
}