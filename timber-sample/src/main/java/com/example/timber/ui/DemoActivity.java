package com.example.timber.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.OnClick;
import com.example.timber.R;
import timber.log.Timber;

import static android.widget.Toast.LENGTH_SHORT;

public class DemoActivity extends Activity {
  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.demo_activity);
    ButterKnife.bind(this);
    Timber.tag("LifeCycles");
    Timber.d("Activity Created");
  }

  @OnClick({ R.id.hello, R.id.hey, R.id.hi })
  public void greetingClicked(Button button) {
    Timber.i("A button with ID %s was clicked to say '%s'.", button.getId(), button.getText());
    Map<String, Object> eventMetadata = new HashMap<>();
    eventMetadata.put("event-type", "button-pressed");
    eventMetadata.put("button-id", String.valueOf(button.getId()));
    Timber.v(eventMetadata, "This is an example of a structured log");
    Toast.makeText(this, "Check logcat for a greeting!", LENGTH_SHORT).show();
  }
}
