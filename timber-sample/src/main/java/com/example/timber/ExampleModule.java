package com.example.timber;

import com.example.timber.ui.DemoActivity;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;
import timber.log.Timber;

@Module(injects = DemoActivity.class)
public class ExampleModule {
  @Provides @Singleton Timber provideTimber() {
    return BuildConfig.DEBUG ? Timber.DEBUG : Timber.PROD;
  }
}
