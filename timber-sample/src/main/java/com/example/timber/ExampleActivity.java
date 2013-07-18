package com.example.timber;

import android.app.Activity;
import android.os.Bundle;
import butterknife.Views;

/** Base activity which handles Dagger and Butter Knife injection. */
public abstract class ExampleActivity extends Activity {
  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ((ExampleApp) getApplication()).inject(this);
  }

  @Override public void setContentView(int layoutResID) {
    super.setContentView(layoutResID);
    Views.inject(this);
  }
}
