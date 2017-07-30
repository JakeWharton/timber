package com.example.timber.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import timber.log.Timber;

@SuppressLint("Registered") //
public class LintActivity extends Activity {
  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Below are some examples of how NOT to use Timber
    /*
    Log.d("TAG", "msg");

    Timber.w(String.format("%s", "arg0"));

    Exception e = new Exception();
    Timber.d("%s", e);

    Timber.d("Hello, " + "world!");

    Timber.d("%s %s", "arg0");

    Timber.d("%s", "arg0", "arg1");

    Timber.d("%d", "arg0");

    Timber.tag("abcdefghijklmnopqrstuvwx");

    Timber.tag("tag").d("%s %s", "arg0");

    Timber.tag("tag").d("%s", "arg0", "arg1");

    Timber.tag("tag").d("%d", "arg0");

    Timber.tag("abcdefghijklmnopqrstuvw" + "x");

    Timber.tag("abcdefghijklmnopqrstuvw" + new String("x"));
    */
  }
}
