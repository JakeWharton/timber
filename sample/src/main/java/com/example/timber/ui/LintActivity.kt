package com.example.timber.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.Log
import timber.log.Timber
import timber.log.debug
import timber.log.warn

import java.lang.String.format

@SuppressLint("Registered") //
class LintActivity : Activity() {

    private val string: String
        get() = "foo"

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // LogNotTimber
        Log.d("TAG", "msg")
        Log.d("TAG", "msg", Exception())
        android.util.Log.d("TAG", "msg")
        android.util.Log.d("TAG", "msg", Exception())

        // StringFormatInTimber
        Timber.warn { String.format("%s", string) }
        Timber.warn { format("%s", string) }

        // ThrowableNotAtBeginning
        Timber.debug{ format("%s", Exception()) }

        // BinaryOperationInTimber
        val foo = "foo"
        val bar = "bar"
        Timber.debug {  "foo" + "bar" }
        Timber.debug { "foo$bar" }
        Timber.debug { "foo" + "bar" }
        Timber.debug { foo + bar }

        /*
        // TimberArgCount
        Timber.d("%s %s", "arg0")
        Timber.d("%s", "arg0", "arg1")
        Timber.tagged("tag").d("%s %s", "arg0")
        Timber.tagged("tag").d("%s", "arg0", "arg1")

        // TimberArgTypes
        Timber.d("%d", "arg0")
        Timber.tagged("tag").d("%d", "arg0")

        // TimberTagLength
        Timber.tagged("abcdefghijklmnopqrstuvwx")
        Timber.tagged("abcdefghijklmnopqrstuvw" + "x")

        // TimberExceptionLogging
        Timber.d(Exception(), Exception().message)
        Timber.d(Exception(), "")
        Timber.d(Exception(), null)
        Timber.d(Exception().message)
        */
    }
}
