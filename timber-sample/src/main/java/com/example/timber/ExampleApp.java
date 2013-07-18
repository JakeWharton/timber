package com.example.timber;

import android.app.Application;
import dagger.ObjectGraph;

public class ExampleApp extends Application {
  private ObjectGraph objectGraph;

  @Override public void onCreate() {
    super.onCreate();
    objectGraph = ObjectGraph.create(new ExampleModule());
  }

  public void inject(Object thing) {
    objectGraph.inject(thing);
  }
}
