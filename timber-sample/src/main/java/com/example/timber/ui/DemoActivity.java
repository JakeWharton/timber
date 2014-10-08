package com.example.timber.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.OnClick;
import com.example.timber.R;
import timber.log.Timber;

import static android.widget.Toast.LENGTH_SHORT;

public class DemoActivity extends Activity {
  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.demo_activity);
    ButterKnife.inject(this);
    Timber.tag("LifeCycles");
    Timber.d("Activity Created");
  }

  @OnClick({ R.id.hello, R.id.hey, R.id.hi })
  public void greetingClicked(Button button) {
    Timber.i("A button with ID %s was clicked to say '%s'.", button.getId(), button.getText());
    Toast.makeText(this, "Check logcat for a greeting!", LENGTH_SHORT).show();
  }
}
