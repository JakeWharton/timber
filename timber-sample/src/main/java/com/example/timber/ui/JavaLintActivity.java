package com.example.timber.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.Nullable;
import timber.log.Timber;

import static java.lang.String.format;

@SuppressLint("Registered") //
public class JavaLintActivity extends Activity {
  /**
   * Below are some examples of how NOT to use Timber.
   *
   * To see how a particular lint issue behaves, comment/remove its corresponding id from the set
   * of SuppressLint ids below.
   */
  @SuppressLint({
      "LogNotTimber", //
      "StringFormatInTimber", //
      "ThrowableNotAtBeginning", //
      "BinaryOperationInTimber", //
      "TimberArgCount", //
      "TimberArgTypes", //
      "TimberTagLength", //
      "TimberExceptionLogging" //
  }) //
  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // LogNotTimber
    Log.d("TAG", "msg");
    Log.d("TAG", "msg", new Exception());
    android.util.Log.d("TAG", "msg");
    android.util.Log.d("TAG", "msg", new Exception());

    // StringFormatInTimber
    Timber.w(String.format("%s", getString()));
    Timber.w(format("%s", getString()));

    // ThrowableNotAtBeginning
    Timber.d("%s", new Exception());

    // BinaryOperationInTimber
    String foo = "foo";
    String bar = "bar";
    Timber.d("foo" + "bar");
    Timber.d("foo" + bar);
    Timber.d(foo + "bar");
    Timber.d(foo + bar);

    // TimberArgCount
    Timber.d("%s %s", "arg0");
    Timber.d("%s", "arg0", "arg1");
    Timber.tag("tag").d("%s %s", "arg0");
    Timber.tag("tag").d("%s", "arg0", "arg1");

    // TimberArgTypes
    Timber.d("%d", "arg0");
    Timber.tag("tag").d("%d", "arg0");

    // TimberTagLength
    Timber.tag("abcdefghijklmnopqrstuvwx");
    Timber.tag("abcdefghijklmnopqrstuvw" + "x");

    // TimberExceptionLogging
    Timber.d(new Exception(), new Exception().getMessage());
    Timber.d(new Exception(), "");
    Timber.d(new Exception(), null);
    Timber.d(new Exception().getMessage());
  }

  private String getString() {
    return "foo";
  }
}
