package com.example.timber.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import butterknife.OnClick;
import com.example.timber.ExampleActivity;
import com.example.timber.R;
import javax.inject.Inject;
import timber.log.Timber;

import static android.widget.Toast.LENGTH_SHORT;

public class DemoActivity extends ExampleActivity {
  @Inject Timber timber;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.demo_activity);
  }

  @OnClick({ R.id.hello, R.id.hey, R.id.hi })
  public void greetingClicked(Button button) {
    timber.i("A button with ID %s was clicked to say '%s'.", button.getId(), button.getText());
    Toast.makeText(this, "Check logcat for a greeting!", LENGTH_SHORT).show();
  }
}
