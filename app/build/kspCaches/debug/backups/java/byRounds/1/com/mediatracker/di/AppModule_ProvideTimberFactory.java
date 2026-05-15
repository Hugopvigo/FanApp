package com.mediatracker.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import timber.log.Timber;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class AppModule_ProvideTimberFactory implements Factory<Timber.Tree> {
  @Override
  public Timber.Tree get() {
    return provideTimber();
  }

  public static AppModule_ProvideTimberFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static Timber.Tree provideTimber() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideTimber());
  }

  private static final class InstanceHolder {
    private static final AppModule_ProvideTimberFactory INSTANCE = new AppModule_ProvideTimberFactory();
  }
}
