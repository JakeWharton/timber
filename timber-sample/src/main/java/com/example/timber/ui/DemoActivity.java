package com.example.timber.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.example.timber.R;

import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Lumber;
import timber.log.Timber;

import static android.widget.Toast.LENGTH_SHORT;

public class DemoActivity extends Activity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.demo_activity);
    ButterKnife.bind(this);
    Timber.tag("LifeCycles");
    Lumber.startSplit(this.getLocalClassName(), "Demo Split");
    Timber.d("Activity Created");
    Lumber.addSplit(this.getLocalClassName(), "Activity created");
  }

  @Override
  protected void onResume() {
    super.onResume();
    Lumber.addSplit(this.getLocalClassName(), "onResume called");
  }

  @OnClick({ R.id.hello, R.id.hey, R.id.hi })
  public void greetingClicked(Button button) {
    Lumber.addSplit(this.getLocalClassName(), "Greeting clicked");
    Timber.i("A button with ID %s was clicked to say '%s'.", button.getId(), button.getText());
    Toast.makeText(this, "Check logcat for a greeting!", LENGTH_SHORT).show();
    Lumber.addSplit(this.getLocalClassName(), "Toast shown");
    Lumber.dumpD(this.getLocalClassName());
  }

  @OnClick(R.id.clearLumber)
  public void clearLumber(Button button) {

    Lumber.resetSplit(this.getLocalClassName());
    Lumber.addSplit(this.getLocalClassName(), "Lumber cleared");
    Toast.makeText(this, "Check logcat for a cleared Lumber!", LENGTH_SHORT).show();
    Lumber.dumpD(this.getLocalClassName());
  }
}
