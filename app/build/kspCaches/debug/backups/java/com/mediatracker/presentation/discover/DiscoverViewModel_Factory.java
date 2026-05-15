package com.mediatracker.presentation.discover;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class DiscoverViewModel_Factory implements Factory<DiscoverViewModel> {
  @Override
  public DiscoverViewModel get() {
    return newInstance();
  }

  public static DiscoverViewModel_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static DiscoverViewModel newInstance() {
    return new DiscoverViewModel();
  }

  private static final class InstanceHolder {
    static final DiscoverViewModel_Factory INSTANCE = new DiscoverViewModel_Factory();
  }
}
