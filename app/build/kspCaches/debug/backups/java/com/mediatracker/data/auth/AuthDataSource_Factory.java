package com.mediatracker.data.auth;

import com.google.firebase.auth.FirebaseAuth;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class AuthDataSource_Factory implements Factory<AuthDataSource> {
  private final Provider<FirebaseAuth> authProvider;

  private AuthDataSource_Factory(Provider<FirebaseAuth> authProvider) {
    this.authProvider = authProvider;
  }

  @Override
  public AuthDataSource get() {
    return newInstance(authProvider.get());
  }

  public static AuthDataSource_Factory create(Provider<FirebaseAuth> authProvider) {
    return new AuthDataSource_Factory(authProvider);
  }

  public static AuthDataSource newInstance(FirebaseAuth auth) {
    return new AuthDataSource(auth);
  }
}
