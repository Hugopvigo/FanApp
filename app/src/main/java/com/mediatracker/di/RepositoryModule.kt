package com.mediatracker.di

import com.mediatracker.data.repository.MediaRepositoryImpl
import com.mediatracker.data.repository.UserRepositoryImpl
import com.mediatracker.domain.repository.MediaRepository
import com.mediatracker.domain.repository.UserRepository
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @dagger.Binds
    abstract fun bindMediaRepository(impl: MediaRepositoryImpl): MediaRepository

    @dagger.Binds
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
}
