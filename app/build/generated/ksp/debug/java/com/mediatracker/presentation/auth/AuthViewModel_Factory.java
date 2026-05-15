package com.mediatracker.presentation.auth;

import com.mediatracker.data.auth.AuthDataSource;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
    "cast"
})
public final class AuthViewModel_Factory implements Factory<AuthViewModel> {
  private final Provider<AuthDataSource> authDataSourceProvider;

  public AuthViewModel_Factory(Provider<AuthDataSource> authDataSourceProvider) {
    this.authDataSourceProvider = authDataSourceProvider;
  }

  @Override
  public AuthViewModel get() {
    return newInstance(authDataSourceProvider.get());
  }

  public static AuthViewModel_Factory create(Provider<AuthDataSource> authDataSourceProvider) {
    return new AuthViewModel_Factory(authDataSourceProvider);
  }

  public static AuthViewModel newInstance(AuthDataSource authDataSource) {
    return new AuthViewModel(authDataSource);
  }
}
